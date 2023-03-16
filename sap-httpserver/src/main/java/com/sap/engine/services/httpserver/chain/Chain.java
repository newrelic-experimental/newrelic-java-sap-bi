package com.sap.engine.services.httpserver.chain;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.Interface)
public abstract class Chain {

	@Trace
	public void process(HTTPRequest request, HTTPResponse response) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Chain",getClass().getSimpleName(),"process");
		Weaver.callOriginal();
	}
}
