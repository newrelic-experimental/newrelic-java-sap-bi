package com.newrelic.instrumentation.labs.sap.sftp;

import java.util.Map;

import javax.resource.cci.InteractionSpec;

import com.sap.aii.adapter.sftp.ra.rar.integration.sftp.SSHConnection;
import com.sap.aii.af.lib.ra.cci.XIInteractionSpec;
import com.sap.engine.interfaces.messaging.api.Action;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;

public class SFTPUtils {
	
	public static void addMessage(Map<String, Object> attributes,Message message) {
		if(message != null) {
			MessageKey msgKey = message.getMessageKey();
			addMessageKey(attributes, msgKey);
			addAction(attributes,message.getAction());
			addValue(attributes, "CorrelationID", message.getCorrelationId());
			addValue(attributes, "MessageID", message.getMessageId());
		}
	}

	public static void addMessageKey(Map<String, Object> attributes, MessageKey msgKey) {
		if(msgKey != null) {
			addValue(attributes, "MessageKey-ID", msgKey.getMessageId());
			addValue(attributes, "MessageKey-Direction", msgKey.getDirection());
		}
	}
	
	public static void addAction(Map<String, Object> attributes,Action action) {
		if(action != null) {
			addValue(attributes, "Action-Name", action.getName());
			addValue(attributes, "Action-Type", action.getType());
		}
	}
	
	public static void addSSHConnection(Map<String, Object> attributes,SSHConnection conn) {
		addValue(attributes, "SSHConnection-Host", conn.getHost());
	}
	
	public static void addValue(Map<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}
	
	public static void addInteractionSpec(Map<String,Object> attributes, InteractionSpec spec) {
		if(spec instanceof XIInteractionSpec) {
			XIInteractionSpec xiSpec = (XIInteractionSpec)spec;
			addValue(attributes, "XIInteractionSpec-FunctionName", xiSpec.getFunctionName());
			addValue(attributes, "XIInteractionSpec-Class", xiSpec.getClass().getSimpleName());
		}
		
	}
}
