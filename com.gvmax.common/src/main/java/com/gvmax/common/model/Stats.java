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
 * Encapsulates statistics
 */
public class Stats implements Serializable {
    /** Used for Java serialization */
    private static final long serialVersionUID = 1L;
    /** When stats was calculated */
    private long timestamp = System.currentTimeMillis();
    /** Account email address */
    private String email;
    /** Account pin */
    private String pin;
    /** Number of sms received */
    private int smsInCount;
    /** Number of voice mail received */
    private int vmInCount;
    /** Number of missed calls received */
    private int mcInCount;
    /** Number of emails received */
    private int emailInCount;
    /** Number of gtalk received */
    private int gTalkCount;
    /** Number of sms sent */
    private int smsOutCount;
    /** Number of api calls received */
    private int apiCount;
    /** Number of errors */
    private int errorCount;
    /** Number of invalid emails ??? */
    private int invalidEmailCount;
    /** Number of fallbacks */
    private int fallbackCount;

    public Stats() {}

    public Stats(String email, String pin) {
        this.email = email;
        this.pin = pin;
    }

    // -------------------
    // GETTERS AND SETTERS
    // -------------------

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long time) {
        this.timestamp = time;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public int getSmsInCount() {
        return smsInCount;
    }

    public void setSmsInCount(int smsInCount) {
        this.smsInCount = smsInCount;
    }

    public int getVmInCount() {
        return vmInCount;
    }

    public void setVmInCount(int vmInCount) {
        this.vmInCount = vmInCount;
    }

    public int getMcInCount() {
        return mcInCount;
    }

    public void setMcInCount(int mcInCount) {
        this.mcInCount = mcInCount;
    }

    public int getgTalkCount() {
        return gTalkCount;
    }

    public void setgTalkCount(int gTalkCount) {
        this.gTalkCount = gTalkCount;
    }

    public int getSmsOutCount() {
        return smsOutCount;
    }

    public void setSmsOutCount(int smsOutCount) {
        this.smsOutCount = smsOutCount;
    }

    public int getApiCount() {
        return apiCount;
    }

    public void setApiCount(int apiCount) {
        this.apiCount = apiCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getEmailInCount() {
        return emailInCount;
    }

    public void setEmailInCount(int emailInCount) {
        this.emailInCount = emailInCount;
    }

    public int getInvalidEmailCount() {
        return invalidEmailCount;
    }

    public void setInvalidEmailCount(int invalidEmailCount) {
        this.invalidEmailCount = invalidEmailCount;
    }

    public int getFallbackCount() {
        return fallbackCount;
    }

    public void setFallbackCount(int fallbackCount) {
        this.fallbackCount = fallbackCount;
    }
}
