package com.sap.aii.af.service.statistic.impl;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.service.statistic.IProfile;

@Weave
public class XPIPerformanceStatisticAggregator {
	
	private IProfile iProfile = Weaver.callOriginal();
	
	@Trace(dispatcher=true)
	public void start() {
		if(iProfile != null && iProfile.getName() != null) {
			NewRelic.getAgent().getTracedMethod().setMetricName("Custom","XPIPerformanceStatisticAggregator","start",iProfile.getName());
		}
		Weaver.callOriginal();
	}
}
