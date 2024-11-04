package com.sap.conn.jco.rt;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.conn.jco.JCoFunction;

@Weave(type = MatchType.Interface)
abstract class CallbackHandler {

	@Trace(dispatcher = true)
	public void execute(JCoFunction function) {
		String classname = getClass().getSimpleName();
		if (classname != null && !classname.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().setMetricName(new String[] { "Custom", "CallbackHander", classname, "execute", "JCoFunction", function.getName() });
		} else {
			NewRelic.getAgent().getTracedMethod().setMetricName(new String[] { "Custom", "CallbackHander", "execute", "JCoFunction", function.getName() });
		} 
		Weaver.callOriginal();
	}

}