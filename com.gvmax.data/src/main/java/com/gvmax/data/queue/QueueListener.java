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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import com.gvmax.data.queue.QueueDAO.QueueEntry;

/**
 * Listens on a queue and calls a listener when items arrive.
 * Can handle concurrent entries.
 * @param <T> Type of object in queue
 */
public class QueueListener<T> {
    private static Logger logger = Logger.getLogger(QueueListener.class);
    private QueueDAO<T> queue;
    private QueueProcessor<T> listener;
    private BlockingQueue<QueueEntry<T>> localQueue;
    private ExecutorService executor;

    public QueueListener(final QueueDAO<T> queue, int numWorkers, QueueProcessor<T> listener) {
        this.queue = queue;
        this.listener = listener;
        this.localQueue = new ArrayBlockingQueue<QueueEntry<T>>(numWorkers);
        this.executor = Executors.newFixedThreadPool(numWorkers, new ThreadFactory() {
            private int workerCount = 1;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, queue.getName() + "-W" + workerCount++);
            }
        });
    }

    public void start() {
        new QueueGrabber().start();
        new LocalQueueGrabber().start();
    }

    /**
     * In charge of pinging db for entries and placing any new entries in the
     * local queue.
     */
    class QueueGrabber extends Thread {
        private long since = 0;
        private int tries;

        public QueueGrabber() {
            super(queue.getName());
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (tries % 60 == 0) {
                        logger.debug("looking for entries; since=" + since);
                    }
                    List<QueueEntry<T>> entries = queue.getEntries(since, 10);
                    for (QueueEntry<T> entry : entries) {
                        localQueue.put(entry);
                    }
                    if (entries.size() == 0) {
                        Thread.sleep(1000);
                        tries += 1;
                    } else {
                        since = entries.get(entries.size() - 1).getId();
                        tries = 0;
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }

    }

    /**
     * In charge of grabbing entries from local queue and spanning workers.
     */
    class LocalQueueGrabber extends Thread {

        public LocalQueueGrabber() {
            super(queue.getName() + "-L");
        }

        @Override
        public void run() {
            while (true) {
                try {
                    QueueEntry<T> entry = localQueue.take();
                    executor.execute(new QueueWorker(entry));
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }

    }

    /**
     * In charge of processing entry
     */
    class QueueWorker implements Runnable {
        private QueueEntry<T> entry;

        public QueueWorker(QueueEntry<T> entry) {
            this.entry = entry;
        }

        @Override
        public void run() {
            // Execute
            try {
                listener.process(entry.getPayload());
            } catch (Exception e) {
                logger.error("Error",e);
            }
            // Delete
            try {
                queue.delete(entry.getId());
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    /**
     * In charge of processing queue entry.
     */
    public interface QueueProcessor<T> {
        void process(T entry);
    }

}
