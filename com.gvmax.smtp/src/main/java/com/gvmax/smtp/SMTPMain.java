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
package com.gvmax.smtp;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class SMTPMain {
    private static final Logger logger = Logger.getLogger(SMTPMain.class);

    private SMTPMain() {}

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        logger.info("Starting SMTP Server");
        new ClassPathXmlApplicationContext("/server-smtp-context.xml");
    }
}
