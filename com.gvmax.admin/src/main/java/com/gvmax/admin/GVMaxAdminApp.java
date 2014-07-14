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
package com.gvmax.admin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;

import com.gvmax.common.util.MiscUtils;
import com.gvmax.common.util.NetUtil;

public final class GVMaxAdminApp {
    public static final Logger logger = Logger.getLogger(GVMaxAdminApp.class);
    private MainFrame frame;
    private static String serverUrl = "https://localhost:9443";
    private static String pin = "";
    private static boolean dontValidate = true;
    private JSONObject user;
    private JSONObject stats;

    static {
        try {
            Properties props = new Properties();
            try {
                props.load(new ClassPathResource("admin.properties").getInputStream());
            } catch (IOException e) {
                MiscUtils.emptyBlock();
            }
            serverUrl = props.getProperty("server",serverUrl);
            pin = props.getProperty("pin");
            dontValidate = !"true".equals(props.getProperty("validate","true"));
            if (dontValidate) {
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private GVMaxAdminApp() {
        frame = new MainFrame();
        frame.pack();
        frame.setSize(frame.getWidth(), 400);
        frame.getPinText().setText(pin);
        frame.setVisible(true);
        frame.getLookupButt().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                lookup();
            }
        });
        frame.getUnregisterButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                unregister();
            }
        });
    }

    private void lookup() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("email", frame.getEmailText().getText());
        params.put("pin", frame.getPinText().getText());
        try {
            String res = NetUtil.doGet(serverUrl + "/api/user.json", params);
            setInfo(new JSONObject(res));
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void unregister() {
        try {
            if (user == null) {
                return;
            }
            Map<String, String> params = new HashMap<String, String>();
            params.put("pin", user.getString("pin"));
            String res = NetUtil.doPost(serverUrl + "/api/unregister.json", params);
            logger.info(res);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void setInfo(JSONObject jobj) {
        if (jobj != null) {
            if (jobj.has("user")) {
                user = jobj.getJSONObject("user");
                stats = jobj.getJSONObject("stats");
                frame.getUserText().setText(user.toString(4));
                frame.getStatsText().setText(stats.toString(4));
            } else {
                jobj = null;
            }
        }

        if (jobj == null) {
            user = null;
            stats = null;
            frame.getUserText().setText("");
            frame.getStatsText().setText("");
            return;
        }

    }

    public static void main(String[] args) {
        new GVMaxAdminApp();
    }

}
