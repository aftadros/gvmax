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
import com.gvmax.common.util.TinyURL;
import com.gvmax.data.user.UserDAO;
import com.gvmax.server.util.ContactUtil;
import com.gvmax.server.util.Notifier;

public class VoicemailHandler {
    private static final Logger logger = Logger.getLogger(VoicemailHandler.class);
    private UserDAO userDAO;
    private GVMaxRelay relay;

    private User user;
    private Voicemail vm;

    public VoicemailHandler(UserDAO userDAO, GVMaxRelay relay) {
        this.userDAO = userDAO;
        this.relay = relay;
    }

    public void handle(Email email) {
        extractInfo(email);
        if (user == null) {
            logger.debug("Unable to retrieve user from email: " + email.getTo());
            return;
        }
        if (vm == null) {
            logger.debug("Unable to retrieve voicemail from email");
            return;
        }
        if (!user.isMonitorVM()) {
            return;
        }
        userDAO.incrementVMInCount(user.getEmail());
        Notifier notifier = new Notifier(user, userDAO, relay);

        // Send notifications
        if (user.isSendProwl()) {
            String text = "Transcript: " + vm.transcript + "\nLink: " + vm.link;
            try {
                notifier.sendProwlNotification("GV VM", vm.contact, text, user.getProwlVMPriority());
            } catch (IOException e) {
                MiscUtils.emptyBlock();
            }
        }
        if (user.isSendHowl()) {
            String text = "Transcript: " + vm.transcript + "\nLink: " + vm.link;
            try {
                notifier.sendHowlNotification("GV VM", vm.contact, text);
            } catch (IOException e) {
                MiscUtils.emptyBlock();
            }
        }
        if (user.isSendEmail()) {
            String text = vm.transcript + "\nLink: " + vm.link;
            notifier.sendEmailNotification(vm.number, "New Voicemail from " + vm.contact, text);
        }
        if (user.isSendSMS()) {
            notifier.sendSMSNotification(vm.number, "vm:" + vm.transcript);
        }
        if (user.isSendPost()) {
            notifier.sendPostNotification("VM", vm.number, vm.contact, vm.transcript, vm.link);
        }
        if (user.isSendTwitter()) {
            notifier.sendTwitterNotification("VM from " + vm.contact + " - " + vm.transcript + " : " + vm.link);
        }
        if (user.isSendAutoResponse()) {
            notifier.sendAutoResponse(vm.number);
        }
        if (user.isSendGTalk()) {
            boolean sent = notifier.sendGTalkNotification(vm.number, vm.contact, "Transcript: " + vm.transcript + "\nLink: " + vm.link);
            if (sent) {
                userDAO.clearFallbackCount(user.getEmail());
            }
            if (!sent && notifier.canFallback()) {
                if (!StringUtils.isBlank(user.getProwlApiKeys())) {
                    String text = "Transcript: " + vm.transcript + "\nLink: " + vm.link;
                    try {
                        logger.warn("Attempting to fallback to prowl");
                        notifier.sendProwlNotification("GV VM", vm.contact, text, user.getProwlVMPriority());
                        return;
                    } catch (IOException e) {
                        MiscUtils.emptyBlock();
                    }
                }
                if (!StringUtils.isBlank(user.getHowlUsername())) {
                    String text = "Transcript: " + vm.transcript + "\nLink: " + vm.link;
                    try {
                        logger.warn("Attempting to fallback to howl");
                        notifier.sendHowlNotification("GV VM", vm.contact, text);
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
                    String emailText = vm.transcript + "\nLink: " + vm.link;
                    emailText += "\n\nYou are receiving this email because GVMax was unable to send you a GTalk notification.";
                    emailText += "\nPlease ensure you are logged into GTalk";
                    notifier.sendEmailNotification(vm.number, "Voicemail from " + vm.contact, emailText);
                }
            }
        }
    }

    // -------------------
    // EXTRACT INFO
    // -------------------

    static class Voicemail {
        private String number;
        private String contact;
        private String transcript;
        private String link;
    }

    private void extractInfo(Email email) {
        String pin = email.getTo().substring(0, email.getTo().indexOf('@'));
        user = userDAO.retrieveByPin(pin);
        if (user == null) {
            return;
        }
        try {
            extractVoicemail(email);
        } catch (Exception e) {
            vm = null;
        }
    }

    private void extractVoicemail(Email email) throws IOException {
        parseEmail(email.getText());
        if (vm == null || vm.number == null) {
            vm = null;
            return;
        }
    }

    private void parseEmail(String text) throws IOException {
        vm = new Voicemail();
        boolean capturingText = false;
        BufferedReader reader = new BufferedReader(new StringReader(text));

        String line = reader.readLine();
        while (line != null) {
            line = line.trim();

            if (line.startsWith("You've got new voicemail from")) {
                int bPos = line.indexOf('(');
                int dPos = line.indexOf('-');
                if (bPos == "You've got new voicemail from  ".length()) {
                    vm.number = line.substring(bPos);
                } else {
                    dPos = line.lastIndexOf('-');
                    vm.number = line.substring(bPos, dPos);
                }
                vm.number = PhoneUtil.normalizeNumber(vm.number.trim());
            }
            if (line.startsWith("Voicemail from")) {
                // Extract number
                int bPos = line.indexOf('(');
                if (bPos == -1) {
                    vm.number = "unknown";
                } else {
                    String number = line.substring(bPos);
                    number = number.substring(0, number.indexOf(" at "));
                    vm.number = PhoneUtil.normalizeNumber(number);
                }
            }
            if (line.startsWith("Play message:")) {
                // Grab link
                capturingText = false;
                vm.link = reader.readLine();
            }
            if (capturingText) {
                // Capturing transcript 2
                vm.transcript = vm.transcript + line + "\n";
            }
            if (line.startsWith("Transcript:")) {
                // Capturing transcript 1
                vm.transcript = line.substring("Transcript:".length()) + "\n";
                capturingText = true;
            }
            line = reader.readLine();
        }

        if (vm.transcript != null && !"".equals(vm.transcript)) {
            vm.transcript = vm.transcript.substring(0, vm.transcript.length() - 1);
        }

        if (!"unknown".equals(vm.number)) {
            vm.contact = ContactUtil.getContact(user, vm.number);
        }
        if (vm.contact == null) {
            vm.contact = vm.number;
        }

        try {
            vm.link = TinyURL.getTinyUrl(vm.link);
        } catch (IOException e) {
            logger.warn("Unable to create short link");
        }

    }

}
