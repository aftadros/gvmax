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

import static com.codahale.metrics.MetricRegistry.name;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;

import com.codahale.metrics.Counter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.log4j.InstrumentedAppender;

/**
 * Metrics related utilities.
 */
public final class MetricsUtil {
    private static MetricRegistry registry = new MetricRegistry();
    /** Created timers */
    private static Map<String, Timer> timers = new HashMap<String, Timer>();
    /** Created counters */
    private static Map<String, Counter> counters = new HashMap<String, Counter>();
    /** Created meters */
    private static Map<String, Meter> meters = new HashMap<String, Meter>();

    static {
        registry.register(MetricRegistry.name("jvm", "gc"), new GarbageCollectorMetricSet());
        registry.register(MetricRegistry.name("jvm", "memory"), new MemoryUsageGaugeSet());
        registry.register(MetricRegistry.name("jvm", "thread-states"), new ThreadStatesGaugeSet());
        registry.register(MetricRegistry.name("jvm", "fd", "usage"), new FileDescriptorRatioGauge());

        InstrumentedAppender appender = new InstrumentedAppender(registry);
        appender.activateOptions();
        LogManager.getRootLogger().addAppender(appender);

        JmxReporter.forRegistry(registry).inDomain("metrics.gvmax").filter(new MetricFilter() {
            @Override
            public boolean matches(String name, Metric metric) {
                return name.contains("gvmax");
            }
        }).build().start();
        JmxReporter.forRegistry(registry).inDomain("metrics.jvm").filter(new MetricFilter() {
            @Override
            public boolean matches(String name, Metric metric) {
                return name.startsWith("jvm.");
            }
        }).build().start();
        JmxReporter.forRegistry(registry).inDomain("metrics.jetty").filter(new MetricFilter() {
            @Override
            public boolean matches(String name, Metric metric) {
                return name.contains("jetty");
            }
        }).build().start();
        JmxReporter.forRegistry(registry).inDomain("metrics.log4j").filter(new MetricFilter() {
            @Override
            public boolean matches(String name, Metric metric) {
                return name.contains("log4j");
            }
        }).build().start();
        JmxReporter.forRegistry(registry).inDomain("metrics.all").build().start();
    }

    private MetricsUtil() {}

    public static MetricRegistry getRegistry() { return registry; }

    // ------------------
    // TIMERS
    // ------------------

    public static Timer getTimer(Class<?> clazz,String name) {
        return getTimer(clazz.getCanonicalName(), name);
    }

    public static Timer getTimer(String className, String name) {
        synchronized (timers) {
            String key = name(className,name);
            Timer timer = timers.get(key);
            if (timer == null) {
                timer = registry.timer(key);
                timers.put(key, timer);
            }
            return timer;
        }
    }

    // -------------
    // COUNTERS
    // -------------

    public static Counter getCounter(Class<?> clazz, String name) {
        return getCounter(clazz.getCanonicalName(),name);
    }

    public static Counter getCounter(String className, String name) {
        synchronized (counters) {
            String key = name(className,name);
            Counter counter = counters.get(key);
            if (counter == null) {
                counter = registry.counter(key);
                counters.put(key, counter);
            }
            return counter;
        }
    }

    // -----------------------
    // Meters
    // -----------------------

    public static Meter getMeter(Class<?> clazz, String name) {
        return getMeter(clazz.getCanonicalName(), name);
    }

    public static Meter getMeter(String className, String name) {
        synchronized (meters) {
            String key = name(className,name);
            Meter meter = meters.get(key);
            if (meter == null) {
                meter = registry.meter(key);
                meters.put(key, meter);
            }
            return meter;
        }
    }

}
