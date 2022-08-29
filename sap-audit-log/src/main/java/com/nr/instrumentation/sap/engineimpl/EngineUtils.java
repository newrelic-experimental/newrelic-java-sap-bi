package com.nr.instrumentation.sap.engineimpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newrelic.api.agent.NewRelic;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.MessageStatus;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;

public class EngineUtils {
	
	private static Properties messageMappings = MessageMappings.getInstance();
	

	public static void recordAuditLog(AuditLogStatus status, String origTextKey, Object[] params) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		String auditStatus;
		if(status == AuditLogStatus.SUCCESS) {
			auditStatus = "Success";
		} else if(status == AuditLogStatus.WARNING) {
			auditStatus = "Warning";
		} else if(status == AuditLogStatus.ERROR) {
			auditStatus = "Error";
		} else {
			auditStatus = "Unknown";
		}
		
		recordValue(attributes,"AuditLogStatus",auditStatus);
		recordTextKey(attributes, origTextKey, params);
		
		NewRelic.getAgent().getInsights().recordCustomEvent("AuditLogStatus", attributes);
	}
	
	public static void recordAuditLog(MessageKey msgKey, AuditLogStatus status, String origTextKey, Object[] params) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		recordMessageKey(attributes, msgKey);

		String auditStatus;
		if(status == AuditLogStatus.SUCCESS) {
			auditStatus = "Success";
		} else if(status == AuditLogStatus.WARNING) {
			auditStatus = "Warning";
		} else if(status == AuditLogStatus.ERROR) {
			auditStatus = "Error";
		} else {
			auditStatus = "Unknown";
		}
		
		recordValue(attributes,"AuditLogStatus",auditStatus);
		recordTextKey(attributes, origTextKey, params);
		
		NewRelic.getAgent().getInsights().recordCustomEvent("AuditLogStatus", attributes);
	}
	
	private static void recordTextKey(Map<String,Object> attributes, String origTextKey, Object[] params) {
		String textKey = convert(origTextKey);
		int size = params.length;
		String key = "TextKey";
		
		switch(size) {
		case 0:
			recordValue(attributes, key, textKey);
			break;
		case 1:
			String temp = textKey.replace("{0}", params[0].toString());
			recordValue(attributes, key, temp);
			break;
		case 2:
			String temp2 = textKey.replace("{0}", params[0].toString()).replace("{1}", params[1].toString());
			recordValue(attributes, key, temp2);
			break;
		case 3:
			String temp3 = textKey.replace("{0}", params[0].toString()).replace("{1}", params[1].toString()).replace("{2}", params[2].toString());
			recordValue(attributes, key, temp3);
			break;
		case 4:
			String temp4 = textKey.replace("{0}", params[0].toString()).replace("{1}", params[1].toString()).replace("{2}", params[2].toString()).replace("{3}", params[3].toString());
			recordValue(attributes, key, temp4);
			break;
		case 5:
			String temp5 = textKey.replace("{0}", params[0].toString()).replace("{1}", params[1].toString()).replace("{2}", params[2].toString()).replace("{3}", params[3].toString()).replace("{4}", params[4].toString());
			recordValue(attributes, key, temp5);
			break;
			
			
		}
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
	
	private static String convert(String s) {
		String t = null;
		if(messageMappings != null) {
			t = messageMappings.getProperty(s);
		}


		return t != null ? t : s;
	}

}
