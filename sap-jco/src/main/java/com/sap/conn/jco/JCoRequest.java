package com.sap.conn.jco;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.Interface)
public abstract class JCoRequest {
	
	public abstract String getName();

	@Trace(dispatcher = true)
	public JCoResponse execute(JCoDestination var1) {
		NewRelic.getAgent().getTracedMethod().setMetricName(new String[] { "Custom", "JCoRequest", getClass().getSimpleName(), "execute", getName() });
		return (JCoResponse)Weaver.callOriginal();
	}
}
