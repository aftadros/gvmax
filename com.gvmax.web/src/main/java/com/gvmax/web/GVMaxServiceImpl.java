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
package com.gvmax.web;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gvmax.common.model.APIAction;
import com.gvmax.common.model.APIAction.ACTIONS;
import com.gvmax.common.model.GlobalStats;
import com.gvmax.common.model.Stats;
import com.gvmax.common.model.User;
import com.gvmax.common.model.XMPPAction;
import com.gvmax.common.relay.GVMaxRelay;
import com.gvmax.common.util.EmailUtils;
import com.gvmax.common.util.MiscUtils;
import com.gvmax.common.util.PhoneUtil;
import com.gvmax.common.util.growl.Prowl;
import com.gvmax.data.queue.QueueDAO;
import com.gvmax.data.user.UserDAO;
import com.gvmax.google.contacts.GoogleContacts;
import com.gvmax.google.talk.GTalk;
import com.gvmax.google.voice.GoogleVoice;

@Service
public class GVMaxServiceImpl {
    private UserDAO userDAO;
    private GVMaxRelay relay;
    private String adminAccount;
    private QueueDAO<APIAction> apiQueue;
    private QueueDAO<XMPPAction> xmppQueue;
    private AtomicLong apiCount = new AtomicLong(System.currentTimeMillis());

    // private Twitter tw = new TwitterFactory().getInstance("my_gv_max",
    // "triostrios");

    @Autowired
    public GVMaxServiceImpl(UserDAO userDAO, GVMaxRelay relay, @Value("${web.adminAccount}") String adminAccount, @Qualifier("apiQueue") QueueDAO<APIAction> apiQueue, @Qualifier("xmppQueue") QueueDAO<XMPPAction> xmppQueue) {
        this.userDAO = userDAO;
        this.relay = relay;
        this.adminAccount = adminAccount;
        this.apiQueue = apiQueue;
        this.xmppQueue = xmppQueue;
    }

    public User getUser(String email) {
        return userDAO.retrieve(email);
    }

    public User getUser(String email, String pin) {
        email = EmailUtils.normalizeEmail(email);
        if (!StringUtils.isBlank(pin)) {
            User user = userDAO.retrieveByPin(pin);
            if (user != null && user.getEmail().equals(adminAccount)) {
                if (StringUtils.isBlank(email)) {
                    return user;
                }
                return userDAO.retrieve(email);
            }
            return user;
        } else {
//        if (email != null) {
            return userDAO.retrieve(email);
//        }
        }
    }

    public List<User> getUsers(int offset, int limit) {
        return userDAO.getUsers(offset,limit);
    }

    public Stats getStats(String email) {
        return userDAO.getStats(email);
    }

    public GlobalStats getStats() {
        return userDAO.getStats();
    }

    public User login(String email, String password) throws IOException {
        email = EmailUtils.normalizeEmail(email);
        User user = userDAO.retrieve(email);
        if (user == null) {
            MiscUtils.sleep(3000);
            throw new IOException("Invalid credentials");
        }
        if (!user.isGvPassword()) {
            if (!password.equals(user.getPassword())) {
                MiscUtils.sleep(3000);
                throw new IOException("Invalid credentials");
            }
        } else {
            GoogleVoice gv = new GoogleVoice(email, password);
            try {
                gv.login();
            } catch (IOException e) {
                MiscUtils.sleep(3000);
                throw new IOException("Invalid crendentials",e);
            }
            if (!password.equals(user.getPassword())) {
                userDAO.setPassword(email, password);
            }
        }
        return user;
    }

    public RegistrationInfo signup(String email, String password, boolean isGVPassword) {
        email = EmailUtils.normalizeEmail(email);
        RegistrationInfo info = new RegistrationInfo();
        info.setGvPassword(isGVPassword);
        User user = userDAO.retrieve(email);

        // ALREADY EXISTS
        if (user != null) {
            info.setAlreadyRegistered(true);
            info.setRegistered(true);
            // Check if password correct
            try {
                checkCredentials(user, email, password);
            } catch (Exception e) {
                info.setRegistered(false);
                info.setInvalidCredentials(true);
            }
            return info;
        }

        user = new User();

        // IF GV CHECK PASSWORD
        GoogleVoice gv = new GoogleVoice(email, password);
        if (isGVPassword) {
            try {
                user.setGvPhone(gv.getPhoneNumber());
            } catch (Exception e) {
                info.setRegistered(false);
                info.setInvalidCredentials(true);
                return info;
            }
        }

        // Blacklist
        if (isBlacklisted(email, user)) {
            // userDAO.delete(user.getEmail());
            info.setRegistered(false);
            info.setBlacklisted(true);
            return info;
        }

        // CREATE ACCOUNT
        user.setEmail(email);
        Stats stats = userDAO.getStats(email);
        if (stats != null) {
            // reuse pin
            user.setPin(stats.getPin());
        } else {
            user.setPin(createPin(email));
        }
        user.setPassword(password);
        user.setGvPassword(isGVPassword);
        user.setMonitorSMS(true);
        user.setMonitorVM(true);
        user.setEmailAddresses(email);
        user.setgTalkGroup("GVMax");
        if (GTalk.validateCredentials(email, password)) {
            user.setgTalkEmail(email);
            user.setgTalkPassword(password);
            user.setSendGTalk(true);
        } else {
            user.setSendEmail(true);
        }

        // CREATE GV FILTERS
        if (isGVPassword) {
            // TODO: createFilters(email, password, user.getPin(), info);
            MiscUtils.emptyBlock();
        }

        // STORE
        user.setCreationDate(System.currentTimeMillis());
        userDAO.store(user);
        info.setRegistered(true);
        return info;

    }

    public RegistrationInfo register(User user, String googlePassword) {
        user.setEmail(EmailUtils.normalizeEmail(user.getEmail()));
        RegistrationInfo info = new RegistrationInfo();
        info.setGvPassword(user.isGvPassword());

        User existingUser = userDAO.retrieve(user.getEmail());
        if (existingUser != null) {
            info.setAlreadyRegistered(true);
        }

        // Check if gv password is ok
        GoogleVoice gv = new GoogleVoice(user.getEmail(), googlePassword);
        try {
            user.setGvPhone(gv.getPhoneNumber());
        } catch (IOException e) {
            info.setRegistered(false);
            info.setInvalidCredentials(true);
            return info;
        }

        // Blacklist
        if (isBlacklisted(user.getEmail(), user)) {
            // userDAO.delete(user.getEmail());
            info.setRegistered(false);
            info.setBlacklisted(true);
            return info;
        }

        if (existingUser != null) {
            user.setPin(existingUser.getPin());
        } else {
            user.setPin(createPin(user.getEmail()));
        }
        info.setPin(user.getPin());

        user.setCreationDate(System.currentTimeMillis());
        userDAO.store(user);
        info.setRegistered(true);
        // TODO: Try to fix filter creation
        info.setFiltersCreated(false);
//        if (googlePassword != null) {
//            try {
//                // Remove existing filters
//                GMail gm = new GMail();
//                gm.deleteFilterWithFwdEmail(user.getEmail(), googlePassword, user.getPin() + "@my.gvmax.com");
//                gm.deleteFilterWithFwdEmail(user.getEmail(), googlePassword, "gvmax@gvmax-post.appspotmail.com");
//            } catch (Exception e) {
//                // Well too bad.
//                MiscUtils.emptyBlock();
//            }
//
//            try {
//                createFilters(user.getEmail(), googlePassword, user.getPin(), info);
//            } catch (Exception e) {
//                info.setFiltersCreated(false);
//            }
//        }

        return info;
    }

    public void unregister(String email) {
        email = EmailUtils.normalizeEmail(email);
        User user = userDAO.retrieve(email);
        if (user == null) {
            return;
        }

        // DELETE FILTER
        //deleteFilters(user.getEmail(), user.getPassword(), user.getPin());

        userDAO.delete(email);

        return;
    }

    public void forgotPassword(String email) throws IOException {
        User user = userDAO.retrieve(email);
        if (user == null) {
            relay.sendEmail(relay.getEmailSender(), relay.getEmailSender(), email, "GVMax Password Retrieval", "Your email address is not registered with GVMax");
            return;
        }
        if (user.isGvPassword()) {
            relay.sendEmail(relay.getEmailSender(), relay.getEmailSender(), email, "GVMax Password Retrieval", "Your account at GVMax uses your Google Voice password, please contact Google Voice to obtain or reset your password.");
            return;
        }
        relay.sendEmail(relay.getEmailSender(), relay.getEmailSender(), email, "GVMax Password Retrieval", "Your password is: " + user.getPassword());
    }

    public void changePassword(String email, String password, boolean gvPassword) throws IOException {
        email = EmailUtils.normalizeEmail(email);
        if (StringUtils.isBlank(email)) {
            throw new IOException("invalid credentials");
        }
        if (StringUtils.isBlank(password)) {
            throw new IOException("invalid credentials");
        }
        User user = getUser(email, null);
        if (user == null) {
            throw new IOException("invalid credentials");
        }
        if (gvPassword) {
            GoogleVoice gv = new GoogleVoice(email, password);
            gv.login();
        }
        user.setPassword(password);
        user.setGvPassword(gvPassword);
        userDAO.store(user);
    }

    public void setMonitors(String email, boolean monitorSMS, boolean monitorVM, boolean monitorMC) {
        email = EmailUtils.normalizeEmail(email);
        User user = getUser(email, null);
        if (user == null) {
            return;
        }
        user.setMonitorSMS(monitorSMS);
        user.setMonitorVM(monitorVM);
        user.setMonitorMC(monitorMC);
        userDAO.store(user);
    }

    public void setNotifiers(User user) throws IOException {
        // GTalk
        if (user.isSendGTalk()) {
            if (StringUtils.isBlank(user.getgTalkEmail())) {
                throw new IOException("you must specify your gtalk address");
            }
            if (StringUtils.isBlank(user.getgTalkPassword())) {
                throw new IOException("gtalk password missing");
            }
            if (!GTalk.validateCredentials(user.getgTalkEmail(), user.getgTalkPassword())) {
                throw new IOException("GTalk Address/Password could not be verified");
            }
        }
        // Check Prowl
        boolean prowlKeyPresent = !StringUtils.isBlank(user.getProwlApiKeys());
        if (user.isSendProwl() && !prowlKeyPresent) {
            throw new IOException("Prowl keys missing");
        }
        if (prowlKeyPresent) {
            String[] prowlKeys = user.getProwlApiKeys().split(",");
            for (String key : prowlKeys) {
                if (!Prowl.verify(key)) {
                    throw new IOException("Prowl key '" + key + "' could not be verified");
                }
            }
        }

        // Check Howl
        if (user.isSendHowl()) {
            if (StringUtils.isBlank(user.getHowlUsername())) {
                throw new IOException("you must specify the howl username");
            }
            if (StringUtils.isBlank(user.getHowlPassword())) {
                throw new IOException("you must specify the howl password");
            }
        }

        // Check out emails
        boolean emailsPresent = !StringUtils.isBlank(user.getEmailAddresses());
        if (user.isSendEmail() && !emailsPresent) {
            throw new IOException("You must specify outbound emails for Email notifications.");
        }
        if (emailsPresent) {
            String[] emails = user.getEmailAddresses().split(",");
            for (String email : emails) {
                if (!EmailUtils.isEmail(email)) {
                    throw new IOException("Email '" + email + "' not valid");
                }
            }
        }

        // Check SMS Group
        if (user.isSendSMS()) {
            String userGroup = user.getSmsGroup();
            if (StringUtils.isBlank(user.getSmsGroup())) {
                throw new IOException("You must specify an SMS group or number for SMS notifications.");
            }
            if (!PhoneUtil.isNumber(userGroup)) {
                List<String> groups = null;
                if (user.isGvPassword()) {
                    groups = GoogleContacts.getGroups(user.getEmail(), user.getPassword());
                } else {
                    groups = GoogleContacts.getGroups(user.getgTalkEmail(), user.getgTalkPassword());
                }
                boolean found = false;
                for (String group : groups) {
                    if (group.equals(userGroup)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new IOException("SMS Group '" + userGroup + "' does not exist");
                }

            }
        }

        // Check post urls
        boolean postsPresent = !StringUtils.isBlank(user.getPostURLs());
        if (user.isSendPost() && !postsPresent) {
            throw new IOException("You must specify post urls for Post notifications.");
        }
        if (postsPresent) {
            String[] urls = user.getPostURLs().split(",");
            for (String url : urls) {
                if (!MiscUtils.isUrl(url)) {
                    throw new IOException("URL '" + url + "' in post urls is invalid.");
                }
            }
        }

        // Check Twitter
        if (user.isSendTwitter() && StringUtils.isBlank(user.getTwitterScreenName())) {
            throw new IOException("You must specify your Twitter screen name");
            // try {
            // tw.createFriendship(user.twScreenName);
            // } catch (TwitterException e) {
            // }
        }
        // Check AutoRespond
        if (user.isSendAutoResponse() && StringUtils.isBlank(user.getAutoResponse())) {
            throw new IOException("You must specify an auto response message");
        }
        userDAO.store(user);
    }

    public String sendSMS(String pin, String number, String text, String callback) throws IOException {
        APIAction action = new APIAction(ACTIONS.SEND);
        action.setId("" + apiCount.incrementAndGet());
        action.setPin(pin);
        action.setNumber(number);
        action.setText(text);
        action.setCallback(callback);
        apiQueue.enqueue(action);
        return action.getId();
    }

    public void sendXMPPIn(XMPPAction action) throws IOException {
        xmppQueue.enqueue(action);
    }

    public User checkCredentials(User user, String email, String password) throws IOException {
        if (!user.isGvPassword()) {
            if (user.getPassword().equals(password)) {
                return user;
            }
            throw new IOException("invalid credentials: 2");
        }
        // Is GV try login
        GoogleVoice gv = new GoogleVoice(email, password);
        gv.login();
        // If pass != account.pass update account.pass
        if (!user.getPassword().equals(password)) {
            user.setPassword(password);
            userDAO.store(user);
        }
        return user;
    }

    public String createPin(String email) {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

//    public void createFilters(String email, String password, String pin, RegistrationInfo info) {
//        GoogleVoice gv = new GoogleVoice(email, password);
//        try {
//            info.setGvFwdEmail(gv.getForwardingAddress().toLowerCase());
//        } catch (Exception e) {
//            info.setInvalidCredentials(true);
//            return;
//        }
//        if (email.equals(info.getGvFwdEmail())) {
//            // Setup GV
//            try {
//                gv.enableForwarding(true, true);
//            } catch (IOException e) {
//                info.setFiltersCreated(false);
//                return;
//            }
//
//            GMail gmail = new GMail();
//            // SMS FILTER
//            GMailFilter filter = new GMailFilter();
//            filter.setFrom("txt.voice.google.com OR voice-noreply@google.com");
//            // HANI
//            filter.setFwdEmail(pin + "@" + relay.getEmailHost());
//            filter.setSkipInbox(true);
//            try {
//                gmail.createFilter(email, password, filter);
//                info.setFiltersCreated(true);
//            } catch (Exception e) {
//                info.setFiltersCreated(false);
//            }
//        }
//
//    }
//
//    private boolean deleteFilters(String email, String password, String fwdEmail) {
//        GMail gmail = new GMail();
//        try {
//            gmail.deleteFilterWithFwdEmail(email, password, fwdEmail);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }

    public void blacklist(String value) {
        userDAO.blacklist(value);
    }

    private boolean isBlacklisted(String email, User user) {
        boolean blackListed = userDAO.isBlacklisted(email);
        if (!blackListed && user != null && user.getGvPhone() != null) {
            blackListed = userDAO.isBlacklisted(user.getGvPhone());
        }
        return blackListed;
    }

}
