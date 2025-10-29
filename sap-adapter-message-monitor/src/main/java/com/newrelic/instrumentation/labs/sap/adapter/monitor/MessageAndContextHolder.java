package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.HashMap;
import java.util.Map;

import com.sap.engine.interfaces.messaging.spi.TransportableMessage;

public class MessageAndContextHolder implements DataHolder {
	
	private TransportableMessage message;
	private Map<String, Object>  context;
	
	
	public MessageAndContextHolder(TransportableMessage msg, Map<String, Object> ctx) {
		message = msg;
		context = ctx;
	}

	public TransportableMessage getMessage() {
		return message;
	}

	public Map<String, String> getContext() {
		Map<String, String> stringContext = new HashMap<String, String>();
		for(String key : context.keySet()) {
			Object value = context.get(key);
			if(value != null) {
				stringContext.put(key, value.toString());
			}
		}
		return stringContext;
	}

	
}
