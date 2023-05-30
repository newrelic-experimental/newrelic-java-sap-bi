package com.nr.instrumentation.sap.as2;

import java.util.Map;

import com.sap.aii.adapter.as2.ra.api.pdu.AS2Message;
import com.sap.aii.af.service.cpa.Channel;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;

public class AS2Utils {

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
//			addValue(attributes, "Message-FromParty", msg.getFromParty());
//			addValue(attributes, "Message-FromService", msg.getFromService());
			addValue(attributes, "Message-Id", msg.getMessageId());
//			addValue(attributes, "Message-Protocol", msg.getProtocol());
			addValue(attributes, "Message-SequenceId", msg.getSequenceId());
//			addValue(attributes, "Message-ToParty", msg.getToParty());
//			addValue(attributes, "Message-ToService", msg.getToService());
		}
		
	}
	
	public static void addChannel(Map<String,Object> attributes, Channel channel) {
		if(channel != null) {
			addValue(attributes, "Channel-Name", channel.getChannelName());
			addValue(attributes, "Channel-AdapterType", channel.getAdapterType());
			addValue(attributes, "Channel-Direction", channel.getDirection());
//			addValue(attributes, "Channel-EngineName", channel.getEngineName());
//			addValue(attributes, "Channel-EngineType", channel.getEngineType());
//			addValue(attributes, "Channel-ObjectName", channel.getObjectName());
//			addValue(attributes, "Channel-ObjectType", channel.getObjectType());
			addValue(attributes, "Channel-Party", channel.getParty());
			addValue(attributes, "Channel-Service", channel.getService());
		}
	}
	
	public static void addAS2Message(Map<String,Object> attributes, AS2Message msg) {
		if(msg != null) {
			addValue(attributes, "AS2Message-ContentType", msg.getContentType());
			addValue(attributes, "AS2Message-FileName", msg.getFileName());
			addValue(attributes, "AS2Message-FromEmain", msg.getFromEmail());
			addValue(attributes, "AS2Message-FromName", msg.getFromName());
			addValue(attributes, "AS2Message-MessageId", msg.getMessageId());
			addValue(attributes, "AS2Message-Subject", msg.getSubject());
			addValue(attributes, "AS2Message-ToName", msg.getToName());
		}
	}
	
	public static void addValue(Map<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}
}
