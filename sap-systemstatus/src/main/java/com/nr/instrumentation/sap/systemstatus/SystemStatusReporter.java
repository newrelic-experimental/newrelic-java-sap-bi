package com.nr.instrumentation.sap.systemstatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.newrelic.api.agent.NewRelic;
import com.sap.engine.interfaces.messaging.api.systemstatus.QueueStatus;
import com.sap.engine.interfaces.messaging.api.systemstatus.SystemStatus;
import com.sap.engine.messaging.runtime.MessagingSystem;

public class SystemStatusReporter implements Runnable {
	
	public static boolean initialized = false;
	
	public static void init() {
		initialized = true;
		SystemStatusReporter reporter = new SystemStatusReporter();
		
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(reporter, 1, 3, TimeUnit.MINUTES);
	}

	@Override
	public void run() {
		SystemStatus systemStatus = MessagingSystem.getSystemStatus();
		
		QueueStatus[] queueStatuses = systemStatus.getQueueStatus();
		if(queueStatuses != null) {
			for(QueueStatus queueStatus : queueStatuses) {
				reportQueueStatus(queueStatus);
			}
		}
	}
	
	private static void reportQueueStatus(QueueStatus queueStatus) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		
		reportValue(attributes,"ConnectionName", queueStatus.getConnectionName());
		reportValue(attributes,"CurrentWorkerThreads", queueStatus.getCurrentWorkerThreads());
		reportValue(attributes,"CurrentWorkingThreads", queueStatus.getCurrentWorkingThreads());
		reportValue(attributes,"QueueName", queueStatus.getQueueName());
		reportValue(attributes,"MaxWorkerThreads", queueStatus.getMaxWorkerThreads());
		reportValue(attributes,"QueueSize", queueStatus.getQueueSize());
		reportValue(attributes,"ThreadPoolSize", queueStatus.getThreadPoolSize());
		NewRelic.getAgent().getInsights().recordCustomEvent("QueueStatus", attributes);
	}

	private static void reportValue(Map<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}
}
