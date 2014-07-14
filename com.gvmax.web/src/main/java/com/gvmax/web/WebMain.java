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

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.core.io.ClassPathResource;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty8.InstrumentedHandler;
import com.codahale.metrics.jetty8.InstrumentedQueuedThreadPool;
import com.codahale.metrics.servlets.MetricsServlet;
import com.gvmax.common.util.MetricsUtil;

/**
 * Web App entry point.
 */
public final class WebMain {
    private static Logger logger = Logger.getLogger(WebMain.class);

    private WebMain() {}

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        try {
            MetricRegistry registry = MetricsUtil.getRegistry();
            Properties props = new Properties();
            props.load(new ClassPathResource("/web.properties").getInputStream());

            int httpPort = Integer.parseInt(props.getProperty("web.http.port", "19080"));
            int httpsPort = Integer.parseInt(props.getProperty("web.https.port", "19443"));
            logger.info("Starting server: " + httpPort + " :: " + httpsPort);

            Server server = new Server(httpPort);
            ThreadPool threadPool = new InstrumentedQueuedThreadPool(registry);
            server.setThreadPool(threadPool);

            // Setup HTTPS
            if (new File("gvmax.jks").exists()) {
                SslSocketConnector connector = new SslSocketConnector();
                connector.setPort(httpsPort);
                connector.setKeyPassword(props.getProperty("web.keystore.password"));
                connector.setKeystore("gvmax.jks");
                server.addConnector(connector);
            } else {
                logger.warn("keystore gvmax.jks not found, ssl disabled");
            }

            // Setup WEBAPP
            URL warUrl = WebMain.class.getClassLoader().getResource("webapp");
            String warUrlString = warUrl.toExternalForm();
            WebAppContext ctx = new WebAppContext(warUrlString,"/");
            ctx.setAttribute(MetricsServlet.METRICS_REGISTRY,registry);
            InstrumentedHandler handler = new InstrumentedHandler(registry,ctx);
            server.setHandler(handler);
            server.start();
            server.join();
        } catch (Exception e) {
            logger.error(e);
            System.exit(0);
        }
    }

}
