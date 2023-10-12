package com.newrelic.instrumentation.labs.sap.adapters.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.Headers;
import com.newrelic.api.agent.NewRelic;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessagePropertyKey;
import com.sap.engine.interfaces.messaging.api.exception.InvalidParamException;

public class SAPMessageHeaders implements Headers {
	
	private Message message = null;
	private static final String NR_NAMESPACE = "com.newrelic.java.sap";
	
	public SAPMessageHeaders(Message msg) {
		message = msg;
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.MESSAGE;
	}

	@Override
	public String getHeader(String name) {
		MessagePropertyKey key = new MessagePropertyKey(name, NR_NAMESPACE);
		return message.getMessageProperty(key);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		List<String> list = new ArrayList<>();
		String value = getHeader(name);
		if(value != null && !value.isEmpty()) {
			list.add(value);
		}
		return list;
	}

	@Override
	public void setHeader(String name, String value) {
		MessagePropertyKey key = new MessagePropertyKey(name, NR_NAMESPACE);
		try {
			message.setMessageProperty(key, value);
		} catch (InvalidParamException e) {
			NewRelic.getAgent().getLogger().log(Level.FINER, e, "Failed to set sap header with key: {0} and value: {1}", name, value);
		}
	}

	@Override
	public void addHeader(String name, String value) {
		setHeader(name, value);
	}

	@Override
	public Collection<String> getHeaderNames() {
		Set<MessagePropertyKey> msgPropertyKeys = message.getMessagePropertyKeys();
		List<String> list = new ArrayList<>();
		if(msgPropertyKeys != null && !msgPropertyKeys.isEmpty()) {
			for(MessagePropertyKey key : msgPropertyKeys) {
				String value = key.getPropertyName();
				if(value != null && !value.isEmpty()) {
					list.add(value);
				}
			}
		}
		return list;
	}

	@Override
	public boolean containsHeader(String name) {
		return getHeaderNames().contains(name);
	}

}
