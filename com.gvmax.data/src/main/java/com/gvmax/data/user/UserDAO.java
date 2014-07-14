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
package com.gvmax.data.user;

import java.util.List;

import com.gvmax.common.model.GlobalStats;
import com.gvmax.common.model.Stats;
import com.gvmax.common.model.User;

/**
 * User related DAO
 */
public interface UserDAO {
    boolean exists(String email);

    List<User> getUsers(int offset, int limit);

    User retrieve(String email);

    User retrieveByPin(String pin);

    User retrieveByGTalk(String gtalkEmail);

    void store(User user);

    void setPassword(String email, String password);

    void setGVFwdPhone(String email, String number, String type);

    void delete(String email);

    // Blacklist
    boolean isBlacklisted(String value);

    void blacklist(String value);

    // Stats
    Stats getStats(String email);

    GlobalStats getStats();

    void store(Stats stats);

    void incrementSMSInCount(String email);

    void incrementVMInCount(String email);

    void incrementMCInCount(String email);

    void incrementEmailInCount(String email);

    void incrementGTalkCount(String email);

    void incrementSMSOutCount(String email, int increment);

    void incrementApiCount(String email);

    void incrementErrorCount(String email);

    void incrementInvalidEmailCount(String email);

    void incrementFallbackCount(String email);

    void clearFallbackCount(String email);
}
