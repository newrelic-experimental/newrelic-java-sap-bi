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

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.Config;
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
	private static int NUMBER_OF_CONSUMERS = 3;
	private int index;

	// Configuration key for polling interval (in seconds)
	private static final String POLLING_INTERVAL_CONFIG_KEY = "SAP.adaptermessagelog.polling_interval_seconds";
	private static final long DEFAULT_POLLING_INTERVAL_SECONDS = 5L; // Default: 5 seconds
	private static volatile long pollingIntervalSeconds = DEFAULT_POLLING_INTERVAL_SECONDS;
	private static volatile boolean configListenerRegistered = false;

	/**
	 * Configuration listener for dynamic polling interval updates
	 * Allows runtime changes without restart
	 */
	private static class PollingIntervalConfigListener implements AgentConfigListener {
		@Override
		public void configChanged(String appName, AgentConfig agentConfig) {
			long oldInterval = pollingIntervalSeconds;
			long newInterval = getPollingIntervalSeconds();

			if (oldInterval != newInterval) {
				pollingIntervalSeconds = newInterval;
				AdapterMonitorLogger.logMessage("Polling interval changed from " + oldInterval +
					" seconds to " + newInterval + " seconds (runtime config reload)");
			}
		}
	}
	
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
		} else {
			NewRelic.recordMetric("/SAP/AttributeProcess/HolderAdded", 1.0f);
			if (AdapterMonitorLogger.isLoggable(Level.FINER)) {
				AdapterMonitorLogger.logMessage(Level.FINER,"added Dataholder to queue: " + holder);
			}
		}
	}

	/**
	 * Read polling interval configuration from newrelic.yml
	 * Defaults to 5 seconds if not configured
	 */
	private static long getPollingIntervalSeconds() {
		try {
			Config agentConfig = NewRelic.getAgent().getConfig();
			// Try reading as Integer first (YAML default for numeric values)
			Integer configuredInterval = agentConfig.getValue(POLLING_INTERVAL_CONFIG_KEY);
			if (configuredInterval != null && configuredInterval > 0) {
				AdapterMonitorLogger.logMessage("Using configured polling interval: " + configuredInterval + " seconds");
				return configuredInterval.longValue();
			}
		} catch (Exception e) {
			AdapterMonitorLogger.logMessage(Level.FINE, "Failed to read polling interval config, using default: " +
				DEFAULT_POLLING_INTERVAL_SECONDS + " seconds");
		}
		AdapterMonitorLogger.logMessage("Using default polling interval: " + DEFAULT_POLLING_INTERVAL_SECONDS + " seconds");
		return DEFAULT_POLLING_INTERVAL_SECONDS;
	}

	public static void startChecker() {
		try {
			// Register config listener for dynamic polling interval updates
			if (!configListenerRegistered) {
				ServiceFactory.getConfigService().addIAgentConfigListener(new PollingIntervalConfigListener());
				configListenerRegistered = true;
				AdapterMonitorLogger.logMessage("Registered polling interval config listener for runtime updates");
			}

			// Read polling interval from configuration
			pollingIntervalSeconds = getPollingIntervalSeconds();
			AdapterMonitorLogger.logMessage("AttributeChecker polling interval set to: " + pollingIntervalSeconds + " seconds");

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
		AdapterMonitorLogger.logMessage(Level.FINE,"Initializing running of checker #" + this.index +
			" with polling interval: " + pollingIntervalSeconds + " seconds");

		while(true) {
			try {
				// Use configurable polling interval (default: 5 seconds)
				// This allows messages to complete processing (DLNG → DLVD) before querying SAP API
				DataHolder holder = queue.poll(pollingIntervalSeconds, TimeUnit.SECONDS);
				if (holder != null) {
					Set<DataHolder> dataHoldersToProcess = new LinkedHashSet<DataHolder>();
					dataHoldersToProcess.add(holder);
					queue.drainTo(dataHoldersToProcess, 49);

					AdapterMonitorLogger.logMessage(Level.FINE,"Processing DataHolders #" + dataHoldersToProcess.size());
					processDataHolders(dataHoldersToProcess);
				}
			} catch (InterruptedException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "AttributeChecker interrupted", e);
				// Restore interrupted status
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Error processing data holders");
				AdapterMonitorLogger.logErrorWithMessage("Error processing data holders",e);
			}
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	private void processDataHolders(Collection<DataHolder> dataHolders) {
		// Collect ALL captures per MessageKey (not just one)
		Map<MessageKey, List<Map<String,String>>> allCapturesPerMessageKey = new HashMap<>();
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
				// Collect ALL captures for this MessageKey (not just one)
				if(messageKey != null) {
					allCapturesPerMessageKey.computeIfAbsent(messageKey, k -> new ArrayList<>()).add(values);
				} else {
					NewRelic.getAgent().getLogger().log(Level.FINE, "MessageKey is null for ModuleDataHolder, skipping attribute mapping");
					NewRelic.recordMetric("SAP/AttributeProcess/NullMessageKey", 1.0f);
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

				// Collect ALL captures for this MessageKey (not just one)
				if(messageKey != null) {
					allCapturesPerMessageKey.computeIfAbsent(messageKey, k -> new ArrayList<>()).add(values);
				} else {
					NewRelic.getAgent().getLogger().log(Level.FINE, "MessageKey is null for MessageAndContextHolder, skipping attribute mapping");
					NewRelic.recordMetric("SAP/AttributeProcess/NullMessageKey", 1.0f);
				}
				long end = System.currentTimeMillis();
				NewRelic.recordMetric("SAP/AttributeProcess/TimeToProcessKey", end-start);
				NewRelic.recordMetric("SAP/AttributeProcess/MessageContextDataHolder", 1.0f);

			}
		}

		// Select best capture for each MessageKey
		Map<MessageKey, Map<String,String>> attributeMappings = selectBestCapturesPerMessageKey(allCapturesPerMessageKey);

		AdapterMonitorLogger.logMessage(Level.FINE,"Found " + attributeMappings.size() + " unique MessageKeys after deduplication");
		if(!attributeMappings.isEmpty()) {
			AttributeProcessor.processAttributes(attributeMappings);
		}
		long endAll = System.currentTimeMillis();
		NewRelic.recordMetric("SAP/AttributeProcess/TimeToProcessAllKeys", endAll-startOfAll);

	}

	/**
	 * Selects the best capture for each MessageKey
	 * Best = capture with most complete fields (highest score)
	 */
	private Map<MessageKey, Map<String,String>> selectBestCapturesPerMessageKey(
			Map<MessageKey, List<Map<String,String>>> allCapturesPerMessageKey) {

		Map<MessageKey, Map<String,String>> bestCapturesPerMessageKey = new HashMap<>();

		for (Map.Entry<MessageKey, List<Map<String,String>>> entry : allCapturesPerMessageKey.entrySet()) {
			MessageKey msgKey = entry.getKey();
			List<Map<String,String>> captures = entry.getValue();

			if (captures.isEmpty()) {
				continue;
			}

			if (captures.size() == 1) {
				// Only one capture, use it
				bestCapturesPerMessageKey.put(msgKey, captures.get(0));
				continue;
			}

			// Multiple captures - select the most complete one
			Map<String,String> bestCapture = selectMostComplete(captures);
			bestCapturesPerMessageKey.put(msgKey, bestCapture);

			AdapterMonitorLogger.logMessage(Level.FINE,
				"Selected best capture for " + msgKey + " from " + captures.size() + " captures");
			NewRelic.recordMetric("SAP/AttributeProcess/MultipleCapturesDeduped", captures.size() - 1);
		}

		return bestCapturesPerMessageKey;
	}

	/**
	 * Selects the most complete capture from multiple captures
	 * Score = count of non-empty, non-"Not_Reported" fields
	 */
	private Map<String,String> selectMostComplete(List<Map<String,String>> captures) {
		Map<String,String> bestCapture = null;
		int bestScore = -1;

		for (Map<String,String> capture : captures) {
			int score = calculateCompleteness(capture);

			if (score > bestScore) {
				bestScore = score;
				bestCapture = capture;
			}
		}

		return bestCapture != null ? bestCapture : captures.get(0);
	}

	/**
	 * Calculates completeness score for a capture
	 * Score = count of fields with actual values (not null, not empty, not "Not_Reported")
	 */
	private int calculateCompleteness(Map<String,String> attributes) {
		if (attributes == null || attributes.isEmpty()) {
			return 0;
		}

		int score = 0;

		for (String value : attributes.values()) {
			if (value != null &&
				!value.isEmpty() &&
				!"Not_Reported".equals(value)) {
				score++;
			}
		}

		return score;
	}

}
