package com.nr.instrumentation.sap.httpclient;

import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.OutboundHeaders;
import com.sap.httpclient.HttpMethod;

public class OutboundWrapper implements OutboundHeaders {
	
	private HttpMethod method = null;
	
	public OutboundWrapper(HttpMethod m) {
		method = m;
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.HTTP;
	}

	@Override
	public void setHeader(String name, String value) {
		method.addRequestHeader(name, value);
	}

}
