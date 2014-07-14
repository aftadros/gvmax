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
package com.gvmax.server;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codahale.metrics.Timer.Context;
import com.gvmax.common.model.Email;
import com.gvmax.common.relay.GVMaxRelay;
import com.gvmax.common.util.MetricsUtil;
import com.gvmax.common.util.TimeTrack;
import com.gvmax.data.queue.QueueListener.QueueProcessor;
import com.gvmax.data.user.UserDAO;
import com.gvmax.server.handlers.ConfirmationHandler;
import com.gvmax.server.handlers.EmailInHandler;
import com.gvmax.server.handlers.MissedCallHandler;
import com.gvmax.server.handlers.SMSInHandler;
import com.gvmax.server.handlers.VoicemailHandler;

@Service
public class SMTPReceiver implements QueueProcessor<Email> {
    private static final Logger logger = Logger.getLogger(SMTPReceiver.class);

    private enum TYPE {
        SMS, VOICEMAIL, MISSEDCALL, EMAIL_IN, CONFIRMATION
    };

    private UserDAO userDAO;
    private GVMaxRelay relay;

    @Autowired
    public SMTPReceiver(UserDAO userDAO, GVMaxRelay relay) {
        this.userDAO = userDAO;
        this.relay = relay;
        logger.info("ready to process emails.");
    }

    @Override
    public void process(Email email) {
        MetricsUtil.getCounter(SMTPReceiver.class, "received").inc();
        if (email == null) {
            MetricsUtil.getCounter(SMTPReceiver.class, "null").inc();
            return;
        }
        Context timer = MetricsUtil.getTimer(SMTPReceiver.class, "processTime").time();
        try {
            TimeTrack tt = new TimeTrack();
            tt.mark("email.time", email.getTimestamp());
            tt.mark("process.start");

            TYPE emailType = getEmailType(email);
            switch (emailType) {
            case SMS:
                new SMSInHandler(userDAO, relay).handle(email);
                break;
            case VOICEMAIL:
                new VoicemailHandler(userDAO, relay).handle(email);
                break;
            case MISSEDCALL:
                new MissedCallHandler(userDAO, relay).handle(email);
                break;
            case CONFIRMATION:
                new ConfirmationHandler(relay).handle(email);
                break;
            case EMAIL_IN:
                new EmailInHandler(userDAO).handle(email);
                break;
            default:
                break;
            }

            tt.mark("process.end");
            logger.info(emailType + " : ProcessTime: " + tt.deltaInSecs() + ", total: " + tt.elapsedInSecs());
        } finally {
            timer.stop();
        }
    }

    private TYPE getEmailType(Email email) {
        if (email.getOriginalFrom() != null) {
            if (email.getOriginalFrom().contains("txt.voice.google.com")) {
                return TYPE.SMS;
            } else if (email.getOriginalFrom().contains("voice-noreply@google.com")) {
                if (email.getSubject().contains("missed call")) {
                    return TYPE.MISSEDCALL;
                }
                return TYPE.VOICEMAIL;
            }
        }
        if (email.getSubject() != null && email.getSubject().contains("Confirmation") && email.getText() != null && email.getText().contains("mail.google.com")) {
            return TYPE.CONFIRMATION;
        } else {
            return TYPE.EMAIL_IN;
        }
    }

}
