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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.codahale.metrics.Timer.Context;
import com.gvmax.common.model.APIAction;
import com.gvmax.common.model.User;
import com.gvmax.common.util.MetricsUtil;
import com.gvmax.common.util.NetUtil;
import com.gvmax.common.util.TimeTrack;
import com.gvmax.data.queue.QueueListener.QueueProcessor;
import com.gvmax.data.user.UserDAO;
import com.gvmax.server.util.Notifier;

public class APIReceiver implements QueueProcessor<APIAction> {
    private static final Logger logger = Logger.getLogger(APIReceiver.class);
    private UserDAO userDAO;

    public APIReceiver(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void process(APIAction action) {
        MetricsUtil.getCounter(APIReceiver.class, "received").inc();
        Context timer = MetricsUtil.getTimer(APIReceiver.class, "processTime").time();
        TimeTrack tt = new TimeTrack();
        try {
            tt.mark("receive.time", action.getDate());
            tt.mark("process.time");
            User user = userDAO.retrieveByPin(action.getPin());
            if (user == null) {
                return;
            }
            if (APIAction.ACTIONS.SEND == action.getAction()) {
                doSend(user, action);
            }
        } finally {
            timer.stop();
            tt.mark("process.end");
            logger.info("ProcessTime: " + tt.deltaInSecs() + ", total: " + tt.elapsedInSecs());
        }
    }

    private void doSend(User user, APIAction action) {
        String error = null;
        try {
            Notifier notifier = new Notifier(user, userDAO, null);
            notifier.sendSMS(action.getNumber(), action.getText());
            userDAO.incrementApiCount(user.getEmail());
        } catch (IOException e) {
            error = e.getMessage();
            userDAO.incrementErrorCount(user.getEmail());
        }
        if (!StringUtils.isBlank(action.getCallback())) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("type", "smsSent");
            params.put("actionId", action.getId());
            if (error != null) {
                params.put("error", error);
            }
            try {
                String[] urls = action.getCallback().split(",");
                for (String url : urls) {
                    NetUtil.doPost(url.trim(), params);
                }
            } catch (IOException e) {
                userDAO.incrementErrorCount(user.getEmail());
            }
        }
    }

}
