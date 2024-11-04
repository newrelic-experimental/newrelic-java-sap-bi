package com.sap.conn.jco.server;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.conn.jco.JCoRequest;
import com.sap.conn.jco.JCoResponse;

@Weave(type = MatchType.Interface)
public abstract class JCoServerRequestHandler {
	
	@Trace(dispatcher = true)
	public void handleRequest(JCoServerContext context, JCoRequest request, JCoResponse response) {
		NewRelic.getAgent().getTracedMethod().setMetricName(new String[] { "Custom", "JCoServerRequestHandler", getClass().getSimpleName(), "handleRequest", request.getName() });
		Weaver.callOriginal();
	}
	
}
