package com.sap.aii.mdt.web.statistic;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.labs.sap.ws.stats.StatsCollector;
import com.sap.aii.af.service.statistic.ws.ProfileProcessorBean;

@Weave
public abstract class PerformanceDataQueryServlet {
	
	public PerformanceDataQueryServlet() {
		StatsCollector.addProcessor(new ProfileProcessorBean());
	}
}
