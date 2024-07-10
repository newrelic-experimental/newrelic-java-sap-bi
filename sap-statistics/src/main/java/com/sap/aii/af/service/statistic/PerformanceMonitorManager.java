package com.sap.aii.af.service.statistic;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.stats.MessageStats;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave
public abstract class PerformanceMonitorManager {
	
	public static PerformanceMonitorManager getInstance() {
		return Weaver.callOriginal();
	}
	
	public abstract IPerformanceCollectorData getPerformanceCollectorData(MessageKey messageKey);
	
	@Trace
	public void stop(MessageKey messageKey) {
		NewRelic.incrementCounter("Custom/PerformanceMonitorManager/stop/Calls");
		Weaver.callOriginal();
	}
	
	@Trace
	public void cancel(MessageKey messageKey) {
		NewRelic.incrementCounter("Custom/PerformanceMonitorManager/cancel/Calls");
		Weaver.callOriginal();
	}
	
	@Trace
	private void updateAccumulationCache(IPerformanceCollectorData iPerformanceCollectorData) {
		NewRelic.incrementCounter("Custom/PerformanceMonitorManager/updateAccumulationCache/Calls");
		Weaver.callOriginal();
		MessageStats.reportPerformanceCollectorData(iPerformanceCollectorData,"PerformanceMonitorManager-updateAccumulationCache");
	}
}
