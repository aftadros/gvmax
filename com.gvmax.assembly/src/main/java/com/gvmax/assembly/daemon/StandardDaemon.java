/*
 * The MIT License
 *
 * Copyright (c) 2009-, Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.gvmax.assembly.daemon;

import java.io.IOException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public abstract class StandardDaemon {
    private Daemon daemon;

    protected abstract void serviceStart(String[] args);

    protected void startService() throws IOException {
        daemon.startDaemon(JavaVMArguments.current());
    }

    protected void stopService() {
        daemon.stopDaemon();
    }

    public void process(String[] args) throws Exception {
        OptionParser parser = new OptionParser();
        parser.accepts("pid").withRequiredArg();
        parser.accepts("start");
        parser.accepts("stop");
        parser.accepts("restart");
        parser.accepts("status");
        parser.accepts("debugPort").withRequiredArg();
        OptionSet options = parser.parse(args);
        String pidFile = (String) options.valueOf("pid");
        if (pidFile == null) {
            throw new IllegalArgumentException("pid file not specified");
        }

        int debugPort = -1;
        if (options.hasArgument("debugPort")) {
            debugPort = Integer.parseInt((String) options.valueOf("debugPort"));
        }

        daemon = new Daemon(pidFile, debugPort);

        if (daemon.isDaemonized()) {
            daemon.initDaemon();
            serviceStart(args);
            return;
        }

        if (options.has("status")) {
            if (daemon.isRunning()) {
                System.out.println("running");
            } else {
                System.out.println("not running");
            }
            return;
        }

        if (options.has("start")) {
            boolean running = daemon.isRunning();
            if (running) {
                System.out.println("Already running");
            } else {
                startService();
            }
            return;
        }

        if (options.has("stop")) {
            stopService();
            return;
        }

        if (options.has("restart")) {
            stopService();
            startService();
        }

        System.out.println("No action specified");

    }

}
