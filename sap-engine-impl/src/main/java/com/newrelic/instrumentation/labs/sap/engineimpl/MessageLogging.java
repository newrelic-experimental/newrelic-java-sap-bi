package com.newrelic.instrumentation.labs.sap.engineimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.newrelic.agent.environment.AgentIdentity;
import com.newrelic.agent.environment.Environment;
import com.newrelic.agent.environment.EnvironmentService;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.NewRelic;
import com.sap.engine.interfaces.messaging.api.APIAccess;
import com.sap.engine.interfaces.messaging.api.APIAccessFactory;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.exception.InvalidParamException;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;
import com.sap.engine.interfaces.messaging.api.message.MessageAccess;
import com.sap.engine.interfaces.messaging.api.message.MessageAccessException;
import com.sap.engine.interfaces.messaging.api.message.MessageData;
import com.sap.engine.interfaces.messaging.api.message.MonitorData;
import com.sap.engine.messaging.impl.api.message.MessageDataFilterImpl;

public class MessageLogging implements Runnable {
	
	public static boolean initialized = false;
	private Date startDate;
	private static String[] dateTypes = {"SentReceive","TransmitDeliver","NextDelivery","ValidUntil","PersistUntil"};
	List<String> recorded = new ArrayList<String>();
	private static EnvironmentService environmentService = ServiceFactory.getEnvironmentService();
	private static Environment agentEnvironment = environmentService.getEnvironment();

	public static void addInstanceName(Map<String, Object> attributes) {
		AgentIdentity agentIdentity = agentEnvironment.getAgentIdentity();
		String instanceId = agentIdentity != null ? agentIdentity.getInstanceName() : null;
		reportValue(attributes, "Agent-InstanceName", instanceId);
	}
	
	
	public static void init() {
		initialized = true;
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new MessageLogging(), 1, 5, TimeUnit.MINUTES);
	}
	
	protected MessageLogging() {
		startDate = new Date();
	}

	@Override
	public void run() {
		try {
			APIAccess apiAccess = APIAccessFactory.getAPIAccess();

			if(apiAccess ==  null) return;
			
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			attributes.put("StartDate", startDate);
			Date endDate = new Date();
			attributes.put("EndDate", endDate);
			
			MessageAccess msgAccess = apiAccess.getMessageAccess();
			List<String> copy = new ArrayList<String>();
			copy.addAll(recorded);
			
			MessageDataFilterImpl filter = new MessageDataFilterImpl();
			for (int i = 1; i <= 5; i++) {
				filter.addFilterByDate(i, startDate, endDate);
				MonitorData monitor = msgAccess.getMonitorData(filter);
				int msgCount = monitor.getTotalMessageCount();
				LinkedList<MessageData> messageDatas = monitor.getMessageData();
				attributes.put("MessageCount-"+dateTypes[i-1], msgCount);
				attributes.put("MessageDataSize-"+dateTypes[i-1], messageDatas.size());
				MessageData msgData = messageDatas.poll();
				int reported = 0;
				
				while (msgData != null) {
					String msgId = msgData.getMessageID();
					if(!recorded.contains(msgId)) {
						reportMessage(msgData);
						recorded.add(msgId);
						reported++;
					} else {
						copy.remove(msgId);
					}
					msgData = messageDatas.poll();
				} 
				attributes.put("MessageReported-"+dateTypes[i-1], reported);
			}
			addInstanceName(attributes);
			NewRelic.getAgent().getInsights().recordCustomEvent("MessageCollection", attributes);
			startDate = endDate;
			recorded.removeAll(copy);
		} catch (InvalidParamException e) {
			NewRelic.getAgent().getLogger().log(Level.FINER, e, "Error getting message datas");
		} catch (MessageAccessException e) {
			NewRelic.getAgent().getLogger().log(Level.FINER, e, "Error getting message datas");
		} catch (MessagingException e) {
			NewRelic.getAgent().getLogger().log(Level.FINER, e, "Error getting message datas");
		}
		
	}

	private static void reportMessage(MessageData msgData) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		reportMessageKey(attributes, msgData.getMessageKey());
		reportValue(attributes, "MessageStatus", msgData.getStatus());
		reportValue(attributes,"CorrelationId",msgData.getCorrelationID());
		reportValue(attributes,"Address",msgData.getAddress());
		NewRelic.getAgent().getInsights().recordCustomEvent("MessageLog", attributes);
	}
	
	private static void reportMessageKey(Map<String,Object> attributes,MessageKey msgKey) {
		if(msgKey != null) {
			reportValue(attributes, "MessageKey-MessageId", msgKey.getMessageId());
			reportValue(attributes, "MessageKey-Direction", msgKey.getDirection());
		}
	}
	
	private static void reportValue(Map<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}
}
