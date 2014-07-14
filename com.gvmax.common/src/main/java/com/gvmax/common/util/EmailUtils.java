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
package com.gvmax.common.util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Email related utilities.
 */
public final class EmailUtils {
    /** Email patter matching */
    private static Pattern emailPattern = Pattern.compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$", Pattern.CASE_INSENSITIVE);

    /**
     * Utility hidding constructor.
     */
    private EmailUtils() {}

    public static boolean isEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher m = emailPattern.matcher(email);
        return m.matches();
    }

    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        email = email.toLowerCase().trim();
        if (email.indexOf('@') == -1) {
            email += "@gmail.com";
        }
        return email;
    }

    // TODO: Do I need this or will normalizeEmail do the job
    public static String formatEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    public static String strip(String text) {
        BufferedReader reader = null;
        try {
            StringBuffer buf = new StringBuffer();
            reader = new BufferedReader(new StringReader(text));
            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith(">") && !line.endsWith("wrote:") && !line.equals("--")) {
                    buf.append(line);
                    buf.append("\n");
                }
                line = reader.readLine();
            }
            return buf.toString();
        } catch (Exception e) {
            return text;
        } finally {
            IOUtil.close(reader);
        }
    }

}
