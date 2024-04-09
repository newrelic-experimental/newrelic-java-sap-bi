package com.newrelic.instrumentation.labs.sap.engineimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.Headers;
import com.newrelic.api.agent.NewRelic;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessagePropertyKey;
import com.sap.engine.interfaces.messaging.api.exception.InvalidParamException;

public class NRMessageHeaders implements Headers {
	
	private static final String NEWRELIC_NAMESPACE = "java.newrelic.com";
	
	private HashMap<String, String> headers = new HashMap<>();
	
	public NRMessageHeaders() {
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.MESSAGE;
	}

	@Override
	public String getHeader(String name) {
		return headers.get(name);
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
		headers.put(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return headers.keySet();
	}

	@Override
	public boolean containsHeader(String name) {
		return getHeaderNames().contains(name);
	}

	public void loadInboundHeaders(Message message) {
		Set<MessagePropertyKey> keys = message.getMessagePropertyKeys();
		for(MessagePropertyKey key : keys) {
			if(key.getPropertyNamespace().equals(NEWRELIC_NAMESPACE)) {
				String value = message.getMessageProperty(key);
				headers.put(key.getPropertyName(), value);
				// remove key so it doesn't get reprocessed upstream
				message.removeMessageProperty(key);
			}
		}
	}
	
	public void populateOutboundHeaders(Message message) {
		Set<String> keys = headers.keySet();
		for(String key : keys) {
			String value = headers.get(key);
			if(value != null && !value.isEmpty()) {
				MessagePropertyKey msgKey = new MessagePropertyKey(key, NEWRELIC_NAMESPACE);
				try {
					message.setMessageProperty(msgKey, value);
				} catch (InvalidParamException e) {
					NewRelic.getAgent().getLogger().log(Level.FINER, e, "Failed to add messagepropertykey {0} with value {1} to message {2}",msgKey,value,message);
					NewRelic.incrementCounter("Supportability/SAP/Messaging/PopulateHeadersFailed");
				}
			}
		}
	}
}
