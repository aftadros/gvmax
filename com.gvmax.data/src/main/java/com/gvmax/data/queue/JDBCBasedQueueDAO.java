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
package com.gvmax.data.queue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.gvmax.common.util.Enc;
import com.gvmax.common.util.JsonUtils;
import com.ryantenney.metrics.annotation.Counted;

/**
 * Implementation of QueueDAO that is JDBC based.
 * @param <T> Type object in queue
 */
public class JDBCBasedQueueDAO<T> implements QueueDAO<T> {
    private Class<?> clazz;
    private JdbcTemplate jdbcTemplate;
    private String tableName;
    private Enc enc;

    public JDBCBasedQueueDAO(Class<?> clazz, DataSource dataSource, String tableName, String encKey) {
        this.clazz = clazz;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.tableName = tableName;
        this.enc = new Enc(encKey);
    }

    @Override
    @Counted
    public String getName() {
        return tableName;
    }

    @Override
    @Timed @ExceptionMetered
    public long enqueue(T obj) throws IOException {
        final String payload = toJson(obj);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                PreparedStatement ps = conn.prepareStatement("insert into " + tableName + " ( enqueuedDate, payload ) values (?,?)", new String[] { "id" });
                ps.setLong(1, System.currentTimeMillis());
                ps.setString(2, payload);
                return ps;
            }
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    @Timed @ExceptionMetered
    public List<QueueEntry<T>> getEntries(long since, int max) throws IOException {
        return jdbcTemplate.query("select * from " + tableName + " where id > ? limit " + max, new Object[] { since }, new RowMapper<QueueEntry<T>>() {
            @Override
            public QueueEntry<T> mapRow(ResultSet rs, int row) throws SQLException {
                QueueEntry<T> entry = new QueueEntry<T>();
                entry.setId(rs.getLong("id"));
                entry.setEnqueuedDate(rs.getLong("enqueuedDate"));
                try {
                    entry.setPayload(fromJson(rs.getString("payload")));
                } catch (IOException e) {
                    throw new SQLException(e);
                }
                return entry;
            }
        });
    }

    @Override
    @Timed @ExceptionMetered
    public long size() throws IOException {
        // TODO: Review deprecated use
        return jdbcTemplate.queryForLong("select count(id) from " + tableName);
    }

    @Override
    @Timed @ExceptionMetered
    public int delete(Long... ids) throws IOException {
        String inIds = StringUtils.join(Arrays.asList(ids), ',');
        return jdbcTemplate.update("delete from " + tableName + " where id in (" + inIds + ")");
    }

    // -------------------
    // JSON
    // -------------------

    private String toJson(T obj) throws IOException {
        return enc.encrypt(JsonUtils.toJson(obj));
    }

    @SuppressWarnings("unchecked")
    private T fromJson(String json) throws IOException {
        return (T) JsonUtils.fromJson(enc.decrypt(json), clazz);
    }

}
