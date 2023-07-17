package com.sap.conn.jco.server;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.conn.jco.JCoFunction;

@Weave(type = MatchType.Interface)
public abstract class JCoServerFunctionHandler {

	@Trace
	public void handleRequest(JCoServerContext var1, JCoFunction var2) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","JCoServerFunctionHandler",getClass().getSimpleName(),"handleRequest",var2.getName());
		Weaver.callOriginal();
	}
}
