/*
 * The MIT License
 *
 * Copyright (c) 2009-, Sun Microsystems, Inc., CloudBees, Inc.
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

import static com.gvmax.assembly.daemon.CLibrary.LIBC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gvmax.common.util.IOUtil;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.StringArray;

public class Daemon {
    private String pidFile;
    private int debugPort = -1;
    private static final Logger LOGGER = Logger.getLogger(Daemon.class.getName());

    public Daemon(String pidFile, int debugPort) {
        this.pidFile = pidFile;
        this.debugPort = debugPort;
    }

    public void startDaemon() throws IOException {
        startDaemon(JavaVMArguments.current());
    }

    public void startDaemon(JavaVMArguments args) {
        if (isDaemonized()) {
            throw new IllegalStateException("Already running as a daemon");
        }

        args.setSystemProperty(Daemon.class.getName(), "daemonized");
        if (debugPort != -1) {
            args.add(1, "-agentlib:jdwp=transport=dt_socket,address=" + debugPort + ",server=y,suspend=n");
        }

        // prepare for a fork
        String exe = getCurrentExecutable();
        StringArray sa = args.toStringArray();

        int i = LIBC.fork();
        if (i < 0) {
            LIBC.perror("initial fork failed");
            System.exit(-1);
        }
        if (i == 0) {
            // with fork, we lose all the other critical threads, to exec to
            // Java again
            LIBC.execv(exe, sa);
            System.err.println("exec failed");
            LIBC.perror("initial exec failed");
            System.exit(-1);
        }

        // parent exits
    }

    public boolean isDaemonized() {
        return System.getProperty(Daemon.class.getName()) != null;
    }

    public boolean isRunning() {
        return getDaemonPid() != null;
    }

    public boolean stopDaemon() {
        Integer pid = getDaemonPid();
        if (pid != null) {
            LIBC.kill(pid, 2);
            return true;
        }
        return false;
    }

    public void restartDaemon() throws IOException {
        stopDaemon();
        startDaemon();
    }

    public void initDaemon() throws Exception {
        LIBC.setsid();
        closeDescriptors();
        if (pidFile != null) {
            writePidFile();
        }
    }

    protected void closeDescriptors() throws IOException {
        if (!Boolean.getBoolean(Daemon.class.getName() + ".keepDescriptors")) {
            System.out.close();
            System.err.close();
            System.in.close();
        }
    }

    public Integer getDaemonPid() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(pidFile));
            int pid = Integer.parseInt(reader.readLine());
            return pid;
        } catch (Exception e) {
            return null;
        } finally {
            IOUtil.close(reader);
        }
    }

    protected void writePidFile() throws IOException {
        FileWriter fw = null;
        try {
            File file = new File(pidFile);
            file.deleteOnExit();
            fw = new FileWriter(file);
            fw.write(String.valueOf(LIBC.getpid()));
            fw.close();
        } catch (IOException e) {
            // if failed to write, keep going because maybe we are run from
            // non-root
            e.printStackTrace();
        } finally {
            IOUtil.close(fw);
        }
    }

    public static String getCurrentExecutable() {
        int pid = LIBC.getpid();
        String name = "/proc/" + pid + "/exe";
        File exe = new File(name);
        if (exe.exists()) {
            try {
                String path = resolveSymlink(exe);
                if (path != null) {
                    return path;
                }
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Failed to resolve symlink " + exe, e);
            }
            return name;
        }

        // cross-platform fallback
        return System.getProperty("java.home") + "/bin/java";
    }

    private static String resolveSymlink(File link) throws IOException {
        String filename = link.getAbsolutePath();

        for (int sz = 512; sz < 65536; sz *= 2) {
            Memory m = new Memory(sz);
            int r = LIBC.readlink(filename, m, new NativeLong(sz));
            if (r < 0) {
                int err = Native.getLastError();
                if (err == 22/* EINVAL --- but is this really portable? */) {
                    return null; // this means it's not a symlink
                }
                throw new IOException("Failed to readlink " + link + " error=" + err + " " + LIBC.strerror(err));
            }

            if (r == sz) {
                continue; // buffer too small
            }

            byte[] buf = new byte[r];
            m.read(0, buf, 0, r);
            return new String(buf);
        }

        throw new IOException("Failed to readlink " + link);
    }

}
