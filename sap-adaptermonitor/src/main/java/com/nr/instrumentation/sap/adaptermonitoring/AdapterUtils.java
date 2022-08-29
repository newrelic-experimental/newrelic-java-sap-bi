package com.nr.instrumentation.sap.adaptermonitoring;

import java.util.Map;

import com.sap.aii.af.service.cpa.Channel;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;

public class AdapterUtils {

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
	
	public static void addChannel(Map<String,Object> attributes, Channel channel) {
		if(channel != null) {
			addValue(attributes, "Channel-Name", channel.getChannelName());
			addValue(attributes, "Channel-AdapterNamespace", channel.getAdapterNamespace());
			addValue(attributes, "Channel-AdapterType", channel.getAdapterType());
			addValue(attributes, "Channel-Direction", channel.getDirection());
			addValue(attributes, "Channel-EngineName", channel.getEngineName());
			addValue(attributes, "Channel-EngineType", channel.getEngineType());
			addValue(attributes, "Channel-ObjectName", channel.getObjectName());
			addValue(attributes, "Channel-ObjectType", channel.getObjectType());
			addValue(attributes, "Channel-Party", channel.getParty());
			addValue(attributes, "Channel-Service", channel.getService());
		}
	}
	
	public static void addValue(Map<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}

	public static String format(Object[] objArray) {
		if(objArray != null && objArray.length > 0) {
			Object object1 = objArray[0];
			int length = objArray.length;
			
			if(object1 instanceof String) {
				String format = (String)object1;
	
				if(length >= 2) {
					int index = format.indexOf("{0}");
					if(index > -1) {
						format = format.replace("{0}", objArray[1].toString());
					}
				}
				if(length >= 3) {
					int index = format.indexOf("{1}");
					if(index > -1) {
						format = format.replace("{1}", objArray[2].toString());
					}
				}
				if(length >= 4) {
					int index = format.indexOf("{2}");
					if(index > -1) {
						format = format.replace("{2}", objArray[3].toString());
					}
				}
				if(length >= 5) {
					int index = format.indexOf("{3}");
					if(index > -1) {
						format = format.replace("{3}", objArray[4].toString());
					}
				}
				return format;
			}
		}
		
		return null;
	}
}
