package com.newrelic.instrumentation.labs.sap.systemstatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.newrelic.agent.environment.AgentIdentity;
import com.newrelic.agent.environment.Environment;
import com.newrelic.agent.environment.EnvironmentService;
import com.newrelic.agent.service.ServiceFactory;
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
	
	private static EnvironmentService environmentService = ServiceFactory.getEnvironmentService();
	private static Environment agentEnvironment = environmentService.getEnvironment();

	public static void addInstanceName(Map<String, Object> attributes) {
		AgentIdentity agentIdentity = agentEnvironment.getAgentIdentity();
		String instanceId = agentIdentity != null ? agentIdentity.getInstanceName() : null;
		reportValue(attributes, "Agent-InstanceName", instanceId);
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
		addInstanceName(attributes);
		NewRelic.getAgent().getInsights().recordCustomEvent("QueueStatus", attributes);
	}

	private static void reportValue(Map<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}
}
