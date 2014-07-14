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
package com.gvmax.web.api;

import static com.gvmax.web.api.APIUtil.getUser;
import static com.gvmax.web.api.APIUtil.internalError;
import static com.gvmax.web.api.APIUtil.invalidCredentials;
import static com.gvmax.web.api.APIUtil.sendError;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.gvmax.common.model.Stats;
import com.gvmax.common.model.User;
import com.gvmax.common.relay.GVMaxRelay;
import com.gvmax.common.util.EmailUtils;
import com.gvmax.common.util.IOUtil;
import com.gvmax.web.GVMaxServiceImpl;
import com.gvmax.web.RegistrationInfo;

/**
 * Web Interface API
 */
@Controller
public class WebAppAPI {
    private static final Logger logger = Logger.getLogger(WebAppAPI.class);
    /** String used in place of passwords */
    private static final String HIDDEN_PASSWORD = "__NO_CHANGE__";
    /** Reference to GVMax service */
    private GVMaxServiceImpl service;
    private GVMaxRelay relay;

    // TODO: make this dynamic
    private Properties thirdParties = new Properties();

    public WebAppAPI() {}

    /**
     * Initializing constructor
     *
     * @param service
     *            Reference to GVMax service
     */
    @Autowired
    public WebAppAPI(GVMaxServiceImpl service, GVMaxRelay relay) throws IOException {
        this.service = service;
        this.relay = relay;
        loadThirdParty();
    }

    private void loadThirdParty() throws IOException {
        ClassPathResource res = new ClassPathResource("thirdparty.properties");
        InputStream in = res.getInputStream();
        try {
            thirdParties.load(in);
        } finally {
            IOUtil.close(in);
        }
        for (Entry<Object, Object> entry : thirdParties.entrySet()) {
            logger.info("Thirdparty: "+entry.getValue()+" registered.");
        }
    }

    /**
     * Returns the currently logged in user.
     *
     * @param pin
     *            Pin for api access
     * @param session
     *            HTTP Session
     * @param resp
     *            HTTP Response
     * @return The user or 'response' = 'nouser'
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    @Timed @ExceptionMetered
    public ModelMap user(@RequestParam(value = "email", required = false) String email, @RequestParam(value = "pin", required = false) String pin, HttpSession session, HttpServletResponse resp) {
        try {
            User user = service.getUser(email,pin);
            if (user == null) {
                user = getUser(service,session, pin);
            }
            if (user != null) {
                user.setPassword(null);
                user.setgTalkPassword(HIDDEN_PASSWORD);
                user.setHowlPassword(HIDDEN_PASSWORD);
                Stats stats = service.getStats(user.getEmail());
                ModelMap retVal = new ModelMap();
                retVal.put("user", user);
                retVal.put("stats", stats);
                return retVal;
            }
            return new ModelMap("response", "nouser");
        } catch (DataAccessException e) {
            internalError(e, resp);
            return null;
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @Timed @ExceptionMetered
    public ModelMap login(@RequestParam("email") String email, @RequestParam("password") String password, HttpSession session, HttpServletResponse resp) {
        try {
            User user = service.login(email, password);
            session.setAttribute("email", user.getEmail());
            return new ModelMap("result", "ok");
        } catch (DataAccessException e) {
            internalError(e, resp);
        } catch (IOException e) {
            invalidCredentials(resp);
        }
        return null;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @Timed @ExceptionMetered
    public ModelMap logout(HttpSession session) {
        session.removeAttribute("email");
        return new ModelMap("result", "ok");
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    @Timed @ExceptionMetered
    public ModelMap signup(@RequestParam(value = "email") String email, @RequestParam(value = "password") String password, @RequestParam(value = "gvPassword", required = false) boolean isGVPassword, HttpSession session, HttpServletResponse resp) throws IOException {
        try {
            email = EmailUtils.normalizeEmail(email);
            RegistrationInfo info = service.signup(email, password, isGVPassword);
            if (info.isInvalidCredentials()) {
                invalidCredentials(resp);
                return null;
            }
            if (info.isRegistered()) {
                String message = "You are receiving this email because your account has been registered with GVMax\n" + "GVMax is a service used to monitor GoogleVoice and provide notifications for incoming SMS and Voicemail\n" + "GVMax also provides other services such as sending Group SMS's and integration with GoogleTalk, Twitter etc...\n" + "You can view your account at https://www.gvmax.com\n" + "username: " + email + "\n";
                try {
                    relay.sendEmail(relay.getEmailSender(), relay.getEmailSender(), email, "Welcome to GVMax", message);
                } catch (Exception e) {
                    logger.warn("Unable to send email: "+e.getMessage());
                }
            }
            session.setAttribute("email", email);
            return new ModelMap("info", info);
        } catch (DataAccessException e) {
            internalError(e, resp);
        }
        return null;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @Timed @ExceptionMetered
    public ModelMap register(@RequestBody MultiValueMap<String, String> params, HttpSession session, HttpServletResponse resp) {
        // Check thirdParty Pin
        String thirdParty = params.getFirst("tpPin");
        if (thirdParty == null) {
            invalidCredentials(resp);
            return null;
        }
        thirdParty = thirdParties.getProperty(thirdParty);
        if (thirdParty == null) {
            invalidCredentials(resp);
            return null;
        }

        // Check email
        String email = params.getFirst("email");
        if (email == null) {
            invalidCredentials(resp);
            return null;
        }
        email = EmailUtils.normalizeEmail(email);

        // Check googlePassword
        String googlePassword = params.getFirst("googlePassword");
        if (googlePassword == null) {
            googlePassword = params.getFirst("password");
        }
        if (googlePassword == null) {
            invalidCredentials(resp);
            return null;
        }

        User user = service.getUser(email);
        if (user == null) {
            user = new User(email);
            user.setPassword(params.getFirst("password"));
            user.setGvPassword(Boolean.parseBoolean(params.getFirst("gvPassword")));
        }
        if (user.getPassword() == null) {
            invalidCredentials(resp);
            return null;
        }

        // Monitors
        if (params.getFirst("monitorSMS") != null) {
            user.setMonitorSMS(Boolean.parseBoolean(params.getFirst("monitorSMS")));
        }
        if (params.getFirst("monitorVM") != null) {
            user.setMonitorVM(Boolean.parseBoolean(params.getFirst("monitorVM")));
        }
        if (params.getFirst("monitorMC") != null) {
            user.setMonitorMC(Boolean.parseBoolean(params.getFirst("monitorMC")));
        }

        // Notifiers
        extractUserFromForm(user, params);

        try {
            RegistrationInfo info = service.register(user, googlePassword);
            if (info.isRegistered()) {
                String message = "You are receiving this email because " + thirdParty + " has registered your account with GVMax\n" + "GVMax is a service used to monitor GoogleVoice and provide notifications for incoming SMS and Voicemail\n" + "GVMax also provides other services such as sending Group SMS's and integration with GoogleTalk, Twitter etc...\n" + "You can view your account at https://www.gvmax.com\n" + "username: " + user.getEmail() + "\n";
                if (info.isAlreadyRegistered()) {
                    message = "You are receiving this email because " + thirdParty + " has modified your account at GVMax\n" + "You can view your account at https://www.gvmax.com\n" + "username: " + user.getEmail() + "\n";
                }
                if (user.isGvPassword()) {
                    message += "password: your google voice password\n";
                } else {
                    message += "password: " + user.getPassword() + "\n";
                }

                try {
                    relay.sendEmail(relay.getEmailSender(), relay.getEmailSender(), user.getEmail(), "Welcome to GVMax", message);
                } catch (Exception e) {
                    logger.warn("Unable to send email: "+e.getMessage());
                }
            }
            return new ModelMap("info", info);
        } catch (DataAccessException e) {
            internalError(e, resp);
            return null;
        }
    }

    @RequestMapping(value = "/unregister", method = RequestMethod.POST)
    @Timed @ExceptionMetered
    public ModelMap unregister(@RequestParam(value = "pin", required = false) String pin, HttpSession session, HttpServletResponse resp) {
        try {
            User user = getUser(service,session, pin);
            if (user == null) {
                invalidCredentials(resp);
                return null;
            }
            service.unregister(user.getEmail());
            logout(session);
            return new ModelMap("result", "ok");
        } catch (DataAccessException e) {
            internalError(e, resp);
        }
        return null;
    }

    @RequestMapping(value = "/monitors", method = RequestMethod.POST)
    @Timed @ExceptionMetered
    public ModelMap monitors(@RequestBody MultiValueMap<String, String> params, HttpSession session, HttpServletResponse resp) {
        try {
            String pin = params.getFirst("pin");
            User user = getUser(service,session, pin);
            if (user == null) {
                invalidCredentials(resp);
                return null;
            }
            boolean monitorSMS = Boolean.parseBoolean(params.getFirst("monitorSMS"));
            boolean monitorVM = Boolean.parseBoolean(params.getFirst("monitorVM"));
            boolean monitorMC = Boolean.parseBoolean(params.getFirst("monitorMC"));
            service.setMonitors(user.getEmail(), monitorSMS, monitorVM, monitorMC);
            return new ModelMap("result", "ok");
        } catch (DataAccessException e) {
            internalError(e, resp);
            return null;
        }
    }

    @RequestMapping(value = "/notifiers", method = RequestMethod.POST)
    @Timed @ExceptionMetered
    public ModelMap notifiers(@RequestBody MultiValueMap<String, String> params, HttpSession session, HttpServletResponse resp) {
        try {
            User user = getUser(service,session, params.getFirst("pin"));
            if (user == null) {
                invalidCredentials(resp);
                return null;
            }

            extractUserFromForm(user, params);

            try {
                service.setNotifiers(user);
                return new ModelMap("result", "ok");
            } catch (IOException e) {
                sendError(400, e.getMessage(), resp);
                return null;
            }
        } catch (DataAccessException e) {
            internalError(e, resp);
            return null;
        }
    }

    @RequestMapping(value = "/enableGV", method = RequestMethod.POST)
    @Timed @ExceptionMetered
    public ModelMap enableGV(@RequestParam(value = "password") String password, @RequestParam(value = "pin", required = false) String pin, HttpSession session, HttpServletResponse resp) {
        try {
            User user = getUser(service,session, pin);
            if (user == null) {
                invalidCredentials(resp);
                return null;
            }
            service.changePassword(user.getEmail(), password, true);
            return new ModelMap("result", "ok");
        } catch (DataAccessException e) {
            internalError(e, resp);
            return null;
        } catch (IOException e) {
            sendError(400, e.getMessage(), resp);
            return null;
        }
    }

    @RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
    @Timed @ExceptionMetered
    public ModelMap forgotPassword(@RequestParam(value = "email") String email, HttpServletResponse resp) {
        try {
            service.forgotPassword(email);
            return new ModelMap("response", "ok");
        } catch (Exception e) {
            logger.warn(e);
            internalError(e, resp);
            return null;
        }
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    @Timed @ExceptionMetered
    public ModelMap changePassword(@RequestParam(value = "pin", required = false) String pin, @RequestParam(value = "old_password", required = false) String oldPassword, @RequestParam(value = "new_password") String newPassword, HttpSession session, HttpServletResponse resp) {
        try {
            User user = getUser(service,session, pin);
            if (user == null) {
                invalidCredentials(resp);
                return null;
            }
            if (user.isGvPassword()) {
                sendError(400, "user is using google voice password. Cannot change password via api.", resp);
                return null;
            }
            if (!user.getPassword().equals(oldPassword)) {
                invalidCredentials(resp);
                return null;
            }
            if (StringUtils.isBlank(newPassword)) {
                invalidCredentials(resp);
                return null;
            }
            service.changePassword(user.getEmail(), newPassword, false);
            return new ModelMap("result", "ok");
        } catch (DataAccessException e) {
            internalError(e, resp);
            return null;
        } catch (IOException e) {
            sendError(400, e.getMessage(), resp);
            return null;
        }
    }



    // -------------------
    // EXTRACT FORM INFO
    // -------------------

    private void extractUserFromForm(User user, MultiValueMap<String, String> form) {
        if (form.containsKey("wc")) {
            ensureExists("sendGTalk", form);
            ensureExists("sendProwl", form);
            ensureExists("sendEmail", form);
            ensureExists("sendPost", form);
            ensureExists("sendTwitter", form);
            ensureExists("sendSMS", form);
            ensureExists("sendHowl", form);
            ensureExists("sendAutoResponse", form);
        }
        // GTALK
        if (form.containsKey("sendGTalk")) {
            user.setSendGTalk(getBoolean("sendGTalk", form));
            user.setgTalkEmail(form.getFirst("gTalkEmail"));
            String gTalkPassword = form.getFirst("gTalkPassword");
            if (!HIDDEN_PASSWORD.equals(gTalkPassword)) {
                user.setgTalkPassword(gTalkPassword);
            }
            user.setgTalkGroup(form.getFirst("gTalkGroup"));
        }

        if (form.containsKey("sendProwl")) {
            user.setSendProwl(getBoolean("sendProwl", form));
            user.setProwlApiKeys(form.getFirst("prowlApiKeys"));
            user.setProwlSMSPriority(getInt("prowlSMSPriority", form));
            user.setProwlVMPriority(getInt("prowlVMPriority", form));
            user.setProwlMCPriority(getInt("prowlMCPriority", form));
        }
        if (form.containsKey("sendEmail")) {
            user.setSendEmail(getBoolean("sendEmail", form));
            user.setEmailAddresses(form.getFirst("emailAddresses"));
        }
        if (form.containsKey("sendPost")) {
            user.setSendPost(getBoolean("sendPost", form));
            user.setPostURLs(form.getFirst("postURLs"));
        }
        if (form.containsKey("sendTwitter")) {
            user.setSendTwitter(getBoolean("sendTwitter", form));
            user.setTwitterScreenName(form.getFirst("twitterScreenName"));
        }
        if (form.containsKey("sendSMS")) {
            user.setSendSMS(getBoolean("sendSMS", form));
            user.setSmsGroup(form.getFirst("smsGroup"));
        }
        if (form.containsKey("sendHowl")) {
            user.setSendHowl(getBoolean("sendHowl", form));
            user.setHowlUsername(form.getFirst("howlUsername"));
            String howlPassword = form.getFirst("howlPassword");
            if (!HIDDEN_PASSWORD.equals(howlPassword)) {
                user.setHowlPassword(howlPassword);
            }
        }
        if (form.containsKey("sendAutoResponse")) {
            user.setSendAutoResponse(getBoolean("sendAutoResponse", form));
            user.setAutoResponse(form.getFirst("autoResponse"));
        }
    }

    private void ensureExists(String key, MultiValueMap<String, String> form) {
        if (!form.containsKey(key)) {
            form.add(key, "false");
        }
    }

    private boolean getBoolean(String key, MultiValueMap<String, String> form) {
        return Boolean.parseBoolean(form.getFirst(key));
    }

    private int getInt(String key, MultiValueMap<String, String> form) {
        try {
            return Integer.parseInt(form.getFirst(key));
        } catch (NumberFormatException e) {
            return 0;
        }
    }


}
