package com.sap.conn.jco.rt;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.conn.jco.AbapClassException;

@Weave
public abstract class ClientConnection {

	public abstract ConnectionAttributes getAttributes();
	
	@Trace
	private void execute(String functionName, DefaultParameterList input,DefaultParameterList inputTables, DefaultParameterList changing,DefaultParameterList output, boolean supportsASXML,
			AbapClassException.Mode classExceptionMode) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","ClientConnection","execute",functionName);
		Weaver.callOriginal();
	}
	
}
