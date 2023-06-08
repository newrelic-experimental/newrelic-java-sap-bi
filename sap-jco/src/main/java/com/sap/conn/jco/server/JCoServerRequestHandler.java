package com.sap.conn.jco.server;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.jco.NRJcoUtils;
import com.sap.conn.jco.JCoRequest;
import com.sap.conn.jco.JCoResponse;

@Weave(type = MatchType.Interface)
public abstract class JCoServerRequestHandler {
	
	@Trace
	public void handleRequest(JCoServerContext context, JCoRequest request, JCoResponse response) {
		NewRelic.getAgent().getTracedMethod().setMetricName(new String[] { "Custom", "JCoServerRequestHandler", getClass().getSimpleName(), "handleRequest", request.getName() });
		HashMap<String, Object> attributes = new HashMap<>();
		NRJcoUtils.addJcoServerContext(attributes, context);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
	
}
