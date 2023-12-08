package com.newrelic.instrumentation.labs.sap.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.Headers;
import com.newrelic.api.agent.NewRelic;
import com.sap.engine.interfaces.messaging.api.exception.MessageFormatException;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;
import com.sap.engine.interfaces.messaging.spi.transport.TransportHeaders;

public class NRTransportHeaders implements Headers {

	private TransportHeaders headers = null;

	public NRTransportHeaders(TransportHeaders msg) {
		headers = msg;
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.MESSAGE;
	}

	@Override
	public String getHeader(String name) {
		Object value = headers.getHeader(name);
		return value.toString();
	}

	@Override
	public Collection<String> getHeaders(String name) {
		List<String> values = new ArrayList<>();
		String value = getHeader(name);
		if(value != null && !value.isEmpty()) {
			values.add(value);
		}
		return values;
	}

	@Override
	public void setHeader(String name, String value) {
		headers.setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		setHeader(name, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<String> getHeaderNames() {
		Map<String,Object> map = headers.toMap();
		return map != null ? map.keySet() : Collections.emptyList();
	}

	@Override
	public boolean containsHeader(String name) {
		return getHeaderNames().contains(name);
	}

}
