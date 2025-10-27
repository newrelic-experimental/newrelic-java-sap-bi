package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import com.sap.engine.interfaces.messaging.api.MessageKey;

public class MessageToProcess {
	
	private static final int MAX_RETRIES = 6;
	private int retries = 0;
	private MessageKey messageKey = null;
	
	public MessageToProcess(MessageKey msgKey) {
		messageKey = msgKey;
		retries = 0;
	}
	
	public void incrementRetries() {
		retries++;
	}
	
	public boolean retry() {
		return retries < MAX_RETRIES;
	}
	
	public MessageKey getMessageKey() {
		return messageKey;
	}
	
	public int getRetries () {
		return retries;
	}

}
