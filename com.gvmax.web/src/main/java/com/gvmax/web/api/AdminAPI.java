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

import static com.gvmax.web.api.APIUtil.getUser;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.gvmax.common.model.User;
import com.gvmax.web.GVMaxServiceImpl;

@Controller
public class AdminAPI {
    private GVMaxServiceImpl service;
    private String adminAccount;

    public AdminAPI() {}

    @Autowired
    public AdminAPI(GVMaxServiceImpl service,  @Value("${web.adminAccount}") String adminAcccount) {
        this.service = service;
        this.adminAccount = adminAcccount;
    }

    @RequestMapping(value = "/stats", method = RequestMethod.GET)
    @Timed @ExceptionMetered
    public ModelMap stats(@RequestParam(value = "pin", required = true) String pin, HttpSession session, HttpServletResponse resp) {
        User user = getUser(service,session, pin);
        if (user != null && user.getEmail().equals(adminAccount)) {
            return new ModelMap("stats", service.getStats());
        }
        return null;
    }

    @RequestMapping(value = "/blacklist", method = RequestMethod.GET)
    @Timed @ExceptionMetered
    public ModelMap blacklist(@RequestParam(value = "pin", required = true) String pin, @RequestParam(value = "value", required = true) String value, HttpSession session, HttpServletResponse resp) {
        User user = getUser(service,session, pin);
        if (user != null && user.getEmail().equals(adminAccount)) {
            service.blacklist(value);
            return new ModelMap("status", "ok");
        }
        return null;
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @Timed @ExceptionMetered
    public ModelMap users(@RequestParam(value = "pin", required = true) String pin, @RequestParam(defaultValue="0", required = false) int offset, @RequestParam(defaultValue="10", required = false) int limit, HttpSession session){
        if (offset < 0) {
            offset = 0;
        }
        if (limit < 1) {
            limit = 10;
        }
        User user = getUser(service,session, pin);
        if (user != null && user.getEmail().equals(adminAccount)) {
            return new ModelMap("users",service.getUsers(offset,limit));
        }
        return null;
    }


}
