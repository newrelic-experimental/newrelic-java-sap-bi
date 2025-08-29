package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class AdapterMonitorThreadFactory implements ThreadFactory {
	
	private static final String MONITOR_GROUP = "Newrelic_AdapterMonitor";
	private static final String THREAD_PREFIX = "AdapterMonitor";
	
	private AtomicInteger counter;
	private ThreadGroup thread_group = new ThreadGroup(MONITOR_GROUP);
	
	protected AdapterMonitorThreadFactory() {
		counter = new AtomicInteger(0);
		
	}

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(thread_group, r, THREAD_PREFIX + counter.getAndIncrement());
	}

}
