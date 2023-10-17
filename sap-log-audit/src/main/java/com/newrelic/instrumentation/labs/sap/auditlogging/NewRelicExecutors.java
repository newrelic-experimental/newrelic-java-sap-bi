package com.newrelic.instrumentation.labs.sap.auditlogging;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NewRelicExecutors {

	
	private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
	
	public static void submit(Runnable r) {
		executorService.submit(r);
	}
	
	public static ScheduledFuture<?> submit(Runnable r, long period, long delay, TimeUnit tu) {
		return executorService.scheduleAtFixedRate(r, delay, period, tu);
	}
	
}
