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

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.gvmax.common.model.Email;
import com.gvmax.common.model.Stats;
import com.gvmax.common.model.User;
import com.gvmax.common.relay.GVMaxRelay;
import com.gvmax.common.util.MiscUtils;
import com.gvmax.common.util.PhoneUtil;
import com.gvmax.data.user.UserDAO;
import com.gvmax.server.util.Notifier;

public class MissedCallHandler {
    private static final Logger logger = Logger.getLogger(MissedCallHandler.class);
    private UserDAO userDAO;
    private GVMaxRelay relay;
    private User user;
    private MC mc;

    public MissedCallHandler(UserDAO userDAO, GVMaxRelay relay) {
        this.userDAO = userDAO;
        this.relay = relay;
    }

    public void handle(Email email) {
        extractInfo(email);
        if (user == null) {
            logger.debug("Unable to retrieve user from email: " + email.getTo());
            return;
        }
        if (mc == null) {
            logger.debug("Unable to retrieve mc from email");
            return;
        }
        if (!user.isMonitorMC()) {
            return;
        }
        userDAO.incrementMCInCount(user.getEmail());
        Notifier notifier = new Notifier(user, userDAO, relay);

        // Send notifications
        if (user.isSendProwl()) {
            try {
                notifier.sendProwlNotification("GV MC", mc.contact, "Missed call", user.getProwlSMSPriority());
            } catch (IOException e) {
                MiscUtils.emptyBlock();
            }
        }
        if (user.isSendHowl()) {
            try {
                notifier.sendHowlNotification("GV SMS", mc.contact, "Missed call");
            } catch (IOException e) {
                MiscUtils.emptyBlock();
            }
        }
        if (user.isSendEmail()) {
            notifier.sendEmailNotification(mc.number, "Missed call from " + mc.contact, "");
        }
        if (user.isSendSMS()) {
            notifier.sendSMSNotification(mc.number, "Missed call");
        }
        if (user.isSendPost()) {
            notifier.sendPostNotification("MC", mc.number, mc.contact, "", null);
        }
        if (user.isSendTwitter()) {
            notifier.sendTwitterNotification(" Missed call from " + mc.contact);
        }
        if (user.isSendAutoResponse()) {
            notifier.sendAutoResponse(mc.number);
        }
        if (user.isSendGTalk()) {
            boolean sent = notifier.sendGTalkNotification(mc.number, mc.contact, "Missed call");
            if (sent) {
                userDAO.clearFallbackCount(user.getEmail());
            }
            if (!sent && notifier.canFallback()) {
                if (!StringUtils.isBlank(user.getProwlApiKeys())) {
                    try {
                        logger.warn("Attempting to fallback to prowl");
                        notifier.sendProwlNotification("GV MC", mc.contact, "Missed call", user.getProwlMCPriority());
                        return;
                    } catch (IOException e) {
                        MiscUtils.emptyBlock();
                    }
                }
                if (!StringUtils.isBlank(user.getHowlUsername())) {
                    try {
                        logger.warn("Attempting to fallback to howl");
                        notifier.sendHowlNotification("GV MC", mc.contact, "Miseed call");
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
                    String emailText = "\n\nYou are receiving this email because GVMax was unable to send you a GTalk notification.";
                    emailText += "\nPlease ensure you are logged into GTalk";
                    notifier.sendEmailNotification(mc.number, "Missed Call from " + mc.contact, emailText);
                }
            }
        }
    }

    private void extractInfo(Email email) {
        String pin = email.getTo().substring(0, email.getTo().indexOf("@"));
        user = userDAO.retrieveByPin(pin);
        if (user == null) {
            return;
        }
        mc = new MC();
        String from = email.getSubject().split("call from ", 2)[1];
        from = from.split(" at")[0];
        from = from.trim();
        if (PhoneUtil.isNumber(from)) {
            mc.number = PhoneUtil.normalizeNumber(from);
            mc.contact = mc.number;
        } else {
            mc.contact = from;
            from = email.getText().split(mc.contact, 2)[1];
            from = from.split(" at")[0];
            from = from.trim();
            if (PhoneUtil.isNumber(from)) {
                mc.number = PhoneUtil.normalizeNumber(from);
            } else {
                mc.number = "unknown";
                mc.contact = "unknown";
            }
        }

    }

    static class MC {
        private String number;
        private String contact;
    }

}
