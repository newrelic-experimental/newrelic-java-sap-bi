package com.newrelic.instrumentation.labs.sap.adaptermonitoring;

import java.util.Map;

import com.newrelic.agent.environment.AgentIdentity;
import com.newrelic.agent.environment.Environment;
import com.newrelic.agent.environment.EnvironmentService;
import com.newrelic.agent.service.ServiceFactory;
import com.sap.aii.af.service.administration.api.monitoring.ChannelState;
import com.sap.aii.af.service.cpa.Channel;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;

/**
 * Utility class for extracting desired attributes and using them to populate spans
 * @author dhilpipre
 *
 */
public class AdapterUtils {
	
	private static EnvironmentService environmentService = ServiceFactory.getEnvironmentService();
	private static Environment agentEnvironment = environmentService.getEnvironment();

	public static void addInstanceName(Map<String, Object> attributes) {
		AgentIdentity agentIdentity = agentEnvironment.getAgentIdentity();
		String instanceId = agentIdentity != null ? agentIdentity.getInstanceName() : null;
		addValue(attributes, "Agent-InstanceName", instanceId);
	}
	
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
	
	public static void addChannelFull(Map<String,Object> attributes, Channel channel) {
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
			addValue(attributes, "Channel-AdapterSWCV", channel.getAdapterSWCV());
			addValue(attributes, "Channel-FromPartyAgency", channel.getFromPartyAgency());
			addValue(attributes, "Channel-FromPartySchema", channel.getFromPartySchema());
			addValue(attributes, "Channel-MsgProt", channel.getMsgProt());
			addValue(attributes, "Channel-MsgProtVers", channel.getMsgProtVers());
			addValue(attributes, "Channel-ObjectId", channel.getObjectId());
			addValue(attributes, "Channel-ObjectName", channel.getObjectName());
			addValue(attributes, "Channel-ObjectType", channel.getObjectType());
			addValue(attributes, "Channel-ToPartyAgency", channel.getToPartyAgency());
			addValue(attributes, "Channel-ToPartySchema", channel.getToPartySchema());
			addValue(attributes, "Channel-TransProt", channel.getTransProt());
			addValue(attributes, "Channel-TransProtVers", channel.getTransProtVers());
		}
	}
		
	
	public static void addChannelState(Map<String,Object> attributes, ChannelState state) {
		if(state != null) {
			addValue(attributes, "ChannelState", state);
		}
	}
	
	public static void addValue(Map<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			String objectString = value.toString();
			// don't report empty values
			if(!objectString.isEmpty()) {
				attributes.put(key, value);
			}
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
