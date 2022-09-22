package com.sap.conn.jco.rt;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRequest;
import com.sap.conn.jco.JCoResponse;

@Weave(type = MatchType.Interface)
public abstract class CallbackHandler {

	@Trace
	public void execute(JCoFunction function) {
		String classname = getClass().getSimpleName();
		if (classname != null && !classname.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().setMetricName(new String[] { "Custom", "CallbackHander", classname, "execute", "JCoFunction", function.getName() });
		} else {
			NewRelic.getAgent().getTracedMethod().setMetricName(new String[] { "Custom", "CallbackHander", "execute", "JCoFunction", function.getName() });
		} 
		Weaver.callOriginal();
	}

	@Trace
	public JCoResponse execute(JCoRequest request) {
		String classname = getClass().getSimpleName();
		if (classname != null && !classname.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().setMetricName(new String[] { "Custom", "CallbackHandler", classname, "execute", "JCoRequest", request.getName() });
		} else {
			NewRelic.getAgent().getTracedMethod().setMetricName(new String[] { "Custom", "CallbackHandler", "execute", "JCoRequest", request.getName() });
		} 
		return (JCoResponse)Weaver.callOriginal();
	}
}