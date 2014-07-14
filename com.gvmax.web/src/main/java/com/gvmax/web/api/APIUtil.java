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

import org.apache.log4j.Logger;

import com.gvmax.common.model.User;
import com.gvmax.common.util.MiscUtils;
import com.gvmax.web.GVMaxServiceImpl;

public final class APIUtil {
    private static final Logger logger = Logger.getLogger(APIUtil.class);

    private APIUtil() {}

    public static User getUser(GVMaxServiceImpl service, HttpSession session, String pin) {
        return service.getUser((String) session.getAttribute("email"), pin);
    }

    public static void invalidCredentials(HttpServletResponse resp) {
        logger.warn("Invalid credentials");
        sendError(403, "Invalid credentials", resp);
    }

    public static void internalError(Exception e, HttpServletResponse resp) {
        logger.warn("Internal Error: " + e.getMessage());
        sendError(500, "Internal Error : " + e.getMessage(), resp);
    }

    public static void sendError(int code, String error, HttpServletResponse resp) {
        try {
            resp.getWriter().println(error);
            resp.setStatus(code);
        } catch (IOException e) {
            MiscUtils.emptyBlock();
        }
    }

}
