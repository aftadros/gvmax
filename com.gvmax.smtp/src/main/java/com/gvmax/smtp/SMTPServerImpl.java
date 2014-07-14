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
package com.gvmax.smtp;

import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import com.gvmax.common.model.Email;
import com.gvmax.common.util.MetricsUtil;
import com.gvmax.data.queue.QueueDAO;

public class SMTPServerImpl {
    private static final Logger logger = Logger.getLogger(SMTPServerImpl.class);
    private SMTPServer server;
    private QueueDAO<Email> smtpQueue;

    public SMTPServerImpl(int port, QueueDAO<Email> smtpQueue) {
        this.smtpQueue = smtpQueue;
        server = new SMTPServer(new SimpleMessageListenerAdapter(new SMTPListener()));
        server.setPort(port);
        server.setHideTLS(true);
    }

    public void start() {
        server.start();
        logger.info("SMTPServerImpl started on port: " + server.getPort());
    }

    public void stop() {
        server.stop();
        logger.info("SMTPServerImpl at port: " + server.getPort() + " stopped.");
    }

    class SMTPListener implements SimpleMessageListener {

        @Override
        public void deliver(String from, String recipient, InputStream data) throws IOException {
            logger.debug("email received from:" + from + " to:" + recipient);
            MetricsUtil.getCounter(SMTPListener.class,"received").inc();
            // Extract Email
            Email email = null;
            try {
                email = extractEmail(from, recipient, data);
            } catch (Exception e) {
                logger.error("error processing email from: " + from + " error: " + e.getMessage(), e);
                MetricsUtil.getCounter(SMTPListener.class,"error").inc();
                return;
            }
            if (from.contains("thehealingmind")) {
                logger.warn("rejected: " + from + " TEXT =\n" + email.getText());
                MetricsUtil.getCounter(SMTPListener.class,"rejected").inc();
                return;
            }
            if (from.endsWith("amazonaws.com")) {
                logger.warn("rejected: " + from + " TEXT =\n" + email.getText());
                MetricsUtil.getCounter(SMTPListener.class,"rejected").inc();
                throw new TooMuchDataException("rejected");
            }
            // Queue email
            MetricsUtil.getCounter(SMTPListener.class,"delivered").inc();
            smtpQueue.enqueue(email);
        }

        private Email extractEmail(String from, String recipient, InputStream data) throws IOException, MessagingException {
            Email email = new Email();
            email.setTimestamp(System.currentTimeMillis());
            email.setFrom(from);
            email.setTo(recipient);
            // Create message
            MimeMessage msg = new MimeMessage(null, data);
            // Extract message text
            if (msg.getContent() instanceof MimeMultipart) {
                MimeMultipart t = (MimeMultipart) msg.getContent();
                email.setText(t.getBodyPart(0).getContent().toString());
            } else {
                email.setText(msg.getContent().toString());
            }

            String[] froms = msg.getHeader("From");
            if (froms != null && froms.length > 0) {
                email.setOriginalFrom(froms[0]);
            }

            email.setSubject(msg.getSubject());

            return email;
        }

        @Override
        public boolean accept(String from, String recipient) {
            boolean ok = recipient.toLowerCase().endsWith("my.gvmax.com");
            if (!ok) {
                logger.warn("rejected, from: " + from + " to: " + recipient);
            }
            return ok;
        }
    }
}
