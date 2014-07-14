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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Miscellaneous utilities.
 */
public final class MiscUtils {

    /**
     * Utility hidding constructor.
     */
    private MiscUtils() {}

    public static void emptyBlock() {}

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            emptyBlock();
        }
    }

    public static boolean isUrl(String url) {
        if (url == null) {
            return false;
        }
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

}
