package com.sap.engine.services.httpserver.server;

import java.net.URI;

import com.newrelic.api.agent.HttpParameters;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.engine.services.httpserver.interfaces.client.Request;
import com.sap.engine.services.httpserver.interfaces.client.RequestLine;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;

@Weave
public abstract class Client {
	
	public abstract Request getRequest();

	@Trace
	public void send(byte[] msg, int off, int len) {
		Request req = getRequest();
//		MimeHeaders headers = req.getHeaders();
//		SAPHeadersWrapper wrapper = new SAPHeadersWrapper(headers);
//		NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(wrapper);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		String host = req.getHost();
		int port = req.getPort();
		String scheme = req.getScheme();
		
		if(req != null) {
			RequestLine reqLine = req.getRequestLine();
			if (reqLine != null) {
				MessageBytes urlBytes = reqLine.getFullUrl();
				if (urlBytes != null) {
					String uriString = scheme + "://" + host + ":" + port + urlBytes.toString();
					URI uri = URI.create(uriString);
					HttpParameters params = HttpParameters.library("SAP-HTTP").uri(uri).procedure("send")
							.noInboundHeaders().build();
					traced.reportAsExternal(params);
				}
			}
		}
		Weaver.callOriginal();
	}

	@Trace
	public void send(byte[] msg, int off, int len, byte connectionFlag) {
		Request req = getRequest();
//		MimeHeaders headers = req.getHeaders();
//		SAPHeadersWrapper wrapper = new SAPHeadersWrapper(headers);
//		NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(wrapper);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		String host = req.getHost();
		int port = req.getPort();
		String scheme = req.getScheme();
		
		if(req != null) {
			RequestLine reqLine = req.getRequestLine();
			if (reqLine != null) {
				MessageBytes urlBytes = reqLine.getFullUrl();
				if (urlBytes != null) {
					String uriString = scheme + "://" + host + ":" + port + urlBytes.toString();
					URI uri = URI.create(uriString);
					HttpParameters params = HttpParameters.library("SAP-HTTP").uri(uri).procedure("send")
							.noInboundHeaders().build();
					traced.reportAsExternal(params);
				}
			}
		}
		Weaver.callOriginal();
	}
}
