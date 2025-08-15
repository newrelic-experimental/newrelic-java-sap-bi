package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.adapter.xi.ms.XIMessage;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.sdk.xi.mo.MessageContext;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;

public class AttributeChecker extends Thread {

	public static boolean initialized = false;
	
	private static BlockingQueue<DataHolder> queue = new LinkedBlockingQueue<DataHolder>();
	
	public static void addDataToQueue(DataHolder holder) {
		queue.add(holder);
	}
	
	public static void startChecker() {
		AttributeChecker checker = new AttributeChecker();
		checker.start();
		initialized = true;
	}
	
	private AttributeChecker() {
	}


	@SuppressWarnings("rawtypes")
	@Override
	public void run() {
		
		while(true) {
			try {
				DataHolder holder = queue.take();
				
				if (holder instanceof ModuleDataHolder) {
					ModuleDataHolder moduleHolder = (ModuleDataHolder)holder;
					ModuleContext moduleContext = moduleHolder.moduleContext;
					ModuleData moduleData = moduleHolder.moduleData;
					Object principalData = moduleData != null ? moduleData.getPrincipalData() : null;
					MessageKey messageKey = null;
					if (principalData != null) {
						if (principalData instanceof Message) {
							Message message = (Message) principalData;
							messageKey = message.getMessageKey();
						}
					}
					AdapterMonitorLogger.logMessage("Processing attributes for " + messageKey + " from ModuleContext and ModuleData: " + moduleContext + ", " + moduleData);
					long start = System.currentTimeMillis();
					Map<String, String> values = new LinkedHashMap<String, String>();
					if (moduleContext != null) {
						String channelId = moduleContext.getChannelID();
						values.put("ChannelId", channelId);
						Enumeration keys = moduleContext.getContextDataKeys();
						while (keys.hasMoreElements()) {
							String key = keys.nextElement().toString();
							String value = moduleContext.getContextData(key);
							if (value != null) {
								values.put(key.toLowerCase(), value);
							}
						}
					}
					if (moduleData != null) {
						Enumeration supplementalNames = moduleData.getSupplementalDataNames();
						while (supplementalNames.hasMoreElements()) {
							String key = supplementalNames.nextElement().toString();
							Object value = moduleData.getSupplementalData(key);
							values.put(key.toLowerCase(), value.toString());
						} 
						
					}
					if (messageKey != null && values != null && !values.isEmpty()) {
						AttributeProcessor.setAttributes(messageKey, values);
					}
					if(principalData != null) {
						processMessageAttributes(principalData);
					}
					
					long end = System.currentTimeMillis();
					NewRelic.recordMetric("/SAP/AttributeProcess/TimeToProcess", end - start);
				} else if(holder instanceof MapDataHolder) {
					MapDataHolder mapHolder = (MapDataHolder)holder;
					Map<String, String> attributes = mapHolder.attributes;
					MessageKey msgKey = mapHolder.messageKey;
					AdapterMonitorLogger.logMessage("Processing attributes " + msgKey + " from Map: " + attributes);
					AttributeProcessor.setAttributes(msgKey, attributes);
				}
			} catch (InterruptedException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Error occurred trying to take holder from queue");
			}
			
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	private static void processMessageAttributes(Object principal) {
		if(principal instanceof XIMessage) {
			XIMessage xiMessage = (XIMessage)principal;
			MessageKey messageKey = xiMessage.getMessageKey();
			MessageContext messageContext = xiMessage.getMessageContext();
			HashMap<String, String> input_attributes = new HashMap<String, String>();
			Enumeration input_keys = messageContext.getAttributeKeys(0);
			while(input_keys.hasMoreElements()) {
				String key = input_keys.nextElement().toString();
				Object valueObject = messageContext.getAttribute(key, 0);
				
				String value = valueObject != null ? valueObject.toString() : null;
				if(value != null) {
					input_attributes.put(key, value);
				}
			}
			if(!input_attributes.isEmpty()) {
				input_attributes.put(AttributeProcessor.MAP_TYPE, AttributeProcessor.INPUT);
				AttributeProcessor.setAttributes(messageKey, input_attributes);
			}
			HashMap<String, String> output_attributes = new HashMap<String, String>();
			Enumeration output_keys = messageContext.getAttributeKeys(1);
			while(output_keys.hasMoreElements()) {
				String key = output_keys.nextElement().toString();
				Object valueObject = messageContext.getAttribute(key, 1);
				
				String value = valueObject != null ? valueObject.toString() : null;
				if(value != null) {
					output_attributes.put(key, value);
				}
			}
			if(!output_attributes.isEmpty()) {
				output_attributes.put(AttributeProcessor.MAP_TYPE, AttributeProcessor.OUTPUT);
				AttributeProcessor.setAttributes(messageKey, output_attributes);
			}
			
		}
	}
}
