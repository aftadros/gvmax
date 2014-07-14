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
package com.gvmax.assembly;

import org.apache.log4j.Logger;

import com.gvmax.assembly.daemon.StandardDaemon;
import com.gvmax.server.ServerMain;
import com.gvmax.smtp.SMTPMain;
import com.gvmax.web.WebMain;

public class Main extends StandardDaemon {
    private static final Logger logger = Logger.getLogger(Main.class);

    @Override
    protected void serviceStart(String[] args) {
        if (args.length == 0) {
            logger.error("missing arguments");
            return;
        }
        String action = args[0];
        if ("server".equals(action)) {
            ServerMain.main(args);
        } else if ("web".equals(action)) {
            WebMain.main(args);
        } else if ("smtp".equals(action)) {
            SMTPMain.main(args);
        } else {
            logger.error("Invalid action : "+action);
        }
    }

    public static void main(String[] args) {
        try {
            new Main().process(args);
        } catch (Exception e) {
            logger.error(e);
        }
    }

}
