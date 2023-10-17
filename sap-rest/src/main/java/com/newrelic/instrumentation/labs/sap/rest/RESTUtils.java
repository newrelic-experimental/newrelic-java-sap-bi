package com.newrelic.instrumentation.labs.sap.rest;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.adapter.xi.ms.XIMessage;
import com.sap.engine.interfaces.messaging.api.Action;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.Party;
import com.sap.engine.interfaces.messaging.api.Service;

public class RESTUtils {

	public static void reportMessage(Message message, Long duration) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		addValue(attributes, "Adapter", "REST");
		addAction(attributes, message.getAction());
		addValue(attributes, "CorrelationId", message.getCorrelationId());
//		addValue(attributes, "DeliverySemantics", message.getDeliverySemantics());
		addParty(attributes, message.getFromParty(),"From");
		addService(attributes, message.getFromService(), "From");
//		addValue(attributes, "MessageClass", message.getMessageClass());
		addValue(attributes, "MessageDirection", message.getMessageDirection());
		addMessageKey(attributes, message.getMessageKey());
		addValue(attributes, "Protocol", message.getProtocol());
//		addValue(attributes, "RefToMessageId", message.getRefToMessageId());
		addValue(attributes, "SequenceId", message.getSequenceId());
		addValue(attributes, "TimeReceived", message.getTimeReceived());
		addValue(attributes, "TimeSent", message.getTimeSent());
		addValue(attributes, "Duration", duration);
		addParty(attributes, message.getToParty(), "To");
		addService(attributes, message.getToService(), "To");
		if(message instanceof XIMessage) {
			addXIMessage(attributes, (XIMessage)message);
		}
		
		NewRelic.getAgent().getInsights().recordCustomEvent("MessageProcessing", attributes);
	}
	
	public static void addXIMessage(HashMap<String, Object> attributes, XIMessage message) {
//		addValue(attributes, "AckDestination", message.getAckDestination());
		addValue(attributes, "EndPoint", message.getEndpoint());
//		addValue(attributes, "ErrorCategory", message.getErrorCategory());
		addValue(attributes, "ErrorCode", message.getErrorCode());
		addValue(attributes, "InterefaceName", message.getInterfaceName());
		addValue(attributes, "MessageId", message.getMessageId());
		addValue(attributes, "MessagePriority", message.getMessagePriority());
		addValue(attributes, "ParentId", message.getParentId());
		addValue(attributes, "ProcessingMode", message.getProcessingMode());
		addValue(attributes, "Retries", message.getRetries());
//		addValue(attributes, "ScenarioIdentifier", message.getScenarioIdentifier());
		addValue(attributes, "SequenceNumber", message.getSequenceNumber());
		addValue(attributes, "Stage", message.getStage());

	}
	
	public static void addValue(HashMap<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}
	
	public static void addAction(HashMap<String,Object> attributes, Action action) {
		if(action != null) {
			addValue(attributes, "Action-Name", action.getName());
			addValue(attributes, "Action-Type", action.getType());
		}
	}
	
	public static void addParty(HashMap<String,Object> attributes, Party party, String direction) {
		addValue(attributes, direction+"Party-Name", party.getName());
		addValue(attributes, direction+"Party-Type", party.getType());
	}

	public static void addService(HashMap<String,Object> attributes, Service service, String direction) {
		addValue(attributes, direction+"Service-Name", service.getName());
		addValue(attributes, direction+"Service-Type", service.getType());
	}

	public static void addMessageKey(HashMap<String,Object> attributes, MessageKey msgKey) {
		addValue(attributes, "MessageKey-ID", msgKey.getMessageId());
		addValue(attributes, "MessageKey-Direction", msgKey.getDirection());
	}

}
