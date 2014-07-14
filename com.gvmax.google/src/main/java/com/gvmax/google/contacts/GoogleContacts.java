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
package com.gvmax.google.contacts;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.ContactGroupEntry;
import com.google.gdata.data.contacts.ContactGroupFeed;
import com.google.gdata.data.extensions.FullName;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.util.ServiceException;
import com.gvmax.common.util.MiscUtils;
import com.gvmax.common.util.PhoneUtil;

public final class GoogleContacts {

    private GoogleContacts() {}

    public static String getContact(String email, String password, String number) {
        try {
            ContactEntry entry = getContactEntryByNumber(email, password, number);
            if (entry == null) {
                return null;
            }
            Name name = entry.getName();
            if (name != null) {
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
            }
        } catch (Exception e) {
            MiscUtils.emptyBlock();
        }
        return null;
    }

    public static ContactEntry getContactEntryByNumber(String email, String password, String number) throws IOException, ServiceException {
        ContactsService contactService = new ContactsService("gvmax");
        contactService.setReadTimeout(5000);
        contactService.setConnectTimeout(5000);
        contactService.setUserCredentials(email, password);
        URL feedUrl = new URL("http://www.google.com/m8/feeds/contacts/" + email + "/full");
        Query myQuery = new Query(feedUrl);
        myQuery.setMaxResults(1000);
        ContactFeed resultFeed = contactService.query(myQuery, ContactFeed.class);

        for (int i = 0; i < resultFeed.getEntries().size(); i++) {
            ContactEntry entry = resultFeed.getEntries().get(i);
            Name name = entry.getName();
            if (name != null) {
                FullName full = name.getFullName();
                if (full != null) {
                    List<PhoneNumber> phones = entry.getPhoneNumbers();
                    for (PhoneNumber phone : phones) {
                        if (PhoneUtil.numbersMatch(phone.getPhoneNumber(), number)) {
                            return entry;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static Map<String, String> getNumbersInGroup(String email, String password, String group) {
        Map<String, String> retVal = new HashMap<String, String>();
        try {
            ContactsService contactService = new ContactsService("gvmax");
            contactService.setReadTimeout(5000);
            contactService.setConnectTimeout(5000);
            contactService.setUserCredentials(email, password);

            String groupId = getGroupId(contactService, email, group);
            if (groupId == null) {
                groupId = getGroupId(contactService, email, "System Group: " + group);
                if (groupId == null) {
                    return null;
                }
                //group = "System Group: " + group;
            }

            URL feedUrl = new URL("http://www.google.com/m8/feeds/contacts/" + email + "/full");
            Query myQuery = new Query(feedUrl);
            myQuery.setMaxResults(1000);
            myQuery.setStringCustomParameter("group", groupId);
            ContactFeed resultFeed = contactService.query(myQuery, ContactFeed.class);

            for (int i = 0; i < resultFeed.getEntries().size(); i++) {
                ContactEntry entry = resultFeed.getEntries().get(i);
                List<PhoneNumber> phones = entry.getPhoneNumbers();
                for (PhoneNumber number : phones) {
                    String label = number.getLabel();
                    if (label == null) {
                        int pos = number.getRel().lastIndexOf("#");
                        if (pos != -1) {
                            label = number.getRel().substring(pos + 1);
                        }
                    }
                    if ("mobile".equals(label)) {
                        retVal.put(entry.getName().getFullName().getValue(), PhoneUtil.normalizeNumber(number.getPhoneNumber()));
                    }
                }
            }
        } catch (Exception e) {
            // logger.warning("Error while looking up numbers in group : "+e.getMessage());
            MiscUtils.emptyBlock();
        }
        return retVal;
    }

    private static String getGroupId(ContactsService contactService, String email, String group) {
        try {
            group = groupXmppId(group);
            URL feedUrl = new URL("http://www.google.com/m8/feeds/groups/" + email + "/full");
            ContactGroupFeed resultFeed = contactService.getFeed(feedUrl, ContactGroupFeed.class);
            for (int i = 0; i < resultFeed.getEntries().size(); i++) {
                ContactGroupEntry groupEntry = resultFeed.getEntries().get(i);
                if (group.equals(groupXmppId(groupEntry.getTitle().getPlainText()))) {
                    return groupEntry.getId();
                }
            }
        } catch (Exception e) {
            // logger.warning("Error while looking up groupId for group : "+e.getMessage());
            MiscUtils.emptyBlock();
        }
        return null;
    }

    private static String groupXmppId(String groupTitle) {
        if (groupTitle == null) {
            return null;
        }
        groupTitle = groupTitle.toLowerCase();
        groupTitle = groupTitle.replace(' ', '_');
        groupTitle = groupTitle.replace(':', '.');
        return groupTitle;
    }

    public static List<String> getGroups(String email, String password) throws IOException {
        List<String> retVal = new ArrayList<String>();
        try {
            ContactsService contactService = new ContactsService("gvmax");
            contactService.setReadTimeout(5000);
            contactService.setConnectTimeout(5000);
            contactService.setUserCredentials(email, password);

            URL feedUrl = new URL("http://www.google.com/m8/feeds/groups/" + email + "/full");
            ContactGroupFeed resultFeed = contactService.getFeed(feedUrl, ContactGroupFeed.class);
            for (int i = 0; i < resultFeed.getEntries().size(); i++) {
                ContactGroupEntry groupEntry = resultFeed.getEntries().get(i);
                retVal.add(groupEntry.getTitle().getPlainText());
            }
            return retVal;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
