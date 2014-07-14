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
package com.gvmax.server.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import com.gvmax.common.model.User;
import com.gvmax.common.relay.GVMaxRelay;
import com.gvmax.common.util.MiscUtils;
import com.gvmax.common.util.NetUtil;
import com.gvmax.common.util.PhoneUtil;
import com.gvmax.common.util.growl.Howl;
import com.gvmax.common.util.growl.Prowl;
import com.gvmax.data.user.UserDAO;
import com.gvmax.google.talk.GTalk;
import com.gvmax.google.voice.GoogleVoice;

public class Notifier {
    private static final Logger logger = Logger.getLogger(Notifier.class);
    private UserDAO userDAO;
    private User user;
    private GVMaxRelay relay;
    private Twitter twitter = new TwitterFactory().getInstance("my_gv_max", "triostrios");

    public Notifier(User user, UserDAO userDAO, GVMaxRelay relay) {
        this.user = user;
        this.userDAO = userDAO;
        this.relay = relay;
    }

    public void sendProwlNotification(String application, String event, String description, int priority) throws IOException {
        Prowl.notify(user.getProwlApiKeys(), application, event, description, priority);
    }

    public void sendHowlNotification(String application, String title, String description) throws IOException {
        Howl.notify(user.getHowlUsername(), user.getHowlPassword(), application, title, description, "www.gvmax.com", "msn");
    }

    public void sendEmailNotification(String number, String subject, String text) {
        String[] emails = user.getEmailAddresses().split(",");
        String replyEmail = user.getPin() + "-" + number + "@" + relay.getEmailHost();
        for (String email : emails) {
            try {
                relay.sendEmail(relay.getEmailSender(), replyEmail, email, subject, text);
            } catch (IOException e) {
                logger.warn("unable to send email: " + e.getMessage());
            }
        }
    }

    public int sendSMSNotification(String number, String text) {
        GoogleVoice gv = new GoogleVoice(user.getEmail(), user.getPassword());
        if (PhoneUtil.isNumber(user.getSmsGroup())) {
            try {
                if (!PhoneUtil.numbersMatch(number, user.getGvPhone()) && !PhoneUtil.numbersMatch(number, user.getSmsGroup())) {
                    gv.sendSMS(user.getSmsGroup(), text);
                    userDAO.incrementSMSOutCount(user.getEmail(), 1);
                    return 1;
                }
            } catch (IOException e) {
                userDAO.incrementErrorCount(user.getEmail());
                logger.warn("unable to send SMS.");
                return 0;
            }
        }
        Map<String, String> contacts = ContactUtil.getNumbersInGroup(user, user.getSmsGroup());
        if (contacts == null) {
            logger.warn("unable to find group for sms notification");
            return 0;
        }
        List<String> sendTo = new ArrayList<String>();
        for (String contactNumber : contacts.values()) {
            if (!PhoneUtil.numbersMatch(contactNumber, number) && !PhoneUtil.numbersMatch(contactNumber, user.getGvPhone())) {
                sendTo.add(contactNumber);
            }
        }
        try {
            gv.sendSMS(sendTo.toArray(new String[0]), text);
            userDAO.incrementSMSOutCount(user.getEmail(), sendTo.size());
            return sendTo.size();
        } catch (IOException e) {
            userDAO.incrementErrorCount(user.getEmail());
            logger.warn("unable to send SMS.");
            return 0;
        }

    }

    public int sendSMS(String contact, String text) throws IOException {
        GoogleVoice gv = new GoogleVoice(user.getEmail(), user.getPassword());
        if (PhoneUtil.isNumber(contact)) {
            gv.sendSMS(contact, text);
            userDAO.incrementSMSOutCount(user.getEmail(), 1);
            return 1;
        }
        Map<String, String> contacts = ContactUtil.getNumbersInGroup(user, contact);
        if (contacts == null) {
            return 0;
        }
        List<String> sendTo = new ArrayList<String>();
        for (String number : contacts.values()) {
            sendTo.add(number);
        }
        gv.sendSMS(sendTo.toArray(new String[0]), text);
        userDAO.incrementSMSOutCount(user.getEmail(), sendTo.size());
        return sendTo.size();
    }

    public void sendPostNotification(String type, String number, String contact, String text, String link) {
        String[] urls = user.getPostURLs().split(",");
        for (String url : urls) {
            try {
                Map<String, String> params = new HashMap<String, String>();
                params.put("type", type);
                params.put("number", number);
                params.put("contact", contact);
                params.put("text", text);
                if (link != null) {
                    params.put("link", link);
                }
                params.put("receiver", user.getEmail());
                NetUtil.doPost(url, params);
            } catch (IOException e) {
                logger.warn("unable to send Post : " + e.getMessage());
            }
        }
    }

    public void sendTwitterNotification(String tweet) {
        try {
            if (tweet.length() > 140) {
                tweet = tweet.substring(0, 140);
            }
            twitter.sendDirectMessage(user.getTwitterScreenName(), tweet);
        } catch (Exception e) {
            MiscUtils.emptyBlock();
        }
    }

    public void sendAutoResponse(String number) {
        if (!PhoneUtil.numbersMatch(user.getGvPhone(), number)) {
            GoogleVoice gv = new GoogleVoice(user.getEmail(), user.getPassword());
            try {
                gv.sendSMS(number, user.getAutoResponse());
                userDAO.incrementSMSOutCount(user.getEmail(), 1);
            } catch (Exception e) {
                userDAO.incrementErrorCount(user.getEmail());
            }
        }
    }

    public boolean sendGTalkNotification(String number, String nickname, String text) {
        String buddy = number + "@" + relay.getAppName() + ".appspotchat.com";
        try {
            GTalk.addBuddy(user.getgTalkEmail(), user.getgTalkPassword(), buddy, nickname, user.getgTalkGroup());
            boolean sent = relay.sendXmppMessage(buddy, user.getgTalkEmail(), text);
            userDAO.incrementGTalkCount(user.getEmail());
            return sent;
        } catch (IOException e) {
            logger.warn("unable to send gtalk notification: " + e.getMessage());
            return false;
        }
    }

    public boolean canFallback() {
        return !user.isSendProwl() && !user.isSendEmail() && !user.isSendHowl() && (user.getProwlApiKeys() != null || user.getEmailAddresses() != null || user.getHowlUsername() != null);
    }

}
