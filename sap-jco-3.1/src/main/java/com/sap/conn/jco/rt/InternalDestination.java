package com.sap.conn.jco.rt;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.jco.NRJcoUtils;
import com.sap.conn.jco.JCoDestination;

@Weave(type=MatchType.BaseClass)
abstract class InternalDestination implements JCoDestination {

	@Trace
	void execute(AbapFunction abapFunction) {
		String destName = getDestinationName();
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Destination",getClass().getSimpleName(),"execute",destName);
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		NRJcoUtils.addJcoDestination(attributes, this);
		NRJcoUtils.addAttribute(attributes, "ABAPFunction",abapFunction != null ? abapFunction.getName() : "UnknownABAPFunction");
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}

	@Trace
	void execute(AbapFunctionUnit abapFunctionUnit) {
		String destName = getDestinationName();
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Destination",getClass().getSimpleName(),"execute",destName);
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		NRJcoUtils.addJCoFunctionUnit(attributes, abapFunctionUnit);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}

	@Trace
	void execute(AbapFunction abapFunction, String tid, String queueName) {
		String destName = getDestinationName();
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Destination",getClass().getSimpleName(),"execute",destName);
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		NRJcoUtils.addJcoDestination(attributes, this);
		NRJcoUtils.addAttribute(attributes, "ABAPFunction",abapFunction != null ? abapFunction.getName() : "UnknownABAPFunction");
		NRJcoUtils.addAttribute(attributes, "TID",tid);
		NRJcoUtils.addAttribute(attributes, "QueueNme",queueName);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}

}
