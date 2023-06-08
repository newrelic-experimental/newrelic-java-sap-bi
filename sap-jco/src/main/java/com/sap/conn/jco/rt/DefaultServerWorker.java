package com.sap.conn.jco.rt;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransactionNamePriority;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.server.JCoServerContext;

@Weave
public abstract class DefaultServerWorker {

	@Trace(dispatcher = true)
	void dispatchRequest(JCoFunction function) {
		String funcName = function.getName();
		NewRelic.getAgent().getTracedMethod().setMetricName(new String[] { "Custom", "DefaultServerWorker", "dispatchRequest", funcName });
		NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.CUSTOM_LOW, false, "SAP-JCO", new String[] { "JCOFunction", funcName });
		Weaver.callOriginal();
	}
	
	@Weave(type = MatchType.BaseClass)
	public static abstract class CallDispatcher {
		
		@Trace
		protected Object handleRequest(JCoServerContext var1, JCoFunction var2) {
			NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","JCO","CallDispatcher",getClass().getSimpleName(),"handleRequest",var2.getName());
			return Weaver.callOriginal();
		}
	}

}