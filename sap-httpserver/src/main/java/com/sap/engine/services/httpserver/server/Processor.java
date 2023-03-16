package com.sap.engine.services.httpserver.server;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransactionNamePriority;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.bc.proj.jstartup.fca.FCAConnection;

@Weave
public class Processor {

	@Trace(dispatcher = true)
	void chainedRequest(FCAProcessorThread processorThread, FCAConnection connection, int icm_id, int client_id) {
		NewRelic.getAgent().getTransaction().convertToWebTransaction();
		String tName = connection.getRequestPath();
		NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.FRAMEWORK_HIGH, false, "SAP-HTTPServer", tName);
		Weaver.callOriginal();
	}
		
	@Weave
	static class FCAProcessorThread  {
		
		protected boolean consume(FCAConnection conn, String consName) {
			NewRelic.getAgent().getTracedMethod().addCustomAttribute("ConsumerType", consName);
			return Weaver.callOriginal();
		}
	}
}
