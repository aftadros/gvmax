/*******************************************************************************
 * Copyright (c) 2013 Hani Naguib.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Hani Naguib - initial API and implementation
 ******************************************************************************/
package com.gvmax.relay.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.gvmax.common.util.Enc;
import com.gvmax.common.util.MiscUtils;
import com.gvmax.common.util.NetUtil;

public class XmppInServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(XmppInServlet.class.getName());
    private static final String SERVER_URL = Config.GVMAX_HOST + "/api/xmppIn";
    // private static final String serverUrl =
    // "https://naguib.homeip.net/gvmax/rpc/xmppIn";
    private static final AtomicLong COUNTER = new AtomicLong();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        XMPPService xmpp = XMPPServiceFactory.getXMPPService();
        Message message = null;
        try {
            message = xmpp.parseMessage(req);
        } catch (Exception e) {
            logger.warning("Received invalid xmpp message: " + e.getMessage());
            return;
        }
        if (message == null) {
            logger.warning("Received null xmpp message");
            return;
        }

        // Prepare message
        String reqId = this + "-" + COUNTER.incrementAndGet();
        String from = message.getFromJid().toString();
        String[] to = new String[message.getRecipientJids().length];
        for (int x = 0; x < to.length; x++) {
            to[x] = message.getRecipientJids()[x].toString();
        }
        String msg = message.getBody();
        // Clear pidgin bug
        msg = msg.replaceAll("\\\\u0009 \\\\u0009\\\\u0009\\\\u0009\\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009\\\\u0009 \\\\u0009", "");
        msg = msg.replaceAll("\\u0009 \\u0009\\u0009\\u0009\\u0009 \\u0009 \\u0009 \\u0009 \\u0009 \\u0009 \\u0009 \\u0009\\u0009 \\u0009", "");

        Map<String, String> params = new HashMap<String, String>();
        Enc enc = new Enc(Config.ENC_KEY, 128);
        params.put("reqId", enc.encrypt(reqId));
        params.put("from", enc.encrypt(from));
        params.put("to", enc.encrypt(toString(to)));
        params.put("msg", enc.encrypt(msg));

        // Send message
        Exception exception = null;
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                NetUtil.doPost(SERVER_URL, params);
                logger.info("xmpp message sent to server from " + from);
                return;
            } catch (Exception e) {
                exception = e;
                retryCount += 1;
                logger.warning(e.getMessage());
                try {
                    Thread.sleep(retryCount * 1000);
                } catch (InterruptedException ie) {
                    MiscUtils.emptyBlock();
                }
            }
        }
        logger.severe("Failed to talk to " + SERVER_URL + " : " + exception.getMessage());
        try {
            Message emsg = new MessageBuilder().withFromJid(message.getRecipientJids()[0]).withRecipientJids(message.getFromJid()).withBody("unable to contact gvmax: " + exception.getMessage()).build();
            xmpp.sendMessage(emsg);
        } catch (Exception e) {
            logger.severe("Unable to send error message : " + e.getMessage());
        }
    }

    private static String toString(String[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (String value : values) {
            buffer.append(value);
            buffer.append(",");
        }
        String retVal = buffer.toString();
        return retVal.substring(0, retVal.length() - 1);
    }

}
