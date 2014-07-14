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
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class ServerMain {
    private static Logger logger = Logger.getLogger(ServerMain.class);

    private ServerMain() {}

    public static void main(String[] args) {
        logger.info("Starting server");
        try {
            new ClassPathXmlApplicationContext("/server-context.xml");
        } catch (Exception e) {
            logger.error(e);
        }
    }

}
