package com.sap.conn.jco.rt;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransactionNamePriority;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.conn.jco.JCoFunction;

@Weave(type = MatchType.BaseClass)
public abstract class AbstractServerWorker {

	
	protected AbstractServerConnection conn = Weaver.callOriginal();
	
	@Trace(dispatcher = true)
	protected void dispatchRequest(JCoFunction function) {
		String funcName = function.getName();
		NewRelic.getAgent().getTracedMethod().setMetricName(new String[] { "Custom", "ServerWorker", getClass().getSimpleName(), "dispatchRequest", funcName });
		NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.CUSTOM_LOW, false, "SAP-JCO", new String[] { "JCOFunction", funcName });
		Weaver.callOriginal();
	}
	
	@Trace(async = true)
	protected void dispatch() {
		if(conn != null) {
			if(conn.token != null) {
				conn.token.linkAndExpire();
				conn.token = null;
			}
		}
		
		Weaver.callOriginal();
	}
}
