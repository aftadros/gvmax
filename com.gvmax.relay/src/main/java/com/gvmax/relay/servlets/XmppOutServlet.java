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
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.gvmax.common.util.Enc;
import com.gvmax.common.util.StringUtil;

public class XmppOutServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(XmppOutServlet.class.getName());
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Enc enc = new Enc(Config.ENC_KEY, 128);
        String from = enc.decrypt(req.getParameter("from"));
        String to = enc.decrypt(req.getParameter("to"));
        String message = enc.decrypt(req.getParameter("message"));
        message = message.replaceAll("\\\\u0009 \\\\u0009\\\\u0009\\\\u0009\\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009\\\\u0009 \\\\u0009", "");
        message = message.replaceAll("\\u0009 \\u0009\\u0009\\u0009\\u0009 \\u0009 \\u0009 \\u0009 \\u0009 \\u0009 \\u0009 \\u0009\\u0009 \\u0009", "");

        if (from == null || to == null || message == null) {
            return;
        }
        message = message.replaceAll("\\\\u0009 \\\\u0009\\\\u0009\\\\u0009\\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009 \\\\u0009\\\\u0009 \\\\u0009", "");
        message = message.replaceAll("\\u0009 \\u0009\\u0009\\u0009\\u0009 \\u0009 \\u0009 \\u0009 \\u0009 \\u0009 \\u0009 \\u0009\\u0009 \\u0009", "");

        JID fromJid = new JID(from);
        JID toJid = new JID(to);
        XMPPService xmpp = XMPPServiceFactory.getXMPPService();
        boolean presence = xmpp.getPresence(toJid, fromJid).isAvailable();
        if (!presence) {
            logger.warning("unable to send xmpp: not present: from = " + from);
            resp.getWriter().print("not present");
        } else {
            String[] msgs = new String[] { message };
            if (message.length() > 2000) {
                msgs = StringUtil.split(message, 2000);
            }
            for (String msgText : msgs) {
                Message msg = new MessageBuilder().withFromJid(fromJid).withRecipientJids(toJid).withBody(msgText).build();
                xmpp.sendMessage(msg);
            }
            logger.info("sent xmpp from = " + from);
            resp.getWriter().print("ok");
        }
        resp.flushBuffer();
    }

}
