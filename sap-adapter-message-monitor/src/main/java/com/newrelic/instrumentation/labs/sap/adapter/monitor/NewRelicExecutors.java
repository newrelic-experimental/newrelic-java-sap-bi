package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;

public class NewRelicExecutors {

	// Reduced thread pool: only 2-4 threads needed for AttributeCheckers
	// Most work happens in consumer threads, this is just for startup/occasional tasks
	private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(50), new AdapterMonitorThreadFactory());

	// Reduced scheduled pool: only 2 threads needed for periodic housekeeping tasks
	// Handles: queue metrics (10s), cache cleanup (60s), thread metrics (2min)
	private static final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
	
	public static Future<?> addRunnableToThreadPool(Runnable runnable) {
		AdapterMonitorLogger.logMessage(Level.FINE,"Adding Runnable to ThreadPool " + runnable.hashCode());
		Future<?> f = threadPoolExecutor.submit(runnable);
		return f;
	}
	
	public static Future<?> addScheduledTask(Runnable task, long delay, TimeUnit timeUnit) {
		AdapterMonitorLogger.logMessage("Scheduling task to run after " + delay + " " + timeUnit.toString());
		Future<?> f = scheduledExecutor.schedule(task, delay, timeUnit);
		return f;
	}
	
	public static Future<?> addScheduledTaskAtFixedRate(Runnable task, long delay, long freq, TimeUnit timeUnit) {
		Future<?> f = scheduledExecutor.scheduleAtFixedRate(task, delay, freq, timeUnit);
		return f;
	}
	
	static {
		addScheduledTaskAtFixedRate(() -> {
			NewRelic.recordMetric("SAP/AttributeProcess/ThreadPoolExecutor/ActiveCount", threadPoolExecutor.getActiveCount());
			NewRelic.recordMetric("SAP/AttributeProcess/ScheduledExecutor/ActiveCount", ((ScheduledThreadPoolExecutor)scheduledExecutor).getActiveCount());
		}, 2, 2, TimeUnit.MINUTES);
	}
}
