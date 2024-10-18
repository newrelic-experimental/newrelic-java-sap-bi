package com.sap.conn.jco.rt;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.jco.NRJcoUtils;
import com.sap.conn.jco.AbapClassException;

@Weave
public abstract class ClientConnection {

	public abstract ConnectionAttributes getAttributes();
	
	@Trace
	private void execute(String functionName, DefaultParameterList input,DefaultParameterList inputTables, DefaultParameterList changing,DefaultParameterList output, boolean supportsASXML,
			AbapClassException.Mode classExceptionMode) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","ClientConnection","execute",functionName);
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		NRJcoUtils.addJcoAttributes(attributes, getAttributes());
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
	
}
