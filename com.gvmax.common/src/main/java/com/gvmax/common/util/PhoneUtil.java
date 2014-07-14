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

/**
 * Phone related utilities.
 */
public final class PhoneUtil {

    /**
     * Utility hidding constructor.
     */
    private PhoneUtil() {}

    /**
     * Normalizes a number. Removes spaces,+,-,(,) characters
     *
     * @param number
     *            The number to normalize
     * @return The normalized number
     */
    public static String normalizeNumber(String number) {
        if (number == null) {
            return null;
        }
        number = number.replace(" ", "");
        number = number.replace("+", "");
        number = number.replace("-", "");
        number = number.replace("(", "");
        number = number.replace(")", "");
        return number;
    }

    /**
     * Checks if two numbers match. The numbers are normalized and then only the
     * last 10 digits are checked for equality. This is to do with being able to
     * handle number with/without international codes etc...
     *
     * @param n1
     *            The first number
     * @param n2
     *            The second number
     * @return True if match, false otherwise
     */
    public static boolean numbersMatch(String n1, String n2) {
        if (n1 == null || n2 == null) {
            return false;
        }
        n1 = normalizeNumber(n1);
        n2 = normalizeNumber(n2);
        int size = 10;
        if (n1.length() < size) {
            size = n1.length();
        }
        if (n2.length() < size) {
            size = n2.length();
        }
        String i1 = n1.substring(Math.max(0, n1.length() - size));
        String i2 = n2.substring(Math.max(0, n2.length() - size));
        return i1.equals(i2);
    }

    /**
     * Checks if the given string is a potential phone number. not 100% accurate
     *
     * @param number
     *            The string to check
     * @return True/False
     */
    public static boolean isNumber(String number) {
        try {
            Long.parseLong(normalizeNumber(number));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
