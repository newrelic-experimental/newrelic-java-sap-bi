package com.sap.conn.jco.rt;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.conn.jco.AbapClassException;
import com.newrelic.api.agent.Trace;

@Weave(type = MatchType.BaseClass)
public abstract class AbstractServerConnection {
	
	@NewField
	public Token token = null;

	AbstractServerConnection(ServerInternal server) {
		
	}
	
	@Trace
	protected void executeCallback(AbstractServerWorker server, String name, DefaultParameterList imp,
			DefaultParameterList imptab, DefaultParameterList chn, DefaultParameterList exp, boolean supportsASXML,
			AbapClassException.Mode classExceptionMode) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","AbstractServerConnection".getClass().getSimpleName(),"executeCallback",name);
		Weaver.callOriginal();
	}
	
	@Trace
	private void dispatchRequest(AbstractServerWorker serverWorker, String functionName) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","AbstractServerConnection".getClass().getSimpleName(),"dispatchRequest",functionName);
		Weaver.callOriginal();
	}
}
