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
package com.gvmax.common.model;

/**
 * Encapsulates global (all users) stats.
 */
public class GlobalStats extends Stats {
    /** Used for Java serialization */
    private static final long serialVersionUID = 1L;
    /** Number of active users */
    private int userCount;
    /** Number of stats users (all users ever registered) */
    private int statsCount;

    public GlobalStats() {}

    // -------------------
    // GETTERS AND SETTERS
    // -------------------

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public int getStatsCount() {
        return statsCount;
    }

    public void setStatsCount(int statsCount) {
        this.statsCount = statsCount;
    }
}
