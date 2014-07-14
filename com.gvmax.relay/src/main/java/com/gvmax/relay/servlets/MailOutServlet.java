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

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailService.Message;
import com.google.appengine.api.mail.MailServiceFactory;
import com.gvmax.common.util.Enc;

public class MailOutServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(MailOutServlet.class.getName());
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Enc enc = new Enc(Config.ENC_KEY, 128);
            String from = enc.decrypt(req.getParameter("from"));
            if (from == null) {
                resp.sendError(404);
                return;
            }
            String replyTo = enc.decrypt(req.getParameter("replyTo"));
            String to = enc.decrypt(req.getParameter("to"));
            String bcc = enc.decrypt(req.getParameter("bcc"));
            String subject = enc.decrypt(req.getParameter("subject"));
            String text = enc.decrypt(req.getParameter("text"));
            String html = enc.decrypt(req.getParameter("html"));

            logger.info("Sending mail to " + to);

            MailService mailService = MailServiceFactory.getMailService();
            Message message = new MailService.Message();
            message.setSender(from);
            message.setTo(to.split(","));
            if (replyTo != null) {
                message.setReplyTo(replyTo);
            }
            if (bcc != null) {
                message.setBcc(bcc.split(","));
            }
            message.setSubject(subject);
            if (text != null) {
                message.setTextBody(text);
            }
            if (html != null) {
                message.setHtmlBody(html);
            }
            mailService.send(message);

            resp.getWriter().println("ok");
        } catch (Exception e) {
            e.printStackTrace(resp.getWriter());
        }
    }

}
