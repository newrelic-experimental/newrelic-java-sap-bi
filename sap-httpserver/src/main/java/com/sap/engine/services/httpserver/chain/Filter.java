package com.sap.engine.services.httpserver.chain;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.Interface)
public abstract class Filter {

	@Trace
	public void process(HTTPRequest var1, HTTPResponse var2, Chain var3) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","HTTPServer","Filter",getClass().getSimpleName(),"process");
		Weaver.callOriginal();
	}
}
