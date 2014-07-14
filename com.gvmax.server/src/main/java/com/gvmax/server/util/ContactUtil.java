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
package com.gvmax.server.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.FullName;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.PhoneNumber;
import com.gvmax.common.model.User;
import com.gvmax.common.util.PhoneUtil;
import com.gvmax.google.contacts.GoogleContacts;

public final class ContactUtil {
    private static final Logger logger = Logger.getLogger(ContactUtil.class);

    private ContactUtil() {}

    public static ContactEntry getContactEntry(User user, String number) {
        ContactEntry entry = null;
        try {
            if (user.isGvPassword()) {
                entry = GoogleContacts.getContactEntryByNumber(user.getEmail(), user.getPassword(), number);
            }
            if (entry == null && user.getgTalkEmail() != null) {
                entry = GoogleContacts.getContactEntryByNumber(user.getgTalkEmail(), user.getgTalkPassword(), number);
            }
        } catch (Exception e) {
            logger.warn("Error getting contact: " + e.getMessage());
        }
        return entry;
    }

    public static String getContact(User user, String number) {
        ContactEntry entry = getContactEntry(user, number);
        if (entry == null) {
            return null;
        }
        Name name = entry.getName();
        if (name == null) {
            return null;
        }
        FullName full = name.getFullName();
        if (full != null) {
            List<PhoneNumber> phones = entry.getPhoneNumbers();
            for (PhoneNumber phone : phones) {
                if (PhoneUtil.numbersMatch(phone.getPhoneNumber(), number)) {
                    String label = phone.getLabel();
                    if (label == null) {
                        int pos = phone.getRel().lastIndexOf("#");
                        if (pos != -1) {
                            label = phone.getRel().substring(pos + 1);
                        }
                    }
                    return full.getValue() + " [" + label + "]";
                }
            }
        }
        return null;
    }

    public static List<String> getGroups(User user) throws IOException {
        if (user.isGvPassword()) {
            return GoogleContacts.getGroups(user.getEmail(), user.getPassword());
        }
        return GoogleContacts.getGroups(user.getgTalkEmail(), user.getgTalkPassword());
    }

    public static Map<String, String> getNumbersInGroup(User user, String group) {
        if (user.isGvPassword()) {
            return GoogleContacts.getNumbersInGroup(user.getEmail(), user.getPassword(), group);
        }
        return GoogleContacts.getNumbersInGroup(user.getgTalkEmail(), user.getgTalkPassword(), group);
    }

}
