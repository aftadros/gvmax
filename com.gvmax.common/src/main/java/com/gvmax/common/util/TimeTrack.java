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
package com.gvmax.common.util;

import java.util.ArrayList;
import java.util.List;

public class TimeTrack {
    private List<TimeEntry> entries = new ArrayList<TimeEntry>();

    public long mark(String label) {
        TimeEntry entry = new TimeEntry();
        entry.label = label;
        entry.time = System.currentTimeMillis();
        entries.add(entry);
        return entry.time;
    }

    public long mark(String label, long time) {
        TimeEntry entry = new TimeEntry();
        entry.label = label;
        entry.time = time;
        entries.add(entry);
        return entry.time;
    }

    public Long delta(String from, String to) {
        TimeEntry fromEntry = getEntry(from);
        if (fromEntry == null) {
            return null;
        }
        TimeEntry toEntry = getEntry(to);
        if (toEntry == null) {
            return null;
        }
        return toEntry.time - fromEntry.time;
    }

    public Long delta(String label) {
        TimeEntry toEntry = getEntry(label);
        if (toEntry == null) {
            return null;
        }
        int index = entries.indexOf(toEntry);
        if (index == 0) {
            return 0L;
        }
        TimeEntry fromEntry = entries.get(index - 1);
        return delta(fromEntry.label, toEntry.label);
    }

    public Long elapsed(String label) {
        if (entries.size() == 0) {
            return null;
        }
        return delta(entries.get(0).label, label);
    }

    // ---------------------
    // STRING SECS
    // ---------------------

    public String deltaInSecs(String from, String to) {
        Long delta = delta(from, to);
        return millisToSecs(delta);
    }

    public String deltaInSecs(String to) {
        Long delta = delta(to);
        return millisToSecs(delta);
    }

    public String deltaInSecs() {
        if (entries.size() == 0) {
            return null;
        }
        return deltaInSecs(entries.get(entries.size() - 1).label);
    }

    public String elapsedInSecs(String label) {
        Long time = elapsed(label);
        return millisToSecs(time);
    }

    public String elapsedInSecs() {
        if (entries.size() == 0) {
            return null;
        }
        return elapsedInSecs(entries.get(entries.size() - 1).label);
    }

    private String millisToSecs(Long millis) {
        if (millis == null) {
            return null;
        }
        double secs = millis / 1000.0;
        double val = Math.round(secs * 100) / 100.0;
        return "" + val;
    }

    public TimeEntry getEntry(String label) {
        for (TimeEntry entry : entries) {
            if (entry.label.equals(label)) {
                return entry;
            }
        }
        return null;
    }

    public void clear() {
        entries.clear();
    }

    static class TimeEntry {
        private String label;
        private long time;
    }
}
