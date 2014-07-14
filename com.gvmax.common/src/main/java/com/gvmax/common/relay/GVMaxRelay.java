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
package com.gvmax.common.relay;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.gvmax.common.util.Enc;
import com.gvmax.common.util.NetUtil;

/**
 * Client library used to talk to GVMax Relay.
 */
public class GVMaxRelay {
    private static final Logger logger = Logger.getLogger(GVMaxRelay.class);
    private String appName;
    private String host;
    private String encKey;

    private String emailHost;
    private String emailSender;

    public GVMaxRelay() {}

    public GVMaxRelay(String host, String encKey, String emailHost, String emailSender) {
        this.appName = host.split("//", 2)[1].split("\\.", 2)[0];
        this.host = host;
        this.encKey = encKey;
        this.emailHost = emailHost;
        this.emailSender = emailSender;
    }

    public String getAppName() {
        return appName;
    }

    public String getHost() {
        return host;
    }

    public String getEmailHost() {
        return emailHost;
    }

    public String getEmailSender() {
        return emailSender;
    }

    public String getEncKey() {
        return encKey;
    }

    @Timed @ExceptionMetered
    public boolean sendEmail(String fromUser, String replyTo, String to, String subject, String text) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        Enc enc = new Enc(encKey, 128);
        params.put("from", enc.encrypt(fromUser));
        params.put("replyTo", enc.encrypt(replyTo));
        params.put("to", enc.encrypt(to));
        // params.put("bcc", bcc);
        params.put("subject", enc.encrypt(subject));
        params.put("text", enc.encrypt(text));
        // params.put("html", html);
        String res = NetUtil.doPost(host + "/mailOut", params);
        return res.startsWith("ok");
    }

    @Timed @ExceptionMetered
    public boolean sendXmppMessage(String from, String to, String message) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        Enc enc = new Enc(encKey, 128);
        params.put("from", enc.encrypt(from));
        params.put("to", enc.encrypt(to));
        params.put("message", enc.encrypt(message));
        String res = NetUtil.doPost(host + "/xmppOut", params);
        if (!res.startsWith("ok")) {
            logger.warn("Unable to send xmpp message, result = " + res);
        }
        return res.startsWith("ok");
    }

}
