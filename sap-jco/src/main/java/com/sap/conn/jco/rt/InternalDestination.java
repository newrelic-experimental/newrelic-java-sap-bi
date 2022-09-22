package com.sap.conn.jco.rt;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.conn.jco.JCoDestination;

@Weave(type=MatchType.BaseClass)
abstract class InternalDestination implements JCoDestination {

	@Trace
	void execute(AbapFunction var1) {
		String destName = getDestinationName();
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Destination",getClass().getSimpleName(),"execute",destName);
		Weaver.callOriginal();
	}

	@Trace
	void execute(AbapFunctionUnit var1) {
		String destName = getDestinationName();
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Destination",getClass().getSimpleName(),"execute",destName);
		Weaver.callOriginal();
	}

	@Trace
	void execute(AbapFunction var1, String var2, String var3) {
		String destName = getDestinationName();
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Destination",getClass().getSimpleName(),"execute",destName);
		Weaver.callOriginal();
	}

}
