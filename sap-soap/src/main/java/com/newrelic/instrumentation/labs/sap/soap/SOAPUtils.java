package com.newrelic.instrumentation.labs.sap.soap;

import java.util.HashMap;
import java.util.Map;

import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;
import com.sap.engine.interfaces.messaging.spi.transport.Endpoint;

/**
 * Utility class to add attributes to a span
 * 
 */
public class SOAPUtils {

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
			addValue(attributes, "Message-Protocol", msg.getProtocol());
			addValue(attributes, "Message-SequenceId", msg.getSequenceId());
			addValue(attributes, "Message-ToParty", msg.getToParty());
			addValue(attributes, "Message-ToService", msg.getToService());
		}
		
	}
	
	private static void addEndpoint(Map<String,Object> attributes, Endpoint endpoint) {
		if(endpoint != null) {
			addValue(attributes, "Endpoint-Address", endpoint.getAddress());
			addValue(attributes, "Endpoint-Transport", endpoint.getTransport());
		}
	}
	
	public static void addValue(Map<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null && !(value.toString().isEmpty())) {
			attributes.put(key, value);
		}
	}
	
	public static Map<String,Object> processObject(Object principalObject) {
		HashMap<String,Object> attributes = new HashMap<String, Object>();
		
		if(principalObject instanceof Message) {
			Message message = (Message)principalObject;
			addMessage(attributes, message);
			addMessageKey(attributes, message.getMessageKey());
		}
		if(principalObject instanceof TransportableMessage) {
			TransportableMessage tMessage = (TransportableMessage)principalObject;
			addEndpoint(attributes, tMessage.getEndpoint());
			addValue(attributes, "SequenceNumber",tMessage.getSequenceNumber());
		}		
		return attributes.isEmpty() ? null : attributes;
	}
}
