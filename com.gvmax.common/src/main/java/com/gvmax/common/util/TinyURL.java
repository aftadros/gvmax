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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates tiny urls using tinyurl.com
 */
public final class TinyURL {

    /**
     * Utility hidding constructor.
     */
    private TinyURL() {}

    public static String getTinyUrl(String longUrl) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("url", longUrl);
        return NetUtil.doPost("http://tinyurl.com/api-create.php", params);
    }

}
