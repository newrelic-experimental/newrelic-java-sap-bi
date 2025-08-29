package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.Map;

import com.sap.engine.interfaces.messaging.api.MessageKey;

public class MapDataHolder implements DataHolder {
 
	protected Map<String,String> attributes;
	protected MessageKey messageKey;
	
	public MapDataHolder(MessageKey msgKey, Map<String,String> map) {
		attributes = map;
		messageKey = msgKey;
	}
	
	public Map<String,String> getAttributes() {
		return attributes;
	}
	
	public MessageKey getMessageId() {
		return messageKey;
	}
}
