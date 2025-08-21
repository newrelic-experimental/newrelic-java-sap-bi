package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;

public class AttributeChecker implements Runnable {

	public static boolean initialized = false;
	
	private static final int MAX = 100000;
	private static BlockingQueue<DataHolder> queue = new LinkedBlockingQueue<DataHolder>(MAX);
	
	public static void addDataToQueue(DataHolder holder) {
		if(queue.remainingCapacity() < 100) {
			List<DataHolder> temp = new ArrayList<DataHolder>();
			int n = queue.drainTo(temp, 20000);
			temp.clear();
			AdapterMonitorLogger.logMessage("Removed " + n + " Dataholder entries due to capacity constraints");
		}
		boolean added = queue.add(holder);
		if(!added) {
			AdapterMonitorLogger.logMessage("Failed to add Dataholder to queue: " + holder);
		}
	}
	
	public static void startChecker() {
		try {
			AttributeChecker checker = new AttributeChecker();
			NewRelicExecutors.addRunnableToThreadPool(checker);
			AdapterMonitorLogger.logMessage("AttributeChecker has been started");			
			initialized = true;
		} catch (Exception e) {
			AdapterMonitorLogger.logErrorWithMessage("AttributeChecker failed to started",e);		
			initialized = false;
		}
	}
	
	private AttributeChecker() {
	}


	@SuppressWarnings("rawtypes")
	@Override
	public void run() {
		AdapterMonitorLogger.logMessage("Call to AttributeChecker.run()");
		
		while(true) {
			try {
				DataHolder holder = queue.take();
				AdapterMonitorLogger.logMessage("Popped Dataholder off queue: " + holder);
				
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
					if(principalData != null) {
						Map<String,String> attributesFromMsg = AttributeProcessor.recordObject(principalData);
						if(attributesFromMsg != null && !attributesFromMsg.isEmpty()) {
							values.putAll(attributesFromMsg);
						}
					}

					if (messageKey != null && values != null && !values.isEmpty()) {
						AttributeProcessor.setAttributes(messageKey, values);
					}
					
					MessageMonitor.addMessageKeyToProcess(messageKey);
					
					long end = System.currentTimeMillis();
					NewRelic.recordMetric("/SAP/AttributeProcess/TimeToProcess", end - start);
				}
			} catch (InterruptedException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Error occurred trying to take holder from queue");
				AdapterMonitorLogger.logErrorWithMessage("Error occurred trying to take holder from queue",e);			
			}
			
		}
		
	}
	

}
