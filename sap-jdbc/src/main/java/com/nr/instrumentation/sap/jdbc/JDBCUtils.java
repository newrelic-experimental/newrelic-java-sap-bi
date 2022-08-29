package com.nr.instrumentation.sap.jdbc;

import java.util.HashMap;

import com.sap.engine.interfaces.messaging.api.Action;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.Party;
import com.sap.engine.interfaces.messaging.api.Service;


public class JDBCUtils {

	
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
