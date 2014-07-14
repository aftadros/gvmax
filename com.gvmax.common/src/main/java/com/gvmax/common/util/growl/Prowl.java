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
package com.gvmax.common.util.growl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gvmax.common.util.MiscUtils;
import com.gvmax.common.util.NetUtil;

/**
 * Class used to interface with Prowl
 */
public final class Prowl {

    private Prowl() {}

    /**
     * Checks to see if the given apikey is valid
     *
     * @param apikey
     *            The prowl api key to be verified
     * @return True if valid, false if invalid or verification failed.
     */
    public static boolean verify(String apikey) {
        if (apikey == null) {
            return false;
        }
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("apikey", apikey);
            String res = NetUtil.doGet("https://prowl.weks.net/publicapi/verify", params);
            if (res.indexOf("success") != -1) {
                return true;
            }
        } catch (Exception e) {
            MiscUtils.emptyBlock();
        }
        return false;
    }

    /**
     * Sends a prowl notification
     *
     * @param apikey
     *            The prowl api key
     * @param application
     *            The application name
     * @param event
     *            The event name
     * @param description
     *            The event description
     * @throws IOException
     *             Thrown if the notification fails
     */
    public static void notify(String apikey, String application, String event, String description, int priority) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("apikey", apikey);
        params.put("application", application);
        params.put("event", event);
        params.put("description", description);
        params.put("priority", "" + priority);
        String res = NetUtil.doPost("https://prowl.weks.net/publicapi/add", params);
        if (res.indexOf("success") == -1) {
            throw new IOException("Prowl Notification delivery failed");
        }
    }

}
