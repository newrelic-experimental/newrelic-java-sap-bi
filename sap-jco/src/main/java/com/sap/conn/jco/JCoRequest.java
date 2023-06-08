package com.sap.conn.jco;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.jco.NRJcoUtils;

@Weave(type = MatchType.Interface)
public abstract class JCoRequest {
	
	public abstract String getName();

	@Trace(dispatcher = true)
	public JCoResponse execute(JCoDestination var1) {
		HashMap<String, Object> attributes = new HashMap<>();
		NRJcoUtils.addJcoDestination(attributes, var1);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName(new String[] { "Custom", "JCoRequest", getClass().getSimpleName(), "execute", getName() });
		traced.addCustomAttributes(attributes);
		return (JCoResponse)Weaver.callOriginal();
	}
}
