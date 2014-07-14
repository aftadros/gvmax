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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * Simple HTTP GET/POST request handler
 */
public final class NetUtil {
    public static final int TIMEOUT = 10000;
    /**
     * Utility hidding constructor.
     */
    private NetUtil() {}

    public static String doGet(String url) throws IOException {
        return doGet(url,Constants.UTF8);
    }

    public static String doGet(String url, String charset) throws IOException {
        URL u = new URL(url);
        URLConnection conn = u.openConnection();
        conn.setReadTimeout(TIMEOUT);
        conn.setConnectTimeout(TIMEOUT);
        InputStream in = conn.getInputStream();
        try {
            return IOUtil.toString(in, charset);
        } finally {
            IOUtil.close(in);
        }
    }

    public static String doGet(String url, Map<String,String> params, Map<String,String> reqProps, String charset) throws IOException {
        if (params != null && params.size() > 0) {
            url = url + "?" + paramsToString(params);
        }
        URL u = new URL(url);
        URLConnection conn = u.openConnection();
        conn.setReadTimeout(TIMEOUT);
        conn.setConnectTimeout(TIMEOUT);
        if (reqProps != null) {
            for (Map.Entry<String, String> rp : reqProps.entrySet()) {
                conn.setRequestProperty(rp.getKey(), rp.getValue());
            }
        }
        InputStream in = conn.getInputStream();
        try {
            return IOUtil.toString(in, charset);
        } finally {
            IOUtil.close(in);
        }
    }

    public static String doGet(String url, Map<String, String> params) throws IOException {
        if (params != null && params.size() > 0) {
            url = url + "?" + paramsToString(params);
        }
        return doGet(url);
    }

    public static String doPost(String url, Map<String, String> params) throws IOException {
        return doPost(url, params, null, Constants.UTF8);
    }

    public static String doPost(String url, Map<String, String> params, String charset) throws IOException {
        return doPost(url, params, null, charset);
    }

    public static String doPost(String url, Map<String, String> params, Map<String, String> reqProperties) throws IOException {
        return doPost(url, params, reqProperties, Constants.UTF8);
    }

    public static String doPost(String url, Map<String, String> params, Map<String, String> reqProperties, String charset) throws IOException {
        String paramsStr = paramsToString(params);
        byte[] postData = paramsStr.getBytes(Constants.UTF8);

        URL u = new URL(url);

        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setReadTimeout(TIMEOUT);
        conn.setConnectTimeout(TIMEOUT);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
        if (reqProperties != null) {
            for (Map.Entry<String, String> rp : reqProperties.entrySet()) {
                conn.setRequestProperty(rp.getKey(), rp.getValue());
            }
        }

        OutputStream out = conn.getOutputStream();
        try {
            out.write(postData);
        } finally {
            IOUtil.close(out);
        }

        InputStream in = conn.getInputStream();
        try {
            return IOUtil.toString(in, charset);
        } finally {
            IOUtil.close(in);
        }
    }

    public static String paramsToString(Map<String, String> params) throws IOException {
        StringBuffer paramsStr = new StringBuffer();
        Iterator<Map.Entry<String,String>> paramIter = params.entrySet().iterator();
        if (paramIter.hasNext()) {
            Map.Entry<String, String> param = paramIter.next();
            String paramName = param.getKey();
            paramsStr.append(paramName);
            paramsStr.append("=");
            paramsStr.append(URLEncoder.encode(param.getValue(), Constants.UTF8));
        }
        while (paramIter.hasNext()) {
            Map.Entry<String, String> param = paramIter.next();
            paramsStr.append("&");
            paramsStr.append(param.getKey());
            paramsStr.append("=");
            paramsStr.append(URLEncoder.encode(param.getValue(), Constants.UTF8));
        }
        return paramsStr.toString();
    }



}
