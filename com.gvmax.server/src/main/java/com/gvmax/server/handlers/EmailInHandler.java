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
package com.gvmax.server.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.gvmax.common.model.Email;
import com.gvmax.common.model.User;
import com.gvmax.common.util.EmailUtils;
import com.gvmax.data.user.UserDAO;
import com.gvmax.server.util.Notifier;

/**
 * This class handles sms send emails. Both {pin}-{destination}@xxxx and
 * {pin}-{destination}-{reply}@xxxxx
 */
public class EmailInHandler {
    /** Logger */
    private static final Logger logger = Logger.getLogger(EmailInHandler.class);
    /** User DAO */
    private UserDAO userDAO;

    // Extracted info
    private User user;
    private String destination;
    private String text;

    public EmailInHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void handle(Email email) {
        extractInfo(email);
        if (user == null) {
            logger.debug("Unable to retrieve user from email: " + email.getTo());
            return;
        }
        userDAO.incrementEmailInCount(user.getEmail());
        if (!user.isGvPassword()) {
            logger.debug("Non GV user attempted to send SMS via email");
            userDAO.incrementInvalidEmailCount(user.getEmail());
            return;
        }

        Notifier notifier = new Notifier(user, userDAO, null);
        try {
            notifier.sendSMS(destination, text);
        } catch (IOException e) {
            userDAO.incrementErrorCount(user.getEmail());
            logger.warn("Unable to send sms via email : " + e.getMessage());
        }

    }

    private void extractInfo(Email email) {
        String recipient = email.getTo().substring(0, email.getTo().indexOf('@'));

        // Get user
        if (recipient.indexOf('-') == -1) {
            return;
        }

        String pin = recipient.substring(0, recipient.indexOf('-'));
        user = userDAO.retrieveByPin(pin);
        if (user == null) {
            return;
        }

        // Get destination
        destination = recipient.substring(pin.length() + 1);
        if (destination.endsWith("-reply")) {
            destination = destination.substring(0, destination.lastIndexOf('-'));
        }

        text = EmailUtils.strip(email.getText());

    }

}
