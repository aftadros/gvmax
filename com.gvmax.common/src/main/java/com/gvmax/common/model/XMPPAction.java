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
 * Encapsulates an XMPP Action
 */
public class XMPPAction implements Serializable {
    /** Used for Java serialization */
    private static final long serialVersionUID = 1L;
    /** Timestamp of when XMPP was received */
    private long timestamp;
    /** Who sent it */
    private String from;
    /** Who is it for */
    private String[] to;
    /** The XMPP message */
    private String message;
    // TODO: Review this
    /** Bot id */
    private String botJId;
    /** Bot number */
    private String botNumber;
    /** Sender id */
    private String senderJId;

    public XMPPAction() {
    }

    public XMPPAction(String from, String[] to, String message) {
        this.timestamp = System.currentTimeMillis();
        this.from = from;
        if (to != null) {
            this.to = to.clone();
        } else {
            this.to = new String[0];
        }
        this.message = message;
    }

    // -------------------
    // GETTERS AND SETTERS
    // -------------------

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long date) {
        this.timestamp = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String[] getTo() {
        return to.clone();
    }

    public void setTo(String[] to) {
        if (to == null) {
            this.to = new String[0];
        } else {
            this.to = to.clone();
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBotJId() {
        return botJId;
    }

    public void setBotJId(String botJId) {
        this.botJId = botJId;
    }

    public String getSenderJId() {
        return senderJId;
    }

    public void setSenderJId(String senderJId) {
        this.senderJId = senderJId;
    }

    public String getBotNumber() {
        return botNumber;
    }

    public void setBotNumber(String botNumber) {
        this.botNumber = botNumber;
    }

}
