package com.newrelic.instrumentation.labs.sap.engineimpl;

import java.util.HashMap;
import java.util.Map;

import com.newrelic.api.agent.NewRelic;
import com.sap.engine.interfaces.messaging.api.Action;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.MessageStatus;
import com.sap.engine.interfaces.messaging.api.Party;
import com.sap.engine.interfaces.messaging.api.Service;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;

public class EngineUtils {
	
	public static String TOKENKEY = "newrelicasynctoken";

	public static void recordMessage(Map<String, Object> attributes, Message msg) {
		if(msg != null && attributes != null) {
			recordValue(attributes, "Message-CorrelationID", msg.getCorrelationId());
			recordValue(attributes, "MessageId", msg.getMessageId());
			recordAction(attributes, msg.getAction());
			recordParty(attributes, msg.getFromParty(),"Message-From");
			recordParty(attributes, msg.getToParty(),"Message-To");
			recordService(attributes, msg.getFromService(),"Message-From");
			recordService(attributes, msg.getToService(),"Message-To");
		}
	}
		
	public static void recordTranportMessage(Map<String, Object> attributes, TransportableMessage msg) {
		if(msg != null && attributes != null) {
			recordValue(attributes, "TransportMessage-CorrelationID", msg.getCorrelationId());
			recordValue(attributes, "TransportMessage-MessageId", msg.getMessageId());
			recordAction(attributes, msg.getAction());
			recordParty(attributes, msg.getFromParty(),"TransportMessage-From");
			recordParty(attributes, msg.getToParty(),"TransportMessage-To");
			recordService(attributes, msg.getFromService(),"TransportMessage-From");
			recordService(attributes, msg.getToService(),"TransportMessage-To");
		}
	}
	
	public static void recordService(Map<String, Object> attributes, Service service, String prefix) {
		if(service != null) {
			recordValue(attributes, prefix != null ? prefix+"Service" : "Service", service.getName());
		}
	}
	
	public static void recordParty(Map<String, Object> attributes, Party party, String prefix) {
		if(party != null) {
			recordValue(attributes, prefix != null ? prefix+"Party" : "Party", party.getName());
		}
	}
	
	public static void recordAction(Map<String, Object> attributes, Action action) {
		if(action != null) {
			recordValue(attributes, "Action", action.getName());
		}
		
	}

	
	public static void recordMessageKey(Map<String,Object> attributes, MessageKey msgKey) {
		recordValue(attributes, "MessageKey-ID", msgKey.getMessageId());
		recordValue(attributes, "MessageKey-Direction", msgKey.getDirection());
	}
	
	public static void recordValue(Map<String,Object> attributes, String key, Object value) {
		if(attributes != null && key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}
	
	public static void recordMessageLog(Message message, MessageStatus status, String errorCode) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		addMessageKey(attributes, message.getMessageKey());
		if(status != null) {
			recordValue(attributes,"MessageStatus",status.toString());
		}
		recordValue(attributes, "ErrorCode", errorCode);
		NewRelic.getAgent().getInsights().recordCustomEvent("MessageLog", attributes);
	}


	public static void addMessageKey(Map<String,Object> attributes, MessageKey msgKey) {
		if(msgKey != null) {
			recordValue(attributes, "MessageKey-ID", msgKey.getMessageId());
			recordValue(attributes, "MessageKey-Direction", msgKey.getDirection());
		}
	}

}
