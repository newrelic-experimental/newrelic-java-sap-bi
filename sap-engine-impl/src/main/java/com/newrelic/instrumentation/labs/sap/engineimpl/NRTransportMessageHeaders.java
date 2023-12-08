package com.newrelic.instrumentation.labs.sap.engineimpl;

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

public class NRTransportMessageHeaders implements Headers {
	
	private TransportableMessage message = null;
	
	public NRTransportMessageHeaders(TransportableMessage msg) {
		message = msg;
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.MESSAGE;
	}

	@Override
	public String getHeader(String name) {
		try {
			TransportHeaders tHeaders = message.getTransportHeaders();
			Object value = tHeaders.getHeader(name);
			return value.toString();
		} catch (MessageFormatException e) {
			NewRelic.getAgent().getLogger().log(Level.FINER, e, "Failed to retrieve header value for {0}",name);
		}
		
		return null;
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
		try {
			TransportHeaders tHeaders = message.getTransportHeaders();
			tHeaders.setHeader(name, value);
		} catch (MessageFormatException e) {
			NewRelic.getAgent().getLogger().log(Level.FINER, e, "Failed to set header value for {0} with value {1}",name, value);
		}
	}

	@Override
	public void addHeader(String name, String value) {
		setHeader(name, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<String> getHeaderNames() {
		try {
			TransportHeaders tHeaders = message.getTransportHeaders();
			Map<String,Object> map = tHeaders.toMap();
			return map != null ? map.keySet() : Collections.emptyList();
		} catch (MessageFormatException e) {
		}
		return Collections.emptyList();
	}

	@Override
	public boolean containsHeader(String name) {
		// TODO Auto-generated method stub
		return false;
	}

}
