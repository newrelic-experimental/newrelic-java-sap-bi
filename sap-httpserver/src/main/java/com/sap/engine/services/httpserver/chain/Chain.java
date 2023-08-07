package com.sap.engine.services.httpserver.chain;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransportType;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.sap.httpserver.SAPExtendedRequest;
import com.newrelic.instrumentation.sap.httpserver.SAPExtendedResponse;
import com.newrelic.instrumentation.sap.httpserver.SAPHeadersWrapper;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.client.Request;
import com.sap.engine.services.httpserver.lib.headers.MimeHeaders;

@Weave(type = MatchType.Interface)
public abstract class Chain {

	@SuppressWarnings("deprecation")
	@Trace
	public void process(HTTPRequest request, HTTPResponse response) {
		if (request != null) {
			NewRelic.getAgent().getTransaction().setWebRequest(new SAPExtendedRequest(request));

			HttpParameters params = request.getHTTPParameters();
			if (params != null) {
				Request req = params.getRequest();
				if (req != null) {
					MimeHeaders headers = req.getHeaders();
					if (headers != null) {
						SAPHeadersWrapper wrapper = new SAPHeadersWrapper(headers);
						NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.HTTP, wrapper);
					}
				}
			} 
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
