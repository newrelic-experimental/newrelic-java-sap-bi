package com.sap.conn.idoc.jco.rt;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;
import com.sap.conn.jco.server.JCoServerRequestHandler;

@Weave
abstract class DefaultJCoIDocServerWorker {

	@Weave
	protected static class IDocDispatcher {
		
		@Trace
		protected Object handleRequest(JCoServerContext serverCtx, JCoFunction function) {
			String fnName = function != null ? function.getName() : null;
			if(fnName != null) {
				NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","IDocDispatcher","handleRequest",fnName);
			} else {
				NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","IDocDispatcher","handleRequest");
			}
			return Weaver.callOriginal();
		}
	}
	
	@Weave
	protected static class FunctionDispatcherIDocServer {

		@Trace
		public JCoServerFunctionHandler handleRequest(JCoServerContext serverCtx, JCoFunction function) {
			return Weaver.callOriginal();
		}
	}
	
	@Weave
	protected static class RequestDispatcherIDocServer {
		
		@Trace
		public JCoServerRequestHandler handleRequest(JCoServerContext serverCtx, JCoFunction function) {
			return Weaver.callOriginal();
		}
	}
}
