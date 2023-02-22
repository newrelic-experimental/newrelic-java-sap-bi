package com.sap.aii.af.sdk.xi.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.Headers;

public class SAPAFHeaders implements Headers {
	
	private TransportMessage message = null;
	
	public SAPAFHeaders(TransportMessage m) {
		message = m;
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.MESSAGE;
	}

	@Override
	public String getHeader(String name) {
		String[] values = message.getHeader(name);
		if(values != null && values.length > 0) {
			return values[0];
		}
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		String[] values = message.getHeader(name);
		
		return Arrays.asList(values);
	}

	@Override
	public void setHeader(String name, String value) {
		message.addHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		message.addHeader(name, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<String> getHeaderNames() {
		List<String> names = new ArrayList<>();
		if(message != null) {
			if(message.headers != null) {
				names.addAll(message.headers.keySet());
			}
		}
		return names;
	}

	@Override
	public boolean containsHeader(String name) {
		
		return getHeaderNames().contains(name);
	}

}
