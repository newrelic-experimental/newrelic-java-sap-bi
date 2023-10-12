package com.sap.nwecm.rt.perf;

import java.util.Map;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.perf.StatsCollector;

@Weave
public abstract class PerformanceMonitor {
	
	public static synchronized PerformanceMonitor getInstance() {
		return Weaver.callOriginal();
	}

	private PerformanceMonitor() {
		if(!StatsCollector.initialized) {
			StatsCollector.initialize();
		}
	}
	
	public abstract boolean isEnabled();
	
	public abstract Map<String, MethodSummary> getECMRuntimeSummaries();
	
	public abstract Map<String, MethodSummary> getMethodSummaries();
	
	public abstract void generateTestData();
	
	@Trace(dispatcher=true)
	public void startAPI(String methodInfo) {
		Weaver.callOriginal();
	}
	
	@Trace(dispatcher=true)
	public void startSPI(String methodInfo) {
		Weaver.callOriginal();
	}
}
