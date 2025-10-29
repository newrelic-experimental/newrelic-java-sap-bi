package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;

public class AttributeChecker implements Runnable {

	public static boolean initialized = false;
	
	private static final int MAX = 100000;
	private static BlockingQueue<DataHolder> queue = new LinkedBlockingQueue<DataHolder>(MAX);
	private static int NUMBER_OF_CONSUMERS = 5;
	private static final long DELAY = (NUMBER_OF_CONSUMERS+1) * 60000L;
	private int index;
	
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
				AttributeChecker checker = new AttributeChecker(i);
				NewRelicExecutors.addScheduledTask(() -> {
					NewRelicExecutors.addRunnableToThreadPool(checker);
				}, i-1,TimeUnit.MINUTES);
				AdapterMonitorLogger.logMessage("Started AttributeChecker #" + checker.index);
						
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
	
	private AttributeChecker(int index) {
		this.index = index;
	}


	@Override
	public void run() {
		
		while(true) {
			try {
				Set<DataHolder> dataHoldersToProcess = new LinkedHashSet<DataHolder>();
				int toProcess = queue.drainTo(dataHoldersToProcess, 50);
				
				try {
					if(toProcess > 0) {
						processDataHolders(dataHoldersToProcess);
					}
				} catch (Exception e) {
					NewRelic.getAgent().getLogger().log(Level.FINER, e, "Error occurred trying to take holder from queue");
					AdapterMonitorLogger.logErrorWithMessage("Error occurred trying to take holder from queue",e);			
				}
				
				Timer timer = new Timer();
				CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
				
				TimerTask waitTask = new TimerTask() {
					
					@Override
					public void run() {
						future.complete(true);
						
					}
				};
				
				timer.schedule(waitTask, DELAY);
				
				try {
					future.get(NUMBER_OF_CONSUMERS+1, TimeUnit.MINUTES);
				} catch (Exception e) {
				}
				
				
			} catch (Exception e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Error occurred trying to take holder from queue");
				AdapterMonitorLogger.logErrorWithMessage("Error occurred trying to take holder from queue",e);			
			}
			
		}
		
	}
	
	
	@SuppressWarnings("rawtypes")
	private void processDataHolders(Collection<DataHolder> dataHolders) {
		Map<MessageKey, Map<String,String>> attributeMappings = new HashMap<MessageKey, Map<String,String>>();
		long startOfAll = System.currentTimeMillis();
		
		for (DataHolder holder : dataHolders) {
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
					Map<String,Map<String,String>> attributesFromMsg = AttributeProcessor.recordObject(principalData);
					if(attributesFromMsg != null && !attributesFromMsg.isEmpty()) {
						Map<String,String> msgAttributes = attributesFromMsg.get(AttributeProcessor.MESSAGE_DOCUMENT);
						if (msgAttributes != null) {
							Set<String> keys = msgAttributes.keySet();
							for (String key : keys) {
								AttributeMonitorLogger.addAttribute(key, AttributeProcessor.MESSAGE_DOCUMENT);
							}
							values.putAll(msgAttributes);
							Map<String, String> payloadAttributes = attributesFromMsg
									.get(AttributeProcessor.PAYLOAD_ATTRIBUTES);
							for (String key : payloadAttributes.keySet()) {
								AttributeMonitorLogger.addAttribute(key, AttributeProcessor.PAYLOAD_ATTRIBUTES);
							}
							values.putAll(payloadAttributes);
							Map<String, String> msgProperties = attributesFromMsg
									.get(AttributeProcessor.MESSAGE_PROPERTIES);
							values.putAll(msgProperties);
						}
						
					}
				}
				if(attributeMappings.containsKey(messageKey)) {
					Map<String,String> existing = attributeMappings.get(messageKey);
					if(existing != null) {
						values.putAll(existing);
					}					
					attributeMappings.put(messageKey, values);
				} else {
					attributeMappings.put(messageKey, values);
				}
				long end = System.currentTimeMillis();
				NewRelic.recordMetric("SAP/AttributeProcess/TimeToProcessKey", end-start);
				NewRelic.recordMetric("SAP/AttributeProcess/ModuleDataHolder", 1.0f);
			} else if(holder instanceof MessageAndContextHolder) {
				long start = System.currentTimeMillis();
				MessageAndContextHolder msgCtxHolder = (MessageAndContextHolder)holder;
				TransportableMessage msg = msgCtxHolder.getMessage();
				MessageKey messageKey = msg.getMessageKey();
				Map<String, String> values = new LinkedHashMap<String, String>();
				Map<String,Map<String,String>> attributesFromMsg = AttributeProcessor.recordObject(msg);
				if(attributesFromMsg != null && !attributesFromMsg.isEmpty()) {
					Map<String,String> msgAttributes = attributesFromMsg.get(AttributeProcessor.MESSAGE_DOCUMENT);
					Set<String> keys = msgAttributes.keySet();
					for(String key : keys) {
						AttributeMonitorLogger.addAttribute(key, AttributeProcessor.MESSAGE_DOCUMENT);
					}
					values.putAll(msgAttributes);
					
					Map<String,String> payloadAttributes = attributesFromMsg.get(AttributeProcessor.PAYLOAD_ATTRIBUTES);
					for(String key : payloadAttributes.keySet()) {
						AttributeMonitorLogger.addAttribute(key, AttributeProcessor.PAYLOAD_ATTRIBUTES);
					}
					values.putAll(payloadAttributes);
					
					Map<String,String> msgProperties = attributesFromMsg.get(AttributeProcessor.MESSAGE_PROPERTIES);
					values.putAll(msgProperties);
					
				}

				if(attributeMappings.containsKey(messageKey)) {
					Map<String,String> existing = attributeMappings.get(messageKey);
					if(existing != null) {
						values.putAll(existing);
					}					
					attributeMappings.put(messageKey, values);
				} else {
					attributeMappings.put(messageKey, values);
				}
				long end = System.currentTimeMillis();
				NewRelic.recordMetric("SAP/AttributeProcess/TimeToProcessKey", end-start);
				NewRelic.recordMetric("SAP/AttributeProcess/MessageContextDataHolder", 1.0f);

			}
		}
		
		if(!attributeMappings.isEmpty()) {
			AttributeProcessor.processAttributes(attributeMappings);
		}
		long endAll = System.currentTimeMillis();
		NewRelic.recordMetric("SAP/AttributeProcess/TimeToProcessAllKeys", endAll-startOfAll);
		
	}

}
