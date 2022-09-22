package com.newrelic.instrumentation.sap.message;

import java.util.Map;

import com.sap.engine.messaging.impl.core.queue.QueueMessage;

public class Utils {

	
	public static void addAttribute(Map<String,Object> attributes, String name, Object value) {
		if(name != null && !name.isEmpty() && value != null) {
			attributes.put(name, value);
		}
		
	}
	
	public static void addQueueMessage(Map<String, Object> attributes, QueueMessage qmsg) {
		if(qmsg != null) {
			addAttribute(attributes, "ConnectionName", qmsg.getConnectionName());
			addAttribute(attributes, "MessageKey", qmsg.getMessageKey());
			addAttribute(attributes, "MessageType", qmsg.getMessageType());
			addAttribute(attributes, "Protocol", qmsg.getProtocol());
		}
	}
}
