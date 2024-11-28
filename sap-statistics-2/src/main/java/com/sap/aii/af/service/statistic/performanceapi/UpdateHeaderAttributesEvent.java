package com.sap.aii.af.service.statistic.performanceapi;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.stats.MessageStats;
import com.sap.aii.af.service.statistic.IPerformanceCollectorData;
import com.sap.aii.af.service.statistic.PerformanceMonitorManager;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave
public abstract class UpdateHeaderAttributesEvent {

	private MessageKey messageKey = Weaver.callOriginal();
	
	@Trace
	public void execute() {
		Weaver.callOriginal();
		PerformanceMonitorManager performanceMonitorManager = PerformanceMonitorManager.getInstance();
		IPerformanceCollectorData iPerformanceCollectorData = performanceMonitorManager.getPerformanceCollectorData(this.messageKey);
		MessageStats.reportPerformanceCollectorData(iPerformanceCollectorData,"UpdateHeaderAttributesEvent");
	}
	
}
