package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.newrelic.api.agent.NewRelic;

public class NewRelicExecutors {

	private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 30, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(50), new AdapterMonitorThreadFactory());
	private static final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(5);
	
	public static Future<?> addRunnableToThreadPool(Runnable runnable) {
		Future<?> f = threadPoolExecutor.submit(runnable);
		return f;
	}
	
	public static Future<?> addScheduledTask(Runnable task, long delay, TimeUnit timeUnit) {
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
