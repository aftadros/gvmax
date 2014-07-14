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
package com.gvmax.google.talk;

import java.io.IOException;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import com.gvmax.common.util.MiscUtils;

public final class GTalk {

    private GTalk() {}

    public static boolean validateCredentials(String email, String password) {
        try {
            ConnectionConfiguration config = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
            XMPPConnection con = new XMPPConnection(config);
            con.connect();
            con.login(email, password);
            con.disconnect();
            return true;
        } catch (XMPPException e) {
            return false;
        }
    }

    public static boolean addBuddy(String email, String password, final String buddy, String nickname, String group) throws IOException {
        try {
            ConnectionConfiguration config = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
            XMPPConnection con = new XMPPConnection(config);
            con.connect();
            con.login(email, password);

            Roster roster = con.getRoster();
            roster.reload();
            roster.setSubscriptionMode(SubscriptionMode.accept_all);
            roster.reload();

            if (roster.contains(buddy)) {
                con.disconnect();
                return true;
            }

            try {
                roster.createGroup(group);
            } catch (Exception e) {
                // Assume failed due to group already existing
                MiscUtils.emptyBlock();
            }

            String[] groups = new String[0];
            if (group != null) {
                groups = new String[1];
                groups[0] = group;
            }

            // Create entry
            roster.createEntry(buddy, nickname, groups);
            MiscUtils.sleep(5000);
            con.disconnect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
