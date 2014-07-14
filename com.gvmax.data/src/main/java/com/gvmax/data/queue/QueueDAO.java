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
import java.util.List;

/**
 * DAO used to deal with Queues
 * @param <T> Type of object contained in the queue.
 */
public interface QueueDAO<T> {

    String getName();

    long size() throws IOException;

    long enqueue(T obj) throws IOException;

    List<QueueEntry<T>> getEntries(long since, int max) throws IOException;

    int delete(Long... ids) throws IOException;

    /**
     * Holds a queue entry.
     * @param <T> Type of object
     */
    class QueueEntry<T> {
        private long id;
        private long enqueuedDate;
        private T payload;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getEnqueuedDate() {
            return enqueuedDate;
        }

        public void setEnqueuedDate(long enqueuedDate) {
            this.enqueuedDate = enqueuedDate;
        }

        public T getPayload() {
            return payload;
        }

        public void setPayload(T payload) {
            this.payload = payload;
        }
    }

}
