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
package com.gvmax.google.voice;

import java.io.Serializable;

/**
 * A Google Voice number
 */
public class GoogleVoicePhone implements Serializable {
    /** Used by Java Serialization */
    private static final long serialVersionUID = 1L;
    // Phone Types
    public static final String TYPE_HOME = "1";
    public static final String TYPE_MOBILE = "2";
    public static final String TYPE_WORK = "3";
    public static final String TYPE_GIZMO = "7";
    public static final String TYPE_GOOGLE_CHAT = "9";
    /** Name given to this phone */
    private String name;
    /** Phone number */
    private String number;
    /** Phone type */
    private String type;
    /** Is it currently selected */
    private boolean defaultPhone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDefaultPhone() {
        return defaultPhone;
    }

    public void setDefaultPhone(boolean defaultPhone) {
        this.defaultPhone = defaultPhone;
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append(name + "[" + number + ":" + type + "]");
        if (defaultPhone) {
            str.append("**");
        }
        return str.toString();
    }

}
