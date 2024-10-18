package com.sap.aii.af.service.statistic.impl;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.stats.DataQuery;
import com.sap.aii.af.service.statistic.IProfile;

@Weave
public abstract class XPIStatisticAggregator {

	private IProfile iProfile = Weaver.callOriginal();
	
	@Trace(dispatcher=true)
	public void start()  {
		if(!DataQuery.initialized) {
			DataQuery.init();
		}
		if(iProfile != null && iProfile.getName() != null) {
			NewRelic.getAgent().getTracedMethod().setMetricName("Custom","XPIStatisticAggregator","start",iProfile.getName());
		}
		Weaver.callOriginal();
	}
}
