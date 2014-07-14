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

import java.io.UnsupportedEncodingException;

public final class StringUtil {

    /**
     * Utility hidding constructor.
     */
    private StringUtil() {}

    public static String toString(byte[] bytes) {
        try {
            return new String(bytes,Constants.UTF8);
        } catch (UnsupportedEncodingException e) {
            // Should never happen
            return null;
        }
    }

    public static byte[] getBytes(String val) {
        try {
            return val.getBytes(Constants.UTF8);
        } catch (UnsupportedEncodingException e) {
            // Should never happen
            return null;
        }
    }

    public static String[] split(String str, int maxLen) {
        if (str == null) {
            return null;
        }
        int origLen = str.length();

        int splitNum = origLen / maxLen;
        if (origLen % maxLen > 0) {
            splitNum += 1;
        }

        String[] splits = new String[splitNum];
        for (int i = 0; i < splitNum; i++) {
            int startPos = i * maxLen;
            int endPos = startPos + maxLen;
            if (endPos > origLen) {
                endPos = origLen;
            }
            String substr = str.substring(startPos, endPos);
            splits[i] = substr;
        }

        return splits;
    }

}
