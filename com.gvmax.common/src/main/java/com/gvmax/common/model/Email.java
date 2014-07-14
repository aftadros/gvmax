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
 * Encapsulates content of an email.
 */
public class Email implements Serializable {
    /** Used for Java serialization */
    private static final long serialVersionUID = 1L;
    /** Email timestamp */
    private long timestamp = System.currentTimeMillis();
    /** Who sent the email */
    private String from;
    /** Original email sender, this allows me to figure out if this is coming from SMS or Voicemail */
    private String originalFrom;
    /** Who is the email sent to */
    private String to;
    /** Email subject line */
    private String subject;
    /** Email content as text */
    private String text;

    // -------------------
    // GETTERS AND SETTERS
    // -------------------

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long time) {
        this.timestamp = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getOriginalFrom() {
        return originalFrom;
    }

    public void setOriginalFrom(String oFrom) {
        this.originalFrom = oFrom;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
