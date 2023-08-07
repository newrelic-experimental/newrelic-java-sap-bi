package com.newrelic.instrumentation.sap.httpserver;

import com.newrelic.api.agent.ExtendedResponse;
import com.newrelic.api.agent.HeaderType;
import com.sap.engine.services.httpserver.chain.HTTPResponse;
import com.sap.engine.services.httpserver.lib.headers.MimeHeaders;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.httpserver.server.ResponseImpl;

public class SAPExtendedResponse extends ExtendedResponse {
	
	HTTPResponse response = null;
	ResponseImpl raw_response = null;
	
	@SuppressWarnings("deprecation")
	public SAPExtendedResponse(HTTPResponse resp) {
		response = resp;
		raw_response = response.getRawResponse();
	}

	@Override
	public int getStatus() throws Exception {
		return response.getStatusCode();
	}

	@Override
	public String getStatusMessage() throws Exception {
		byte[] responsePhrase = raw_response.getResponsePhrase();
		return new String(responsePhrase);
	}

	@Override
	public String getContentType() {
		MimeHeaders headers = raw_response.getHeaders();
		
		return headers != null ? headers.getHeader(HeaderNames.entity_header_content_type) : null;
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.HTTP;
	}

	@Override
	public void setHeader(String name, String value) {
		raw_response.putHeader(name.getBytes(), value.getBytes());
	}

	@Override
	public long getContentLength() {
		return response.getContentLength();
	}

}
