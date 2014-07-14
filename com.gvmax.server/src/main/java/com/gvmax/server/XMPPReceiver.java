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
package com.gvmax.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.codahale.metrics.Timer.Context;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.PhoneNumber;
import com.gvmax.common.model.User;
import com.gvmax.common.model.XMPPAction;
import com.gvmax.common.relay.GVMaxRelay;
import com.gvmax.common.util.EmailUtils;
import com.gvmax.common.util.MetricsUtil;
import com.gvmax.common.util.MiscUtils;
import com.gvmax.common.util.PhoneUtil;
import com.gvmax.common.util.TimeTrack;
import com.gvmax.data.queue.QueueListener.QueueProcessor;
import com.gvmax.data.user.UserDAO;
import com.gvmax.google.talk.GTalk;
import com.gvmax.google.voice.GoogleVoice;
import com.gvmax.google.voice.GoogleVoicePhone;
import com.gvmax.server.util.ContactUtil;
import com.gvmax.server.util.Notifier;

public class XMPPReceiver implements QueueProcessor<XMPPAction> {
    private static final Logger logger = Logger.getLogger(XMPPReceiver.class);
    private UserDAO userDAO;
    private GVMaxRelay relay;

    public XMPPReceiver(UserDAO userDAO, GVMaxRelay relay) {
        this.userDAO = userDAO;
        this.relay = relay;
    }

    @Override
    public void process(XMPPAction action) {
        MetricsUtil.getCounter(XMPPReceiver.class, "received").inc();
        Context timer = MetricsUtil.getTimer(XMPPReceiver.class, "processTime").time();
        TimeTrack tt = new TimeTrack();
        try {
            tt.mark("received.time", action.getTimestamp());
            tt.mark("process.time");
            User user = extractInfo(action);
            XMPPProcessor processor = new XMPPProcessor(user, action);
            if (user == null) {
                MetricsUtil.getCounter(XMPPReceiver.class, "unregistered").inc();
                processor.sendMessage("You are not a registered gvmax user. Action cancelled");
                return;
            }
            userDAO.incrementGTalkCount(user.getEmail());
            if (!processor.processCommands()) {
                if (!user.isGvPassword()) {
                    processor.sendMessage("google voice password required to send SMS replies");
                    return;
                }
                if ("unknown".equals(action.getBotNumber())) {
                    processor.sendMessage("cannot send sms to unknown number");
                    return;
                }
                try {
                    Notifier notifier = new Notifier(user, userDAO, null);
                    int num = notifier.sendSMS(action.getBotNumber(), action.getMessage());
                    if (num > 1) {
                        processor.sendMessage("Sending SMS to " + num + " numbers");
                    }
                } catch (Exception e) {
                    processor.sendMessage("Failed to send SMS error = " + e.getMessage());
                }
            }
        } finally {
            timer.stop();
            tt.mark("end.time");
            logger.info("XMPP_IN ProcessTime: " + tt.deltaInSecs() + ", total: " + tt.elapsedInSecs());
        }
    }

    private User extractInfo(XMPPAction action) {
        String[] to = action.getTo();
        String botJId = to[0].substring(6, to[0].indexOf('>'));
        String botNumber = botJId.substring(0, botJId.indexOf('@'));
        String senderJId = action.getFrom().substring(6, action.getFrom().indexOf('>'));
        String gtalkEmail = EmailUtils.formatEmail(action.getFrom().substring(6, action.getFrom().indexOf('/')));
        action.setBotJId(botJId);
        action.setBotNumber(botNumber);
        action.setSenderJId(senderJId);
        return userDAO.retrieveByGTalk(gtalkEmail);
    }

    class XMPPProcessor {
        private User user;
        private XMPPAction action;

        public XMPPProcessor(User user, XMPPAction action) {
            this.user = user;
            this.action = action;
        }

        private boolean processCommands() {
            String cmd = action.getMessage().trim().toLowerCase();
            if (COMMAND_HELP.equals(cmd)) {
                help();
                return true;
            }
            if (COMMAND_INFO.equals(cmd)) {
                info();
                return true;
            }
            if (cmd.startsWith(COMMAND_ADDNUMBER)) {
                return addNumber();
            }
            if (COMMAND_PHONES.equals(cmd)) {
                displayPhones();
                return true;
            }
            if (cmd.startsWith(COMMAND_PHONES)) {
                return setFwdPhone();
            }
            if (COMMAND_CALL.equals(cmd)) {
                callNumber(action.getBotNumber());
                return true;
            }
            if (cmd.startsWith(COMMAND_CALL)) {
                String[] args = cmd.split(" ");
                if (args.length > 1) {
                    callNumber(restOf(args));
                    return true;
                }
            }
            if (COMMAND_GROUPS.equals(cmd)) {
                showGroups();
                return true;
            }
            if (cmd.startsWith(COMMAND_GROUPLIST)) {
                String[] args = cmd.split(" ");
                if (args.length == 2) {
                    try {
                        int index = Integer.parseInt(args[1]);
                        listGroup(index);
                        return true;
                    } catch (Exception e) {
                        MiscUtils.emptyBlock();
                    }
                }
            }
            if (cmd.startsWith(COMMAND_GROUPS)) {
                String[] args = cmd.split(" ");
                if (args.length == 2) {
                    try {
                        int index = Integer.parseInt(args[1]);
                        addGroup(index);
                        return true;
                    } catch (Exception e) {
                        MiscUtils.emptyBlock();
                    }
                }
            }
            return false;
        }

        // ---------------------
        // COMMAND METHODS
        // ---------------------

        private void help() {
            StringBuffer msg = new StringBuffer("HELP\n");
            msg.append("gg - Displays this help text.\n");
            msg.append("ggi - Displays information about buddy.\n");
            msg.append("gga {number} - Adds a new phone number to your IM roster.\n");
            msg.append("ggp - Displays list of available fowarding phones.\n");
            msg.append("ggp {index} - Selects new fowarding number.\n");
            msg.append("ggc - Calls buddy via GoogleVoice.\n");
            msg.append("ggc {number} - Calls a number via GoogleVoice.\n");
            msg.append("ggr - Show list of groups.\n");
            msg.append("ggr {index} - Add group to IM roster.\n");
            msg.append("ggrl {index} - List group members.\n");
            sendMessage(msg.toString());
        }

        private void info() {
            sendMessage("retrieving info for " + action.getBotNumber() + "...");
            ContactEntry entry = ContactUtil.getContactEntry(user, action.getBotNumber());
            StringBuffer msg = new StringBuffer();
            if (entry != null) {
                if (entry.hasName()) {
                    Name name = entry.getName();
                    if (name.hasFullName()) {
                        String fullNameToDisplay = name.getFullName().getValue();
                        if (name.getFullName().hasYomi()) {
                            fullNameToDisplay += " (" + name.getFullName().getYomi() + ")";
                        }
                        msg.append("Name: " + fullNameToDisplay + "\n");
                    }
                }
                if (entry.getEmailAddresses().size() > 0) {
                    msg.append("Email Addresses:\n");
                    for (Email email : entry.getEmailAddresses()) {
                        msg.append("    " + email.getAddress() + "\n");
                    }
                }
                if (entry.getPhoneNumbers().size() > 0) {
                    msg.append("Phone Numbers:\n");
                    for (PhoneNumber number : entry.getPhoneNumbers()) {
                        msg.append("    ");
                        msg.append(PhoneUtil.normalizeNumber(number.getPhoneNumber()));
                        if (number.getLabel() != null) {
                            msg.append(" [" + number.getLabel().substring(number.getLabel().lastIndexOf('#') + 1) + "]");
                        }
                        if (number.getRel() != null) {
                            msg.append(" [" + number.getRel().substring(number.getRel().lastIndexOf('#') + 1) + "]");
                        }
                        msg.append("\n");
                    }
                }
            } else {
                msg.append("Contact information for " + action.getBotNumber() + " could not retrieved.");
            }
            sendMessage(msg.toString());
        }

        private boolean addNumber() {
            String[] info = action.getMessage().split(" ");
            if (info.length <= 1) {
                return false;
            }
            String number = PhoneUtil.normalizeNumber(restOf(info));
            if (!PhoneUtil.isNumber(number)) {
                return false;
            }
            sendMessage("Adding buddy " + number);
            String nickname = ContactUtil.getContact(user, number);
            if (nickname == null) {
                nickname = number;
            }
            try {
                GTalk.addBuddy(user.getgTalkEmail(), user.getgTalkPassword(), number + "@" + relay.getAppName() + ".appspotchat.com", nickname, user.getgTalkGroup());
            } catch (IOException e) {
                sendMessage("Unable to add number " + number + " : " + e.getMessage());
            }
            sendMessage("Added " + number + " as " + nickname);
            return true;
        }

        private void displayPhones() {
            if (!user.isGvPassword()) {
                sendMessage("google voice password required for this functionality");
                return;
            }
            sendMessage("retrieving available forward phones...");
            StringBuffer msg = new StringBuffer();
            try {
                GoogleVoice gv = new GoogleVoice(user.getEmail(), user.getPassword());
                List<GoogleVoicePhone> phones = gv.getPhones();
                if (phones.size() == 0) {
                    msg.append("No phones have been registered with your GoogleVoice account");
                } else {
                    int pn = 1;
                    for (GoogleVoicePhone phone : phones) {
                        if (PhoneUtil.numbersMatch(phone.getNumber(), user.getGvFwdPhone())) {
                            msg.append(pn + ". ** " + phone.getName() + "  " + PhoneUtil.normalizeNumber(phone.getNumber()) + " **\n");
                        } else {
                            msg.append(pn + ". " + phone.getName() + "  " + PhoneUtil.normalizeNumber(phone.getNumber()) + "\n");
                        }
                        pn += 1;
                    }
                }
                sendMessage(msg.toString());
            } catch (Exception e) {
                sendMessage("Unable to retrieve phones : " + e.getMessage());
            }
        }

        private boolean setFwdPhone() {
            if (!user.isGvPassword()) {
                sendMessage("google voice password required for this functionality");
                return true;
            }

            String[] info = action.getMessage().split(" ");
            if (info.length != 2) {
                return false;
            }
            int index = -1;
            try {
                index = Integer.parseInt(info[1]);
            } catch (Exception e) {
                return false;
            }

            try {
                GoogleVoice gv = new GoogleVoice(user.getEmail(), user.getPassword());
                List<GoogleVoicePhone> phones = gv.getPhones();
                if (phones.size() == 0) {
                    sendMessage("No phones have been registered with your GoogleVoice account");
                } else {
                    if (index <= 0) {
                        sendMessage("Invalid phone index '" + index + "' please select an index from 1 to " + phones.size());
                        return true;
                    }
                    if (phones.size() < index) {
                        sendMessage("invalid phone index '" + index + "' please select an index from 1 to " + phones.size());
                        displayPhones();
                        return true;
                    }
                    GoogleVoicePhone phone = phones.get(index - 1);
                    // HANI new API
                    userDAO.setGVFwdPhone(user.getEmail(), phone.getNumber(), phone.getType());
                    sendMessage("Foward phone set to : " + phone.getName());
                }
            } catch (Exception e) {
                sendMessage("Unable to select phone: " + e.getMessage());
            }
            return true;
        }

        private void callNumber(String number) {
            if (!user.isGvPassword()) {
                sendMessage("google voice password required for this functionality");
                return;
            }
            if (user.getGvFwdPhone() == null) {
                sendMessage("forwarding phone not selected, please select phone via ggp command");
                return;
            }
            number = PhoneUtil.normalizeNumber(number);
            if (!PhoneUtil.isNumber(number)) {
                sendMessage(number + " is not valid");
                return;
            }

            GoogleVoice gv = new GoogleVoice(user.getEmail(), user.getPassword());
            sendMessage("Placing call to " + number + ", " + user.getGvFwdPhone() + " set as fowarding phone");
            try {
                gv.call(number, user.getGvFwdPhone());
            } catch (IOException e) {
                sendMessage("Call failed : " + e.getMessage());
            }

        }

        private void showGroups() {
            sendMessage("retrieving list of groups");
            try {
                List<String> groups = ContactUtil.getGroups(user);
                StringBuffer msg = new StringBuffer("GROUPS\n");
                int index = 1;
                for (String group : groups) {
                    msg.append("  " + index + ". " + group + "\n");
                    index += 1;
                }
                if (groups.size() > 0) {
                    sendMessage(msg.toString());
                } else {
                    sendMessage("no GVMax compatible group found");
                }
            } catch (IOException e) {
                sendMessage("Unable to retrieve groups : " + e.getMessage());
            }

        }

        private void addGroup(int index) {
            try {
                sendMessage("adding group...");
                List<String> groups = ContactUtil.getGroups(user);
                if (index <= 0) {
                    sendMessage("Invalid group index '" + index + "' please select an index from 1 to " + groups.size());
                    showGroups();
                    return;
                }
                if (groups.size() < index) {
                    sendMessage("invalid group index '" + index + "' please select an index from 1 to " + groups.size());
                    showGroups();
                    return;
                }
                String group = groups.get(index - 1);
                String g = group;
                g = g.replaceAll(" ", "_");
                g = g.replaceAll(":", ".");
                GTalk.addBuddy(user.getgTalkEmail(), user.getgTalkPassword(), g + "@" + relay.getAppName() + ".appspotchat.com", group, user.getgTalkGroup());
                sendMessage("group " + group + " added");
            } catch (IOException e) {
                sendMessage("unable to add group : " + e.getMessage());
            }
        }

        private void listGroup(int index) {
            sendMessage("retrieving group members");
            try {
                List<String> groups = ContactUtil.getGroups(user);
                if (index <= 0) {
                    sendMessage("Invalid group index '" + index + "' please select an index from 1 to " + groups.size());
                    showGroups();
                    return;
                }
                if (groups.size() < index) {
                    sendMessage("invalid group index '" + index + "' please select an index from 1 to " + groups.size());
                    showGroups();
                    return;
                }
                String group = groups.get(index - 1);
                Map<String, String> contacts = ContactUtil.getNumbersInGroup(user, group);
                if (contacts.size() == 0) {
                    sendMessage("group " + group + " has no members");
                    return;
                }
                StringBuffer msg = new StringBuffer("Contacts for " + group + "\n");
                for (Map.Entry<String, String> contact : contacts.entrySet()) {
                    msg.append(contact.getKey() + " : " + contact.getValue() + "\n");
                }
                msg.append("Total : ");
                msg.append(contacts.size());
                sendMessage(msg.toString());

            } catch (IOException e) {
                sendMessage("Unable to get group contacts: " + e.getMessage());
            }

        }

        // --------------------------
        // UTIL
        // --------------------------

        String restOf(String[] args) {
            StringBuffer retVal = new StringBuffer(args[1]);
            for (int x = 2; x < args.length; x++) {
                retVal.append(" ");
                retVal.append(args[x]);
            }
            return retVal.toString();
        }

        private void sendMessage(String message) {
            try {
                relay.sendXmppMessage(action.getBotJId(), action.getSenderJId(), message);
                if (user != null) {
                    userDAO.incrementGTalkCount(user.getEmail());
                }
            } catch (IOException e) {
                MiscUtils.emptyBlock();
            }
        }

    }

    // -----------------
    // COMMANDS
    // -----------------

    private static final String COMMAND_HELP = "gg";
    private static final String COMMAND_INFO = "ggi";
    private static final String COMMAND_ADDNUMBER = "gga";
    private static final String COMMAND_PHONES = "ggp";
    private static final String COMMAND_CALL = "ggc";
    private static final String COMMAND_GROUPS = "ggr";
    private static final String COMMAND_GROUPLIST = "ggrl";

}
