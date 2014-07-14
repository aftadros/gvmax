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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.gvmax.common.util.Constants;
import com.gvmax.common.util.NetUtil;
import com.gvmax.common.util.PhoneUtil;

public class GoogleVoice {
    private static final Logger logger = Logger.getLogger(GoogleVoice.class);
    private String email;
    private String password;
    private String auth;
    private String rnr;
    private String phoneNumber;
    private List<GoogleVoicePhone> phones = new ArrayList<GoogleVoicePhone>();

    public GoogleVoice(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void login() throws IOException {
        Map<String,String> params = new HashMap<String, String>();
        params.put("accountType", "HOSTED_OR_GOOGLE");
        params.put("Email", email);
        params.put("Passwd", password);
        params.put("service", "grandcentral");
        params.put("source", "GOOGLE");

        auth = extractAuth(NetUtil.doPost("https://www.google.com/accounts/ClientLogin", params));
        extractJsonInfo();
    }

    public String getPhoneNumber() throws IOException {
        ensureLoggedIn();
        return phoneNumber;
    }

    public List<GoogleVoicePhone> getPhones() throws IOException {
        ensureLoggedIn();
        return phones;
    }

    public void sendSMS(String number, String text) throws IOException {
        ensureLoggedIn();
        Map<String, String> params = new HashMap<String, String>();
        params.put("_rnr_se", rnr);
        params.put("c", "undefined");
        params.put("id", "undefined");
        params.put("smstext", text);
        params.put("number", PhoneUtil.normalizeNumber(number));

        Map<String, String> reqProps = new HashMap<String, String>();
        reqProps.put("Authorization", "GoogleLogin auth=" + auth);

        String res = NetUtil.doPost("https://www.google.com/voice/m/sendsms", params, reqProps);
        if (!res.contains("Text sent")) {
            throw new IOException("Error sending SMS");
        }
    }

    public void sendSMS(final String[] numbers, final String text) throws IOException {
        ensureLoggedIn();
        if (numbers == null || numbers.length == 0) {
            return;
        }
        sendSMS(numbers[0], text);
        if (numbers.length > 1) {
            new Thread() {
                @Override
                public void run() {
                    for (int x = 1; x < numbers.length; x++) {
                        try {
                            logger.info("Sending SMS # " + x);
                            sendSMS(numbers[x], text);
                            Thread.sleep(10000);
                        } catch (Exception e) {
                            logger.warn("Unable to send SMS : "+e.getMessage());
                        }
                    }
                }
            }.start();
        }
    }

    public void call(final String number, final String forwardPhoneNumber) throws IOException {
        ensureLoggedIn();

        boolean validPhone = false;
        for (GoogleVoicePhone phone : phones) {
            if (PhoneUtil.numbersMatch(phone.getNumber(), forwardPhoneNumber)) {
                validPhone = true;
                break;
            }
        }

        if (!validPhone) {
            throw new IOException("Invalid forwarding phone");
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("_rnr_se", rnr);
        params.put("number", PhoneUtil.normalizeNumber(number));
        params.put("phone", forwardPhoneNumber);

        Map<String, String> reqProps = new HashMap<String, String>();
        reqProps.put("Authorization", "GoogleLogin auth=" + auth);

        String res = NetUtil.doPost("https://www.google.com/voice/m/sendcall", params, reqProps);
        if (!res.contains("This may take a few seconds")) {
            throw new IOException("Error placing call");
        }
    }


    // ------------------
    // UTILS
    // ------------------

    private String load(String location) throws IOException {
        Map<String, String> reqProps = new HashMap<String, String>();
        reqProps.put("Authorization", "GoogleLogin auth="+auth);
        return NetUtil.doGet(location,null,reqProps,Constants.UTF8);
    }

    private String extractAuth(String res) throws IOException {
        String retVal = null;
        BufferedReader reader = new BufferedReader(new StringReader(res));
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.contains("Auth=")) {
                retVal = line.split("=", 2)[1].trim();
            }
        }

        if (retVal == null) {
            throw new IOException("Unable to login to GoogleVoice [1]");
        }
        return retVal;
    }

    private void extractJsonInfo() throws IOException {
        String json = load("https://www.google.com/voice/");
        json = json.substring(json.indexOf("_gcData = ") + 10);
        json = json.substring(0, json.indexOf("};") + 1);
        try {
            JSONObject jobj = new JSONObject(json);
            rnr = jobj.getString("_rnr_se");
            phoneNumber = PhoneUtil.normalizeNumber(jobj.getJSONObject("number").getString("formatted"));
            JSONObject jphones = jobj.getJSONObject("phones");
            @SuppressWarnings("unchecked")
            Iterator<String> phoneKeys = jphones.keys();
            while (phoneKeys.hasNext()) {
                String phoneKey = phoneKeys.next();
                JSONObject jphone = jphones.getJSONObject(phoneKey);
                GoogleVoicePhone phone = new GoogleVoicePhone();
                phone.setName(jphone.getString("name"));
                phone.setNumber(PhoneUtil.normalizeNumber(jphone.getString("phoneNumber")));
                phone.setType("" + jphone.getInt("type"));
                phones.add(phone);
            }
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    protected void ensureLoggedIn() throws IOException {
        if (auth == null) {
            login();
        }
    }

}
