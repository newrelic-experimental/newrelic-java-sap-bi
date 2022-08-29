package com.nr.instrumentation.sap.engineimpl;

import java.util.HashMap;
import java.util.Map;

import com.newrelic.api.agent.NewRelic;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.MessageStatus;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;

public class EngineUtils {

	public static void recordAuditLog(AuditLogStatus status, String textKey, Object[] params) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		recordValue(attributes,"AuditLogStatus",status.toString());
		recordValue(attributes,"TextKey",textKey);
		if (params != null) {
			int count = 1;
			for (int i = 0; i < params.length; i++) {
				Object value = params[i];
				recordValue(attributes, "Parameter-"+count, value);
				count++;
			} 
		}
		NewRelic.getAgent().getInsights().recordCustomEvent("AuditLogStatus", attributes);
	}
	
	public static void recordAuditLog(MessageKey msgKey, AuditLogStatus status, String textKey, Object[] params) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		recordMessageKey(attributes, msgKey);
		recordValue(attributes,"AuditLogStatus",status.toString());
		recordValue(attributes,"TextKey",textKey);
		if (params != null) {
			int count = 1;
			for (int i = 0; i < params.length; i++) {
				Object value = params[i];
				recordValue(attributes, "Parameter-"+count, value);
				count++;
			} 
		}
		NewRelic.getAgent().getInsights().recordCustomEvent("AuditLogStatus", attributes);
	}
	
	private static void recordMessageKey(Map<String,Object> attributes, MessageKey msgKey) {
		recordValue(attributes, "MessageKey-ID", msgKey.getMessageId());
		recordValue(attributes, "MessageKey-Direction", msgKey.getDirection());
	}
	
	private static void recordValue(Map<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
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
