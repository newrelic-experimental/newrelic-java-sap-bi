package com.newrelic.instrumentation.labs.sap.perf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.newrelic.agent.environment.AgentIdentity;
import com.newrelic.agent.environment.Environment;
import com.newrelic.agent.environment.EnvironmentService;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.NewRelic;
import com.sap.nwecm.rt.perf.MethodSummary;
import com.sap.nwecm.rt.perf.MethodType;
import com.sap.nwecm.rt.perf.PerformanceMonitor;

public class StatsCollector implements Runnable {

	public static boolean initialized = false;

	private static EnvironmentService environmentService = ServiceFactory.getEnvironmentService();
	private static Environment agentEnvironment = environmentService.getEnvironment();

	public static void addInstanceName(Map<String, Object> attributes) {
		AgentIdentity agentIdentity = agentEnvironment.getAgentIdentity();
		String instanceId = agentIdentity != null ? agentIdentity.getInstanceName() : null;
		if(instanceId != null && !instanceId.isEmpty()) {
			attributes.put("AgentInstanceId", instanceId);
		}
	}
	
	
	private static StatsCollector instance = null;
	public static StatsCollector getInstance() {
		if(instance == null) {
			instance = new StatsCollector();
		}
		return instance;
	}

	private StatsCollector() {

	}

	public static void initialize() {
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(getInstance(), 1, 1, TimeUnit.MINUTES);
		initialized = true;
		
	}

	@Override
	public void run() {
		
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		long start = System.currentTimeMillis();
		
		
		PerformanceMonitor monitor = PerformanceMonitor.getInstance();
		Map<String, MethodSummary> ecmsummaries = monitor.getECMRuntimeSummaries();
		Set<String> keys = ecmsummaries.keySet();
		attributes.put("ECMRuntimeSummaries", ecmsummaries != null ? ecmsummaries.size() : 0);
		for(String key : keys) {
			MethodSummary summary = ecmsummaries.get(key);
			if(summary != null) {
				reportMetrics("ECMMethodSummary",key, summary);
			}
		}

		Map<String, MethodSummary> summaries = monitor.getMethodSummaries();
		attributes.put("MethodSummaries", summaries != null ? summaries.size() : 0);
		keys = summaries.keySet();
		
		for(String key : keys) {
			MethodSummary summary = summaries.get(key);
			if(summary != null) {
				reportMetrics("MethodSummary",key, summary);
			}
		}
		long end = System.currentTimeMillis();
		attributes.put("Duration", end-start);
		addInstanceName(attributes);
		NewRelic.getAgent().getInsights().recordCustomEvent("PerformanceMonitor", attributes);
	}

	private void reportMetrics(String summaryType, String name, MethodSummary summary) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("SummaryType", summaryType);
		attributes.put("Name", name);
		
		MethodType type = summary.getType();
		attributes.put("MethodType", type.name());
		attributes.put("AverageTime", summary.getAverageTime());
		attributes.put("AverageTimeMs", summary.getAverageTimeMs());
		attributes.put("MaximumTime", summary.getMaximumTime());
		attributes.put("MaximumTimeMs", summary.getMaximumTimeMs());
		attributes.put("MinimumTime", summary.getMinimumTime());
		attributes.put("MinimumTimeMs", summary.getMinimumTimeMs());
		attributes.put("NumberOfCalls", summary.getNumberOfCalls());
		attributes.put("TotalTime", summary.getTotalTime());
		attributes.put("TotalTimeMs", summary.getTotalTimeMs());
		attributes.put("MethodInfo", summary.getMethodInfo());
		addInstanceName(attributes);
		NewRelic.getAgent().getInsights().recordCustomEvent("MethodSummary", attributes);
	}
	
}
