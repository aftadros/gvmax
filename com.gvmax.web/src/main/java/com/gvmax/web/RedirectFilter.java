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
package com.gvmax.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.gvmax.common.util.MetricsUtil;

/**
 * Redirects traffic from one port to another.
 * Used to redirect HTTP to HTTPS
 */
public class RedirectFilter implements Filter {
    private static Logger logger = Logger.getLogger(RedirectFilter.class);
    private String baseRedirectURL;
    private int[] fromPorts;

    public RedirectFilter(String baseRedirectURL, String fromPorts) {
        this.baseRedirectURL = baseRedirectURL;
        String[] ports = fromPorts.split(",");
        this.fromPorts = new int[ports.length];
        for (int x = 0; x < ports.length; x++) {
            int port = Integer.parseInt(ports[x]);
            this.fromPorts[x] = port;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (isMatchingPort(request.getLocalPort())) {
            MetricsUtil.getCounter(RedirectFilter.class, "redirects").inc();
            logger.debug("REDIRECTING : " + req.getLocalName() + " PORT = " + request.getLocalPort());
            resp.sendRedirect(baseRedirectURL + req.getRequestURI());
            return;
        }
        chain.doFilter(request, response);
    }

    public boolean isMatchingPort(int port) {
        for (int fromPort : fromPorts) {
            if (port == fromPort) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void destroy() {
    }

}
