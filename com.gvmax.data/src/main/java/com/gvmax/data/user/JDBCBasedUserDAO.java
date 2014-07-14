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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.gvmax.common.model.GlobalStats;
import com.gvmax.common.model.Stats;
import com.gvmax.common.model.User;
import com.gvmax.common.util.Enc;
import com.gvmax.common.util.JsonUtils;

/**
 * Implementation of UserDAO that is JDBC based.
 */
public class JDBCBasedUserDAO implements UserDAO {
    private static final Logger logger = Logger.getLogger(JDBCBasedUserDAO.class);
    public static final String USER_TABLE = "users";
    public static final String STATS_TABLE = "stats";
    public static final String BLACKLIST_TABLE = "blacklist";
    private JdbcTemplate jdbcTemplate;
    private Enc enc;

    public JDBCBasedUserDAO(DataSource dataSource, String encKey) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.enc = new Enc(encKey);
    }

    @Override
    @Timed @ExceptionMetered
    public boolean exists(String email) {
        // TODO: Review deprecated use
        int count = jdbcTemplate.queryForInt("select count(0) from " + USER_TABLE + " where email = ?", enc(email));
        return count > 0;
    }

    @Override
    @Timed @ExceptionMetered
    public List<User> getUsers(int offset, int limit) {
        return jdbcTemplate.query("select user from "+USER_TABLE+" LIMIT ?,?",new Object[] { offset, limit }, new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                String userJson = rs.getString("user");
                return toUser(userJson);
            }
        });
    }

    @Override
    @Timed @ExceptionMetered
    public User retrieve(String email) {
        String userJson = queryForString("select user from " + USER_TABLE + " where email = ?", enc(email));
        return toUser(userJson);
    }

    @Override
    @Timed @ExceptionMetered
    public User retrieveByPin(String pin) {
        String userJson = queryForString("select user from " + USER_TABLE + " where pin = ?", enc(pin));
        return toUser(userJson);
    }

    @Override
    @Timed @ExceptionMetered
    public User retrieveByGTalk(String gTalkEmail) {
        String userJson = queryForString("select user from " + USER_TABLE + " where gTalkEmail = ?", enc(gTalkEmail));
        return toUser(userJson);
    }

    @Override
    @Timed @ExceptionMetered
    public void store(User user) {
        try {
            String userJson = JsonUtils.toJson(user);
            if (user.getEmail() != null) {
                user.setEmail(user.getEmail().trim().toLowerCase());
            }
            if (user.getgTalkEmail() != null) {
                user.setgTalkEmail(user.getgTalkEmail().trim().toLowerCase());
            }
            if (exists(user.getEmail())) {
                jdbcTemplate.update("update " + USER_TABLE + " set pin = ?, gTalkEmail = ?, user = ? where email = ?", enc(user.getPin()), enc(user.getgTalkEmail()), enc(userJson), enc(user.getEmail()));
            } else {
                jdbcTemplate.update("insert into " + USER_TABLE + " (email, pin, gTalkEmail,user) values (?,?,?,?)", enc(user.getEmail()), enc(user.getPin()), enc(user.getgTalkEmail()), enc(userJson));
                if (getStats(user.getEmail()) == null) {
                    store(new Stats(user.getEmail(), user.getPin()));
                }
            }
        } catch (IOException e) {
            logger.error("Unable to store user", e);
        }
    }

    @Override
    @Timed @ExceptionMetered
    public void setPassword(String email, String password) {
        User user = retrieve(email);
        user.setPassword(password);
        store(user);
    }

    @Override
    @Timed @ExceptionMetered
    public void setGVFwdPhone(String email, String number, String type) {
        User user = retrieve(email);
        user.setGvFwdPhone(number);
        user.setGvFwdPhoneType(type);
        store(user);
    }

    @Override
    @Timed @ExceptionMetered
    public void delete(String email) {
        jdbcTemplate.update("delete from " + USER_TABLE + " where email = ?", enc(email));
    }

    // -----------------
    // STATS
    // -----------------

    @Override
    @Timed @ExceptionMetered
    public GlobalStats getStats() {
        try {
            GlobalStats stats = jdbcTemplate.queryForObject("select " + "count(email) as statsCount," + "sum(smsInCount) as smsInCount," + "sum(smsOutCount) as smsOutCount," + "sum(vmInCount) as vmInCount," + "sum(mcInCount) as mcInCount," + "sum(emailInCount) as emailInCount," + "sum(gTalkCount) as gTalkCount," + "sum(apiCount) as apiCount," + "sum(errorCount) as errorCount," + "sum(invalidEmailCount) as invalidEmailCount," + "sum(fallbackCount) as fallbackCount " + "from " + STATS_TABLE, new RowMapper<GlobalStats>() {
                @Override
                public GlobalStats mapRow(ResultSet rs, int row) throws SQLException {
                    GlobalStats stats = new GlobalStats();
                    stats.setTimestamp(System.currentTimeMillis());
                    stats.setStatsCount(rs.getInt("statsCount"));
                    stats.setSmsInCount(rs.getInt("smsInCount"));
                    stats.setSmsOutCount(rs.getInt("smsOutCount"));
                    stats.setVmInCount(rs.getInt("vmInCount"));
                    stats.setMcInCount(rs.getInt("mcInCount"));
                    stats.setEmailInCount(rs.getInt("emailInCount"));
                    stats.setgTalkCount(rs.getInt("gTalkCount"));
                    stats.setApiCount(rs.getInt("apiCount"));
                    stats.setErrorCount(rs.getInt("errorCount"));
                    stats.setInvalidEmailCount(rs.getInt("invalidEmailCount"));
                    stats.setFallbackCount(rs.getInt("fallbackCount"));
                    return stats;
                }
            });
            // TODO: Review deprecated use
            int userCount = jdbcTemplate.queryForInt("select count(email) from " + USER_TABLE);
            stats.setUserCount(userCount);
            return stats;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    @Timed @ExceptionMetered
    public Stats getStats(final String email) {
        try {
            Stats stats = jdbcTemplate.queryForObject("select * from " + STATS_TABLE + " where email = ?", new Object[] { enc(email) }, new RowMapper<Stats>() {
                @Override
                public Stats mapRow(ResultSet rs, int row) throws SQLException {
                    Stats stats = new Stats(email, dec(rs.getString("pin")));
                    stats.setTimestamp(System.currentTimeMillis());
                    stats.setSmsInCount(rs.getInt("smsInCount"));
                    stats.setSmsOutCount(rs.getInt("smsOutCount"));
                    stats.setVmInCount(rs.getInt("vmInCount"));
                    stats.setMcInCount(rs.getInt("mcInCount"));
                    stats.setEmailInCount(rs.getInt("emailInCount"));
                    stats.setgTalkCount(rs.getInt("gTalkCount"));
                    stats.setApiCount(rs.getInt("apiCount"));
                    stats.setErrorCount(rs.getInt("errorCount"));
                    stats.setInvalidEmailCount(rs.getInt("invalidEmailCount"));
                    stats.setFallbackCount(rs.getInt("fallbackCount"));
                    return stats;
                }
            });
            return stats;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    @Timed @ExceptionMetered
    public void store(Stats stats) {
        jdbcTemplate.update("delete from " + STATS_TABLE + " where email = ?", stats.getEmail());
        jdbcTemplate.update("insert into " + STATS_TABLE + " " + "(email,pin,smsInCount,vmInCount,mcInCount,emailInCount,gTalkCount,smsOutCount,apiCount,errorCount,invalidEmailCount,fallbackCount) " + " values(?,?,?,?,?,?,?,?,?,?,?,?)", enc(stats.getEmail()), enc(stats.getPin()), stats.getSmsInCount(), stats.getVmInCount(), stats.getMcInCount(), stats.getEmailInCount(), stats.getgTalkCount(), stats.getSmsOutCount(), stats.getApiCount(), stats.getErrorCount(), stats.getInvalidEmailCount(), stats.getFallbackCount());
    }

    @Override
    @Timed @ExceptionMetered
    public void incrementSMSInCount(String email) {
        jdbcTemplate.update("update " + STATS_TABLE + " set smsInCount=smsInCount+1 WHERE email = ?", enc(email));
    }

    @Override
    @Timed @ExceptionMetered
    public void incrementVMInCount(String email) {
        jdbcTemplate.update("update " + STATS_TABLE + " set vmInCount=vmInCount+1 WHERE email = ?", enc(email));
    }

    @Override
    @Timed @ExceptionMetered
    public void incrementMCInCount(String email) {
        jdbcTemplate.update("update " + STATS_TABLE + " set mcInCount=mcInCount+1 WHERE email = ?", enc(email));
    }

    @Override
    @Timed @ExceptionMetered
    public void incrementEmailInCount(String email) {
        jdbcTemplate.update("update " + STATS_TABLE + " set emailInCount=emailInCount+1 WHERE email = ?", enc(email));
    }

    @Override
    @Timed @ExceptionMetered
    public void incrementGTalkCount(String email) {
        jdbcTemplate.update("update " + STATS_TABLE + " set gTalkCount=gTalkCount+1 WHERE email = ?", enc(email));
    }

    @Override
    @Timed @ExceptionMetered
    public void incrementApiCount(String email) {
        jdbcTemplate.update("update " + STATS_TABLE + " set apiCount=apiCount+1 WHERE email = ?", enc(email));
    }

    @Override
    @Timed @ExceptionMetered
    public void incrementSMSOutCount(String email, int increment) {
        jdbcTemplate.update("update " + STATS_TABLE + " set smsOutCount=smsOutCount+1 WHERE email = ?", enc(email));
    }

    @Override
    @Timed @ExceptionMetered
    public void incrementErrorCount(String email) {
        jdbcTemplate.update("update " + STATS_TABLE + " set errorCount=errorCount+1 WHERE email = ?", enc(email));
    }

    @Override
    @Timed @ExceptionMetered
    public void incrementFallbackCount(String email) {
        jdbcTemplate.update("update " + STATS_TABLE + " set fallbackCount=fallbackCount+1 WHERE email = ?", enc(email));
    }

    @Override
    @Timed @ExceptionMetered
    public void clearFallbackCount(String email) {
        jdbcTemplate.update("update " + STATS_TABLE + " set fallbackCount=0 WHERE email = ?", enc(email));
    }

    @Override
    @Timed @ExceptionMetered
    public void incrementInvalidEmailCount(String email) {
        jdbcTemplate.update("update " + STATS_TABLE + " set invalidEmailCount=invalidEmailCount+1 WHERE email = ?", enc(email));
    }

    // --------------
    // BLACKLIST
    // --------------

    @Override
    @Timed @ExceptionMetered
    public boolean isBlacklisted(String value) {
        return false;
    }

    @Override
    @Timed @ExceptionMetered
    public void blacklist(String value) {
    }

    // -----------------
    // UTILS
    // -----------------

    private String enc(String value) {
        return enc.encrypt(value);
    }

    private String dec(String value) {
        return enc.decrypt(value);
    }

    private User toUser(String json) {
        if (json == null) {
            return null;
        }
        json = dec(json);
        try {
            return JsonUtils.fromJson(json, User.class);
        } catch (IOException e) {
            logger.error("Unable to convert json", e);
            return null;
        }
    }

    private String queryForString(String sql, Object... params) {
        List<String> res = jdbcTemplate.query(sql, params, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int row) throws SQLException {
                return resultSet.getString(1);
            }
        });
        if (res.isEmpty()) {
            return null;
        }
        return res.get(0);
    }

}
