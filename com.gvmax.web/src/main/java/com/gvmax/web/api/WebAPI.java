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
package com.gvmax.web.api;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.gvmax.common.model.User;
import com.gvmax.web.GVMaxServiceImpl;

/**
 * Web API
 */
@Controller
public class WebAPI {
    /** Reference to GVMax service */
    private GVMaxServiceImpl service;

    public WebAPI() {}

    @Autowired
    public WebAPI(GVMaxServiceImpl service) {
        this.service = service;
    }

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    @Timed @ExceptionMetered
    public ModelMap send(@RequestParam(value = "pin", required = false) String pin, @RequestParam(value = "number") String number, @RequestParam(value = "text") String text, @RequestParam(value = "callbackUrl", required = false) String callbackUrl, HttpSession session, HttpServletResponse resp) {
        try {
            User user = APIUtil.getUser(service,session, pin);
            if (user == null) {
                APIUtil.invalidCredentials(resp);
                return null;
            }
            String id = service.sendSMS(user.getPin(), number, text, callbackUrl);
            return new ModelMap("id", id);
        } catch (DataAccessException e) {
            APIUtil.internalError(e, resp);
        } catch (IOException e) {
            APIUtil.internalError(e, resp);
        }
        return null;
    }

}
