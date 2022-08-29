package com.sap.httpclient;

import java.net.URI;

import com.newrelic.api.agent.HttpParameters;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.httpclient.OutboundWrapper;
import com.sap.httpclient.exception.URIException;

@Weave
public abstract class HttpClient {
	
	@Trace
	public int executeMethod(HostConfiguration hostconfig, HttpMethod method, HttpState state) {
		try {
			URI uri = URI.create(method.getURI().getURI());
			HttpParameters params = HttpParameters.library("SAP-HttpClient").uri(uri).procedure("executeMethod").noInboundHeaders().build();
			NewRelic.getAgent().getTracedMethod().reportAsExternal(params);
			
		} catch (URIException e) {
		}
		NewRelic.getAgent().getTracedMethod().addOutboundRequestHeaders(new OutboundWrapper(method));
		
		return Weaver.callOriginal();
	}

}
