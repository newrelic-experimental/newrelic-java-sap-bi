package com.sap.aii.af.lib.scheduler;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type=MatchType.Interface)
public abstract class Task {

	
	@Trace
	public void invoke() {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Task",getClass().getSimpleName(),"invoke");
		Weaver.callOriginal();
	}

	
}
