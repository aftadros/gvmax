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
package com.gvmax.common.model;

import java.io.Serializable;

/**
 * Encapsulates user information
 *
 * Note: I should probably split the notifier settings into separate classes.
 */
public class User implements Serializable {
    /** Used for Java serialization */
    private static final long serialVersionUID = 1L;
    /** When was account created */
    private long creationDate;
    // Login Info
    /** User's Google Voice email */
    private String email;
    /** User's password */
    private String password;
    /** Is this password the GoogleVoice password a GVMax password */
    private boolean gvPassword;
    // Monitors
    /** Is the user monitoring SMS */
    private boolean monitorSMS;
    /** Is the user monitoring Voice Mail */
    private boolean monitorVM;
    /** Is the user monitoring Missed Calls */
    private boolean monitorMC;

    // Notifiers
    /** Is GTalk notifier enabled */
    private boolean sendGTalk;
    /** GTalk email that should be notified */
    private String gTalkEmail;
    /** GTalk password */
    private String gTalkPassword;
    /** GTalk group to add new buddies to */
    private String gTalkGroup;

    /** Is HTTPPost notifier enabled */
    private boolean sendPost;
    /** Post urls (comma separated) */
    private String postURLs;

    /** Is email notifier enabled */
    private boolean sendEmail;
    /** Email addresses to be notified (comma separated) */
    private String emailAddresses;

    /** Is SMS notifier enabled */
    private boolean sendSMS;
    /** SMS group to notify */
    private String smsGroup;

    /** Is prowl notifier enabled */
    private boolean sendProwl;
    /** Prowl api keys (comma separated) */
    private String prowlApiKeys; // TODO: Double check I support multiple keys
    /** Prowl SMS notification priority */
    private int prowlSMSPriority;
    /** Prowl Voicemail notification priority */
    private int prowlVMPriority;
    /** Prowl MissedCalls notification priority */
    private int prowlMCPriority;

    /** Is howl notifier enabled */
    private boolean sendHowl;
    /** Howl username */
    private String howlUsername;
    /** Howl password */
    private String howlPassword;

    /** Is twitter notifier enabled */
    private boolean sendTwitter; // TODO: Check that this still works
    /** Screen name to notify */
    private String twitterScreenName;

    /** Is AutoResponse enabled */
    private boolean sendAutoResponse;
    /** AutoResponse message */
    private String autoResponse;

    // Pin
    /** User pin, used for api etc */
    private String pin;

    // GV Info
    /** The users Google Voice number */
    private String gvPhone;
    /** The fowarding phone */
    private String gvFwdPhone;
    /** Fowarding phone type */
    private String gvFwdPhoneType;

    public User() {}

    public User(String email) {
        this.email = email;
    }

    // -------------------
    // GETTERS AND SETTERS
    // -------------------

    public long getCreationDate() {
         return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isGvPassword() {
        return gvPassword;
    }

    public void setGvPassword(boolean gvPassword) {
        this.gvPassword = gvPassword;
    }

    public boolean isMonitorSMS() {
        return monitorSMS;
    }

    public void setMonitorSMS(boolean monitorSMS) {
        this.monitorSMS = monitorSMS;
    }

    public boolean isMonitorVM() {
        return monitorVM;
    }

    public void setMonitorVM(boolean monitorVM) {
        this.monitorVM = monitorVM;
    }

    public boolean isMonitorMC() {
        return monitorMC;
    }

    public void setMonitorMC(boolean monitorMC) {
        this.monitorMC = monitorMC;
    }

    public boolean isSendGTalk() {
        return sendGTalk;
    }

    public void setSendGTalk(boolean sendGTalk) {
        this.sendGTalk = sendGTalk;
    }

    public String getgTalkEmail() {
        return gTalkEmail;
    }

    public void setgTalkEmail(String gTalkEmail) {
        this.gTalkEmail = gTalkEmail;
    }

    public String getgTalkPassword() {
        return gTalkPassword;
    }

    public void setgTalkPassword(String gTalkPassword) {
        this.gTalkPassword = gTalkPassword;
    }

    public String getgTalkGroup() {
        return gTalkGroup;
    }

    public void setgTalkGroup(String gTalkGroup) {
        this.gTalkGroup = gTalkGroup;
    }

    public boolean isSendPost() {
        return sendPost;
    }

    public void setSendPost(boolean sendPost) {
        this.sendPost = sendPost;
    }

    public String getPostURLs() {
        return postURLs;
    }

    public void setPostURLs(String postURLs) {
        this.postURLs = postURLs;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public String getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(String emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public boolean isSendSMS() {
        return sendSMS;
    }

    public void setSendSMS(boolean sendSMS) {
        this.sendSMS = sendSMS;
    }

    public String getSmsGroup() {
        return smsGroup;
    }

    public void setSmsGroup(String smsGroup) {
        this.smsGroup = smsGroup;
    }

    public boolean isSendProwl() {
        return sendProwl;
    }

    public void setSendProwl(boolean sendProwl) {
        this.sendProwl = sendProwl;
    }

    public String getProwlApiKeys() {
        return prowlApiKeys;
    }

    public void setProwlApiKeys(String prowlApiKeys) {
        this.prowlApiKeys = prowlApiKeys;
    }

    public int getProwlSMSPriority() {
        return prowlSMSPriority;
    }

    public void setProwlSMSPriority(int prowlSMSPriority) {
        this.prowlSMSPriority = prowlSMSPriority;
    }

    public int getProwlVMPriority() {
        return prowlVMPriority;
    }

    public void setProwlVMPriority(int prowlVMPriority) {
        this.prowlVMPriority = prowlVMPriority;
    }

    public int getProwlMCPriority() {
        return prowlMCPriority;
    }

    public void setProwlMCPriority(int prowlMCPriority) {
        this.prowlMCPriority = prowlMCPriority;
    }

    public boolean isSendHowl() {
        return sendHowl;
    }

    public void setSendHowl(boolean sendHowl) {
        this.sendHowl = sendHowl;
    }

    public String getHowlUsername() {
        return howlUsername;
    }

    public void setHowlUsername(String howlUsername) {
        this.howlUsername = howlUsername;
    }

    public String getHowlPassword() {
        return howlPassword;
    }

    public void setHowlPassword(String howlPassword) {
        this.howlPassword = howlPassword;
    }

    public boolean isSendTwitter() {
        return sendTwitter;
    }

    public void setSendTwitter(boolean sendTwitter) {
        this.sendTwitter = sendTwitter;
    }

    public String getTwitterScreenName() {
        return twitterScreenName;
    }

    public void setTwitterScreenName(String twitterScreenName) {
        this.twitterScreenName = twitterScreenName;
    }

    public boolean isSendAutoResponse() {
        return sendAutoResponse;
    }

    public void setSendAutoResponse(boolean sendAutoResponse) {
        this.sendAutoResponse = sendAutoResponse;
    }

    public String getAutoResponse() {
        return autoResponse;
    }

    public void setAutoResponse(String autoResponse) {
        this.autoResponse = autoResponse;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getGvPhone() {
        return gvPhone;
    }

    public void setGvPhone(String gvPhone) {
        this.gvPhone = gvPhone;
    }

    public String getGvFwdPhone() {
        return gvFwdPhone;
    }

    public void setGvFwdPhone(String gvFwdPhone) {
        this.gvFwdPhone = gvFwdPhone;
    }

    public String getGvFwdPhoneType() {
        return gvFwdPhoneType;
    }

    public void setGvFwdPhoneType(String gvFwdPhoneType) {
        this.gvFwdPhoneType = gvFwdPhoneType;
    }

}
