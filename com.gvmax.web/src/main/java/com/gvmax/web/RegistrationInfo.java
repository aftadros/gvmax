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

import java.io.Serializable;

/**
 * Encapsulates the result of registration
 */
public class RegistrationInfo implements Serializable {
    /** Used for Java serialization */
    private static final long serialVersionUID = 1L;
    /** Was an invalid credetials provided */
    private boolean invalidCredentials;
    /** Account was already registered */
    private boolean alreadyRegistered;
    /** Did registration succeed */
    private boolean registered;
    /** The Google Voice fowarding address */
    private String gvFwdEmail; // TODO: Do I still need this.
    /** Were filters created */
    private boolean filtersCreated;
    /** Was a Google Voice password used */
    private boolean gvPassword;
    /** The users pin */
    private String pin;
    /** Is this blacklisted */
    private boolean blacklisted;

    // -------------------
    // GETTERS AND SETTERS
    // -------------------

    public boolean isInvalidCredentials() {
        return invalidCredentials;
    }

    public void setInvalidCredentials(boolean invalidCredentials) {
        this.invalidCredentials = invalidCredentials;
    }

    public boolean isAlreadyRegistered() {
        return alreadyRegistered;
    }

    public void setAlreadyRegistered(boolean alreadyRegistered) {
        this.alreadyRegistered = alreadyRegistered;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public String getGvFwdEmail() {
        return gvFwdEmail;
    }

    public void setGvFwdEmail(String gvFwdEmail) {
        this.gvFwdEmail = gvFwdEmail;
    }

    public boolean isFiltersCreated() {
        return filtersCreated;
    }

    public void setFiltersCreated(boolean filtersCreated) {
        this.filtersCreated = filtersCreated;
    }

    public boolean isGvPassword() {
        return gvPassword;
    }

    public void setGvPassword(boolean gvPassword) {
        this.gvPassword = gvPassword;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public boolean isBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
    }

}
