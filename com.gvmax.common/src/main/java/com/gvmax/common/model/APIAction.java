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
 * Encapsulates an API action
 */
public class APIAction implements Serializable {
    public static enum ACTIONS { SEND };
    /** Used for Java serialization */
    private static final long serialVersionUID = 1L;
    /** Timestamp of when the API action was received */
    private long date;
    /** Identifier */
    private String id;
    /** API action to take */
    private ACTIONS action;
    /** User email */
    private String email;
    /** User pin */
    private String pin;
    /** Phone number */
    private String number;
    /** Text */
    private String text;
    /** Callback */
    private String callback;

    public APIAction() {}

    public APIAction(ACTIONS action) {
        this.action = action;
    }

    // -------------------
    // GETTERS AND SETTERS
    // -------------------

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ACTIONS getAction() {
        return action;
    }

    public void setAction(ACTIONS action) {
        this.action = action;
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
}
