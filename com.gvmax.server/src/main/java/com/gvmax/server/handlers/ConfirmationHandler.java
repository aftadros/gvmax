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
import com.gvmax.common.relay.GVMaxRelay;
import com.gvmax.common.util.NetUtil;

public class ConfirmationHandler {
    private static final Logger logger = Logger.getLogger(ConfirmationHandler.class);
    private static final String SUBJECT = "GVMax is sending you a copy of Google's confirm email, in case auto confirm did not work.";
    private GVMaxRelay relay;

    public ConfirmationHandler(GVMaxRelay relay) {
        this.relay = relay;
    }

    public void handle(Email email) {
        String emailText = email.getText();

        // Send copy of email to user
        try {
            relay.sendEmail(relay.getEmailSender(), relay.getEmailSender(), email.getFrom(), SUBJECT, emailText);
        } catch (IOException e) {
            logger.error("Unable to send confirmation copy. :" + e.getMessage());
        }

        // Extract link
        String[] links = { "https://isolated.mail.google.com", "https://mail.google.com" };
        for (String tlink : links) {
            int index = emailText.indexOf(tlink);
            if (index != -1) {
                String link = emailText.substring(emailText.indexOf(tlink));
                index = link.indexOf('\n');
                if (index != -1) {
                    link = link.substring(0, link.indexOf('\n'));

                    // Confirm
                    try {
                        NetUtil.doGet(link);
                        logger.debug("gmail confirmation confirmed");
                        break;
                    } catch (Exception e) {
                        logger.warn("unable to confirm gmail : " + e.getMessage());
                    }
                }
            }
        }

    }

}
