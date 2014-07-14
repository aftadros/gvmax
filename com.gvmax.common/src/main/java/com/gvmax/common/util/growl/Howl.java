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

import org.apache.commons.codec.binary.Base64;

import com.gvmax.common.util.NetUtil;
import com.gvmax.common.util.StringUtil;

/**
 * Utility used to send howl notifications.
 */
public final class Howl {

    private Howl() {}

    public static void notify(String username, String password, String application, String title, String description, String hostname, String iconName) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "GVMax");
        params.put("application", application);
        params.put("title", title);
        params.put("description", description);
        params.put("icon-name", iconName);
        params.put("hostname", "www.gvmax.com");

        String userPassword = username + ":" + password;
        String encoded = StringUtil.toString(Base64.encodeBase64(StringUtil.getBytes(userPassword)));
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("Authorization", "Application " + encoded);

        NetUtil.doPost("https://howlapp.com/public/api/notification", params, properties);
    }

}
