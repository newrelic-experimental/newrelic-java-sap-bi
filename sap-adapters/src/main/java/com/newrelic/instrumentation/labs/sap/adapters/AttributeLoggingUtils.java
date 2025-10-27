package com.newrelic.instrumentation.labs.sap.adapters;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessagePropertyKey;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;
import com.sap.engine.interfaces.messaging.spi.transport.Endpoint;

public class AttributeLoggingUtils  {
	
	private static final HashSet<String> unidentified = new HashSet<String>();
	private static AttributeLoggingUtils INSTANCE = null;
	private static Producer producer = null;
	private static Future<?> consumerTask = null;
	private static Consumer consumer = null;
	
	public static AttributeLoggingUtils getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new AttributeLoggingUtils();
		}
		return INSTANCE;
	}
	
	private AttributeLoggingUtils() {
	}

	public static void logAdapterDetails(String methodDirection, ModuleContext context, ModuleData data) {
		LoggingInfo info = new LoggingInfo(methodDirection, data, context);
		addEntry(info);
	}
	
	private static void addEntry(LoggingInfo info) {
		if(producer == null) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "AttributeLoggingUtils Producer is null, creating");
			if(consumer != null) {
				if(consumerTask == null) {
					start();
				} else {
					producer = new Producer(consumer.getQueue());
				}
			} else {
				start();
			}
		}
		boolean added = producer.addEntry(info);
		if(!added) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Logging info not added to queue: {0}",info);
		}
	}
	
	
	public static void start() {
		ArrayBlockingQueue<LoggingInfo> entries = new ArrayBlockingQueue<AttributeLoggingUtils.LoggingInfo>(1000);	
		consumer = new Consumer(entries);
		consumerTask = Executors.newSingleThreadExecutor().submit(consumer);
		producer = new Producer(entries);
	}
	
	
	@SuppressWarnings("rawtypes")
	private static void log(String methodDir, ModuleContext context, ModuleData data) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		
		if(context != null) {
			String channelId = context.getChannelID();
			addAttribute(attributes, "ChannelId", channelId);
			Enumeration keys = context.getContextDataKeys();
			if(keys != null) {
				while(keys.hasMoreElements()) {
					String key = keys.nextElement().toString();
					String value = context.getContextData(key);
					addAttribute(attributes, "ModuleContext-"+key, value);
				}
			}
		}
		if(data != null) {
			Enumeration keys = data.getSupplementalDataNames();
			if(keys != null) {
				while(keys.hasMoreElements()) {
					String key = keys.nextElement().toString();
					Object value = data.getSupplementalData(key);
					addAttribute(attributes, "SupplementalData-" + key, value);
				}
			}
			Object principalObject = data.getPrincipalData();
			if (principalObject != null) {
				Map<String, Object> principalAttributues = addPrincipalData(principalObject);
				if (principalAttributues != null && !principalAttributues.isEmpty()) {
					attributes.putAll(principalAttributues);
				} 
			}
		}
		if(!attributes.isEmpty()) {
			attributes.put("Method-Direction", methodDir);
			LoggingUtils.logAttributes(attributes);
		}

	}
	
	private static Map<String, Object> addPrincipalData(Object principalData) {
		
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		boolean typeIdentified = false;
		if(principalData instanceof Message) {
			typeIdentified = true;
			Message message = (Message)principalData;
			addMessage(attributes, message);
		}
		if(principalData instanceof TransportableMessage) {
			TransportableMessage message = (TransportableMessage)principalData;
			addTransportableMessage(attributes, message);
			typeIdentified = true;
		}
		
		if(!typeIdentified) {
			String classname = principalData.getClass().getName();
			if(!unidentified.contains(classname)) {
				NewRelic.getAgent().getLogger().log(Level.INFO, "The principalData type {0} was not handled", classname);
				unidentified.add(classname);
			}
		}
		return attributes;
	}
	
	private static void addMessage(Map<String, Object> attributes, Message message) {
		if(message != null) {
			addAttribute(attributes, "Message-CorrelationId", message.getCorrelationId());
			addAttribute(attributes, "Message-MessageId", message.getMessageId());
			addAttribute(attributes, "Message-Protocol", message.getProtocol());
			addAttribute(attributes, "Message-RefToMessageId", message.getRefToMessageId());
			addAttribute(attributes, "Message-SequenceId", message.getSequenceId());
			addAttribute(attributes, "Message-Action", message.getAction().toString());
			addAttribute(attributes, "Message-DeliverySematics", message.getDeliverySemantics().toString());
			addAttribute(attributes, "Message-FromParty", message.getFromParty().toString());
			addAttribute(attributes, "Message-FromService", message.getFromService().toString());
			addAttribute(attributes, "Message-ToParty", message.getToParty().toString());
			addAttribute(attributes, "Message-ToService", message.getToService().toString());
			addAttribute(attributes, "Message-MessageClass", message.getMessageClass().toString());
			addAttribute(attributes, "Message-MessageDirection", message.getMessageDirection().toString());
			addAttribute(attributes, "Message-MessageKey", message.getMessageKey().toString());
			addAttribute(attributes, "Message-TimeReceived", message.getTimeReceived());
			addAttribute(attributes, "Message-TimeSent", message.getTimeSent());
			Set<MessagePropertyKey> msgPropertyKeys = message.getMessagePropertyKeys();
			for(MessagePropertyKey propKey : msgPropertyKeys) {
				String propValue = message.getMessageProperty(propKey);
				addAttribute(attributes, "Message-Property-"+propKey.toString(), propValue);
			}
		}
	}
	
	
	private static void addTransportableMessage(Map<String,Object> attributes, TransportableMessage message) {
		if(message != null) {
			addAttribute(attributes, "TransportableMessage-PrincipalPropagationUserName", message.getPrincipalPropagationUserName());
			addEndPoint(attributes, message.getEndpoint());
			addAttribute(attributes, "TransportableMessage-MessagePriority", message.getMessagePriority().toString());
			addAttribute(attributes, "TransportableMessage-Retries", message.getRetries());
			addAttribute(attributes, "TransportableMessage-SequenceNumber", message.getSequenceNumber());

		}
	}
	
	private static void addEndPoint(Map<String, Object> attributes, Endpoint endpoint) {
		if(endpoint != null) {
			addAttribute(attributes, "TransportableMessage-Endpoint-Address", endpoint.getAddress());
			addAttribute(attributes, "TransportableMessage-Endpoint-Transport", endpoint.getTransport().toString());
		}
	}
	
	private static void addAttribute(Map<String, Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			String valueString = value.toString();
			if(!valueString.isEmpty()) {
				attributes.put(key, value);
			}
		}
	}
	
	private static class LoggingInfo {
		
		private String methodDir = null;
		private ModuleData data;
		private ModuleContext context;
		
		public LoggingInfo(String methodDir, ModuleData data, ModuleContext context) {
			super();
			this.methodDir = methodDir;
			this.data = data;
			this.context = context;
		}

		@Override
		public String toString() {
			return "LoggingInfo [methodDir=" + methodDir + ", data=" + data + ", context=" + context + "]";
		}
		
		
		
	}
	
	private static class Producer {
		
		private ArrayBlockingQueue<LoggingInfo> entries;

		public Producer(ArrayBlockingQueue<LoggingInfo> queue) {
			entries = queue;
		}
		
		public boolean addEntry(LoggingInfo info) {
			boolean added = entries.add(info);
			NewRelic.recordMetric("SAP/AdapterLogger/LoggingEntries/Size", entries.size());
			return added;
		}
	}
	
	private static class Consumer implements Runnable {
		
		private ArrayBlockingQueue<LoggingInfo> entries;
		
		public Consumer(ArrayBlockingQueue<LoggingInfo> queue) {
			entries = queue;
		}
		
		public ArrayBlockingQueue<LoggingInfo> getQueue() {
			return entries;
		}

		public void run() {
			NewRelic.getAgent().getLogger().log(Level.FINE, "AdapterLoggingUtils Consumer has started");
			while(true) {
				try {
					NewRelic.getAgent().getLogger().log(Level.FINE, "Call to take LoggingInfo from queue, current size {0}",entries.size());
//					LoggingInfo info = entries.poll(10,TimeUnit.SECONDS);
					LoggingInfo info = entries.take();
					if(info != null) {
						NewRelic.getAgent().getLogger().log(Level.FINE, "entries.take returned info {0}",info);
						log(info.methodDir, info.context, info.data);
					} else {
						NewRelic.getAgent().getLogger().log(Level.FINE, "entries.take timed out");
					}
				} catch (Exception e) {
					NewRelic.getAgent().getLogger().log(Level.FINE, e, "Exception while calling entries.take");
				}
			}
		}
		
	}
}
