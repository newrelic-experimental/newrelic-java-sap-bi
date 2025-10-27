package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
	private static int NUMBER_OF_CONSUMERS = 5;
	
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
			NewRelic.recordMetric("/SAP/AttributeProcess/HolderNotAdded", 1.0f);
		}
	}

	public static void startChecker() {
		try {
			for(int i = 1;i<=NUMBER_OF_CONSUMERS;i++) {
				AttributeChecker checker = new AttributeChecker();
				NewRelicExecutors.addRunnableToThreadPool(checker);
				AdapterMonitorLogger.logMessage("AttributeChecker "+ i +" has been started");		
			}
			initialized = true;
		} catch (Exception e) {
			AdapterMonitorLogger.logErrorWithMessage("AttributeChecker failed to started",e);		
			initialized = false;
		}
		NewRelicExecutors.addScheduledTaskAtFixedRate(() -> {
			NewRelic.recordMetric("/SAP/AttributeProcess/HolderQueueSize", queue.size());
		}, 1, 1, TimeUnit.MINUTES);
	}
	
	private AttributeChecker() {
	}


	@Override
	public void run() {
		AdapterMonitorLogger.logMessage("Call to AttributeChecker.run()");
		
		while(true) {
			try {
				DataHolder holder = queue.take();
				AdapterMonitorLogger.logMessage("Popped Dataholder off queue: " + holder);
				Future<?> future = NewRelicExecutors.addRunnableToThreadPool(() -> {
					processDataHolder(holder);
				});
				
				try {
					Object result = future.get(1, TimeUnit.SECONDS);
					if(result != null) {
					}
				} catch (ExecutionException e) {
					AdapterMonitorLogger.logErrorWithMessage("Call to process dataholder was cancelled due to ExecutionException", e);
					NewRelic.recordMetric("/SAP/AttributeProcess/ProcessingFromQueue/ExecutionException", 1.0f);
					future.cancel(true);
				} catch (TimeoutException e) {
					AdapterMonitorLogger.logErrorWithMessage("Call to process dataholder was cancelled due to TimeoutException", e);
					NewRelic.recordMetric("/SAP/AttributeProcess/ProcessingFromQueue/Timeout", 1.0f);
					future.cancel(true);
				}
				
			} catch (InterruptedException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Error occurred trying to take holder from queue");
				AdapterMonitorLogger.logErrorWithMessage("Error occurred trying to take holder from queue",e);			
			}
			
		}
		
	}
	

	@SuppressWarnings("rawtypes")
	private void processDataHolder(DataHolder holder) {
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
					AttributeMonitorLogger.addAttribute(key, "ModuleContext");
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
					AttributeMonitorLogger.addAttribute(key, "ModuleData-Supplemental");
					Object value = moduleData.getSupplementalData(key);
					values.put(key.toLowerCase(), value.toString());
				} 
				
			}
			if(principalData != null) {
				Map<String,String> attributesFromMsg = AttributeProcessor.recordObject(principalData);
				if(attributesFromMsg != null && !attributesFromMsg.isEmpty()) {
					Set<String> keys = attributesFromMsg.keySet();
					for(String key : keys) {
						AttributeMonitorLogger.addAttribute(key, "MessageDocument");
					}
					values.putAll(attributesFromMsg);
				}
			}

			if (messageKey != null && values != null && !values.isEmpty()) {
				AttributeProcessor.setAttributes(messageKey, values);
			}
			
			
			long end = System.currentTimeMillis();
			NewRelic.recordMetric("/SAP/AttributeProcess/TimeToProcess", end - start);
		}
		
	}
}
