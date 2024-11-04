package com.sap.conn.jco.rt;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.server.JCoServerContext;

@Weave(type = MatchType.Interface)
abstract class ServerCallDispatcher {

	@Trace
	public void handleRequest(JCoServerContext var1, JCoFunction var2) {
		String functionName = var2.getName();
		if(functionName != null && !functionName.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","JCO","ServerCallDispatcher",getClass().getSimpleName(),"handleRequest",functionName);
		} else {
			NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","JCO","ServerCallDispatcher",getClass().getSimpleName(),"handleRequest");
		}
		Weaver.callOriginal();
	}
}
