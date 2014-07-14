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
package com.gvmax.server.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.gvmax.common.model.Email;
import com.gvmax.common.model.Stats;
import com.gvmax.common.model.User;
import com.gvmax.common.relay.GVMaxRelay;
import com.gvmax.common.util.MiscUtils;
import com.gvmax.common.util.PhoneUtil;
import com.gvmax.data.user.UserDAO;
import com.gvmax.server.util.ContactUtil;
import com.gvmax.server.util.Notifier;

public class SMSInHandler {
    private static final Logger logger = Logger.getLogger(SMSInHandler.class);
    private UserDAO userDAO;
    private GVMaxRelay relay;

    // Extracted Info
    private User user;
    private SMS sms;

    public SMSInHandler(UserDAO userDAO, GVMaxRelay relay) {
        this.userDAO = userDAO;
        this.relay = relay;
    }

    public void handle(Email email) {
        extractInfo(email);
        if (user == null) {
            logger.debug("Unable to retrieve user from email: " + email.getTo());
            return;
        }
        if (sms == null) {
            logger.debug("Unable to retrieve sms from email");
            return;
        }
        if (!user.isMonitorSMS()) {
            return;
        }
        userDAO.incrementSMSInCount(user.getEmail());
        Notifier notifier = new Notifier(user, userDAO, relay);

        // Send notifications
        if (user.isSendProwl()) {
            try {
                notifier.sendProwlNotification("GV SMS", sms.contact, sms.text, user.getProwlSMSPriority());
            } catch (IOException e) {
                MiscUtils.emptyBlock();
            }
        }
        if (user.isSendHowl()) {
            try {
                notifier.sendHowlNotification("GV SMS", sms.contact, sms.text);
            } catch (IOException e) {
                MiscUtils.emptyBlock();
            }
        }
        if (user.isSendEmail()) {
            notifier.sendEmailNotification(sms.number, "New SMS from " + sms.contact, sms.text);
        }
        if (user.isSendSMS()) {
            notifier.sendSMSNotification(sms.number, sms.text);
        }
        if (user.isSendPost()) {
            notifier.sendPostNotification("SMS", sms.number, sms.contact, sms.text, null);
        }
        if (user.isSendTwitter()) {
            notifier.sendTwitterNotification(" SMS from " + sms.contact + " - " + sms.text);
        }
        if (user.isSendAutoResponse()) {
            notifier.sendAutoResponse(sms.number);
        }
        if (user.isSendGTalk()) {
            boolean sent = notifier.sendGTalkNotification(sms.number, sms.contact, sms.text);
            if (sent) {
                userDAO.clearFallbackCount(user.getEmail());
            }
            if (!sent && notifier.canFallback()) {
                if (!StringUtils.isBlank(user.getProwlApiKeys())) {
                    try {
                        logger.warn("Attempting to fallback to prowl");
                        notifier.sendProwlNotification("GV SMS", sms.contact, sms.text, user.getProwlSMSPriority());
                        return;
                    } catch (IOException e) {
                        MiscUtils.emptyBlock();
                    }
                }
                if (!StringUtils.isBlank(user.getHowlUsername())) {
                    try {
                        logger.warn("Attempting to fallback to howl");
                        notifier.sendHowlNotification("GV SMS", sms.contact, sms.text);
                        return;
                    } catch (IOException e) {
                        MiscUtils.emptyBlock();
                    }
                }
                Stats stats = userDAO.getStats(user.getEmail());
                if (stats == null) {
                    logger.error("unable to find stats for user: "+user.getEmail());
                    return;
                }
                logger.warn("Attempting to fallback to email: fbc=" + stats.getFallbackCount());
                userDAO.incrementFallbackCount(user.getEmail());
                if (stats.getFallbackCount() < 10) {
                    String msg = sms.text;
                    msg += "\n\nYou are receiving this email because GVMax was unable to send you a GTalk notification.";
                    msg += "\nPlease ensure you are logged into GTalk";
                    notifier.sendEmailNotification(sms.number, "SMS from " + sms.contact, msg);
                }
            }
        }
    }

    // ----------------
    // EXTRACT INFO
    // ----------------

    static class SMS {
        private String number;
        private String contact;
        private String text;
    }

    private void extractInfo(Email email) {
        String pin = email.getTo().substring(0, email.getTo().indexOf("@"));
        user = userDAO.retrieveByPin(pin);
        if (user == null) {
            return;
        }
        extractSMS(email);
    }

    private void extractSMS(Email email) {
        sms = new SMS();
        // Number
        sms.number = extractSMSNumber(email.getSubject());
        // Contact
        sms.contact = extractSMSContact(user, email.getSubject(), sms.number);
        // Text
        StringBuffer text = new StringBuffer("");
        // Extract smsText (note special support for beejive media links)
        try {
            BufferedReader reader = new BufferedReader(new StringReader(email.getText()));
            String line = reader.readLine();
            while (line != null) {
                // Special beejive tweek to deal with media content
                if (line.contains("beejive.com")) {
                    line += "  ";
                } else {
                    line = line.trim();
                }
                if (line.equals("--")) {
                    break;
                }
                if (!"".equals(line)) {
                    text.append(line);
                    text.append("\n");
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            sms = null;
            return;
        }
        sms.text = text.toString();
        if (!"".equals(sms.text)) {
            sms.text = sms.text.substring(0, sms.text.length() - 1);
        }
        return;
    }

    private static String extractSMSNumber(String subject) {
        String fromNumber = subject.substring("SMS from ".length());
        int bPos = fromNumber.indexOf('[');
        if (bPos != -1) {
            fromNumber = fromNumber.substring(bPos + 1, fromNumber.length() - 1);
        }
        return PhoneUtil.normalizeNumber(fromNumber);
    }

    private static String extractSMSContact(User user, String subject, String number) {
        String contact = ContactUtil.getContact(user, number);
        if (contact == null) {
            contact = extractSMSContactFromSubject(subject);
        }
        if (contact == null) {
            contact = number;
        }
        return contact;
    }

    private static String extractSMSContactFromSubject(String subject) {
        String from = subject.substring("SMS from ".length());
        int bPos = from.indexOf('[');
        if (bPos != -1) {
            return from.substring(0, bPos).trim();
        }
        return null;
    }

}
