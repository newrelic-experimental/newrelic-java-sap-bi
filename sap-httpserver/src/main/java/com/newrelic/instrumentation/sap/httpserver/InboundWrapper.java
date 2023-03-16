package com.newrelic.instrumentation.sap.httpserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.Headers;
import com.sap.engine.services.httpserver.lib.headers.MimeHeaders;

public class InboundWrapper implements Headers {
	
	private MimeHeaders headers = null;
	
	public InboundWrapper(MimeHeaders h) {
		headers = h;
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.HTTP;
	}

	@Override
	public String getHeader(String name) {
		if(headers != null) {
			return headers.getHeader(name);
		}
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		if(headers != null) {
			String[] headerArray = headers.getHeaders(name);
			return Arrays.asList(headerArray);
		}
		return new ArrayList<>();
	}

	@Override
	public void setHeader(String name, String value) {
		if(headers != null) {
			headers.putHeader(name.getBytes(), value.getBytes());
		}
	}

	@Override
	public void addHeader(String name, String value) {
		if(headers != null) {
			headers.addHeader(name.getBytes(), value.getBytes());
		}
	}

	@Override
	public Collection<String> getHeaderNames() {
		
		if(headers != null) {
			Enumeration<?> names = headers.names();
			ArrayList<String> list = new ArrayList<>();
			while(names.hasMoreElements()) {
				list.add(names.nextElement().toString());
			}
			return list;
		}
		
		
		return Collections.emptyList();
	}

	@Override
	public boolean containsHeader(String name) {
		
		return headers != null ? headers.containsHeader(name) : false;
	}

}
