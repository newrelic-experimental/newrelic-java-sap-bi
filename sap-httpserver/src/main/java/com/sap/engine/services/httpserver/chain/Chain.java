package com.sap.engine.services.httpserver.chain;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
//import com.newrelic.api.agent.TransportType;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.sap.httpserver.SAPExtendedRequest;
import com.newrelic.instrumentation.sap.httpserver.SAPExtendedResponse;

@Weave(type = MatchType.Interface)
public abstract class Chain {

	@Trace
	public void process(HTTPRequest request, HTTPResponse response) {
		if (request != null) {
			NewRelic.getAgent().getTransaction().setWebRequest(new SAPExtendedRequest(request));
		}
		boolean processAfter = true;
		if(response != null) {
			processAfter = false;
			NewRelic.getAgent().getTransaction().setWebResponse(new SAPExtendedResponse(response));
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Chain",getClass().getSimpleName(),"process");
		Weaver.callOriginal();
		if(processAfter) {
			NewRelic.getAgent().getTransaction().setWebResponse(new SAPExtendedResponse(response));
		}
	}
}
