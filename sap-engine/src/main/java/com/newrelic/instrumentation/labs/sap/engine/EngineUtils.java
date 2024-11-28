package com.newrelic.instrumentation.labs.sap.engine;

import java.util.Map;

import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;

public class EngineUtils {
	
	public static ThreadLocal<Boolean> HEADERS_SET = new ThreadLocal<Boolean>() {

		@Override
		protected Boolean initialValue() {
			return false;
		}
		
		
	};
	
	public static void addMessageKey(Map<String,Object> attributes, MessageKey msgKey) {
		if(msgKey != null) {
			addValue(attributes, "MessageKey-ID", msgKey.getMessageId());
			addValue(attributes, "MessageKey-Direction", msgKey.getDirection());
		}
	}
	
	public static void addMessage(Map<String,Object> attributes, Message msg) {
		
		if(msg != null) {
			addValue(attributes, "Message-Action", msg.getAction());
			addValue(attributes, "Message-CorrelationId", msg.getCorrelationId());
			addValue(attributes, "Message-FromParty", msg.getFromParty());
			addValue(attributes, "Message-FromService", msg.getFromService());
			addValue(attributes, "Message-Id", msg.getMessageId());
			addValue(attributes, "Message-SequenceId", msg.getSequenceId());
			addValue(attributes, "Message-ToParty", msg.getToParty());
			addValue(attributes, "Message-ToService", msg.getToService());
		}
		
	}
	
	public static void addValue(Map<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}
	

}
