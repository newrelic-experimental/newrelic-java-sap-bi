package com.sap.conn.jco.server;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.jco.NRJcoUtils;
import com.sap.conn.jco.JCoFunction;

@Weave(type = MatchType.Interface)
public abstract class JCoServerFunctionHandler {

	@Trace
	public void handleRequest(JCoServerContext var1, JCoFunction var2) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","JCoServerFunctionHandler",getClass().getSimpleName(),"handleRequest",var2.getName());
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		NRJcoUtils.addJcoServerContext(attributes, var1);
		NRJcoUtils.addAttribute(attributes, "JCoFunction", var2 != null ? var2.getName() : "Unknown");
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
}
