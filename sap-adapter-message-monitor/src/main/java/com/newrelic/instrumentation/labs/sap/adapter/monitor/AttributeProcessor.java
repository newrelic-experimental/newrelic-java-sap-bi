package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.adapter.xi.ms.XIMessage;
import com.sap.aii.adapter.xi.validator.ModuleContextImpl;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.sdk.xi.mo.MessageContext;
import com.sap.engine.interfaces.messaging.api.APIAccess;
import com.sap.engine.interfaces.messaging.api.APIAccessFactory;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.MessagePropertyKey;
import com.sap.engine.interfaces.messaging.api.MessageStatus;
import com.sap.engine.interfaces.messaging.api.XMLPayload;
import com.sap.engine.interfaces.messaging.api.exception.InvalidParamException;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;
import com.sap.engine.interfaces.messaging.api.message.MessageAccess;
import com.sap.engine.interfaces.messaging.api.message.MessageAccessException;
import com.sap.engine.interfaces.messaging.api.message.MessageData;
import com.sap.engine.interfaces.messaging.api.message.MessageDataFilter;
import com.sap.engine.interfaces.messaging.api.message.MonitorData;
import com.sap.engine.interfaces.messaging.spi.AbstractMessage;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;

public class AttributeProcessor {

	// Cache: Persists business fields across API calls
	private static final Map<String, CachedBusinessFields> businessFieldsCache =
		new ConcurrentHashMap<>();

	// Buffer: Holds incomplete entries, indexed by MessageId (O(1) lookups)
	private static final Map<String, List<BufferedMessageData>> messageDataBuffer =
		new ConcurrentHashMap<>();

	// Cleanup old cache and buffer entries periodically
	static {
		try {
			NewRelicExecutors.addScheduledTaskAtFixedRate(() -> {
				cleanupExpiredCache();
				processTimedOutBufferedEntries();
			}, 5, 5, TimeUnit.MINUTES);
		} catch (Exception e) {
			NewRelic.getAgent().getLogger().log(Level.WARNING, e,
				"Failed to schedule cache/buffer cleanup task");
		}
	}

	protected static final String MESSAGE_DOCUMENT = "MessageDocument";
	protected static final String PAYLOAD_ATTRIBUTES = "PayLoad-Attributes";
	protected static final String MESSAGE_PROPERTIES = "Message-Property";
	protected static final String MESSAGECONTEXT_IN_ATTRIBUTES = "MessageContext-In-Attributes";
	protected static final String MESSAGECONTEXT_OUT_ATTRIBUTES = "MessageContext-Out-Attributes";
	private static APIAccess apiAccess = null;

	public static final String MAP_TYPE = "MapType";
	
	private static APIAccess getAPIAccess() {
		if(apiAccess == null) {
			try {
				apiAccess = APIAccessFactory.getAPIAccess();
			} catch (MessagingException e) {
			}
		}
		return apiAccess;
	}


	protected static void processAttributes(Map<MessageKey, Map<String,String>> attributeMappings ) {
		AdapterMonitorLogger.logMessage(Level.FINE,"processAttributes call has " + attributeMappings.size() + " attribute mappings");
		long startTime = System.nanoTime();

		List<MessageKey> msgKeys = new ArrayList<MessageKey>();
		msgKeys.addAll(attributeMappings.keySet());

		// Remove null keys to prevent NullPointerException
		msgKeys.removeIf(key -> key == null);

		if(msgKeys.isEmpty()) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "No valid message keys to process");
			return;
		}

		String[] msgKeysArray = new String[msgKeys.size()];
		int index = 0;

		for(MessageKey key : msgKeys) {
			// Double-check for null before calling getMessageId()
			if(key != null) {
				String messageId = key.getMessageId();
				if(messageId != null && !messageId.isEmpty()) {
					msgKeysArray[index] = messageId;
					index++;
				} else {
					NewRelic.getAgent().getLogger().log(Level.FINE, "MessageKey has null or empty messageId, skipping");
				}
			}
		}

		// Adjust array size if some keys were skipped
		if(index < msgKeysArray.length) {
			String[] adjustedArray = new String[index];
			System.arraycopy(msgKeysArray, 0, adjustedArray, 0, index);
			msgKeysArray = adjustedArray;
		}

		if(msgKeysArray.length == 0) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "No valid message IDs to process");
			return;
		}

		try {
			MessageAccess messageAccess = getAPIAccess().getMessageAccess();
			MessageDataFilter filter = messageAccess.createMessageDataFilter();
			
			filter.setMessageIds(msgKeysArray);
			MonitorData data = messageAccess.getMonitorData(filter);
			LinkedList<MessageData> msgDataList = data.getMessageData();
			HashMap<MessageKey, List<MessageData>> dataMapping = new HashMap<MessageKey, List<MessageData>>();
			for(MessageData msgData : msgDataList) {
				MessageKey msgKey = msgData.getMessageKey();
				List<MessageData> list = dataMapping.get(msgKey);
				if(list == null) {
					list = new ArrayList<MessageData>();
					list.add(msgData);
				} else {
					list.add(msgData);
				}
				dataMapping.put(msgKey, list);
			}


			// Process each MessageKey
			for(MessageKey msgKey : msgKeys) {
				String messageId = msgKey.getMessageId();
				Map<String,String> attributeMapping = attributeMappings.get(msgKey);

				if(attributeMapping != null && !attributeMapping.isEmpty()) {
					Map<String,String> attributesToReport = findConfiguredAttributes(attributeMapping);

					// STEP 1: Check if we have complete business fields in current attributeMapping
					Map<String,String> completeBusinessFields = extractCompleteBusinessFields(attributesToReport);

					if (!completeBusinessFields.isEmpty()) {
						// STEP 2: Cache for future API calls
						businessFieldsCache.put(messageId, new CachedBusinessFields(completeBusinessFields));
						AdapterMonitorLogger.logMessage(Level.FINE,
							"Cached " + completeBusinessFields.size() + " business fields for " + messageId);

						// STEP 3: Check if we have buffered entries waiting for this data
						List<BufferedMessageData> bufferedEntries = getBufferedEntriesForMessage(messageId);
						if (!bufferedEntries.isEmpty()) {
							AdapterMonitorLogger.logMessage(Level.FINE,
								"Found " + bufferedEntries.size() + " buffered entries for " + messageId + ", enriching and logging now");

							// Enrich and log all buffered entries
							for (BufferedMessageData buffered : bufferedEntries) {
								enrichWithCachedFields(buffered.attributes, completeBusinessFields);
								String jsonString = MessageLoggingProcessor.getLogJson(buffered.messageData, buffered.attributes);
								AdapterMessageLogger.log(jsonString);
								AdapterMonitorLogger.logMessage(Level.FINE,"Logged buffered entry: " + jsonString);
							}

							// Remove from buffer
							removeBufferedEntriesForMessage(messageId);
						}
					}

					// STEP 4: Try to get cached business fields (might be from previous call)
					CachedBusinessFields cached = businessFieldsCache.get(messageId);

					List<MessageData> list = dataMapping.get(msgKey);
					if (list != null) {
						for (MessageData msgData : list) {
							// Create a copy of attributes for each entry
							Map<String,String> entryAttributes = new HashMap<>(attributesToReport);

							// STEP 5: Enrich with cached fields if available
							if (cached != null && !cached.isExpired()) {
								enrichWithCachedFields(entryAttributes, cached.fields);

								// Log immediately with complete data
								String jsonString = MessageLoggingProcessor.getLogJson(msgData, entryAttributes);
								AdapterMessageLogger.log(jsonString);
								AdapterMonitorLogger.logMessage(Level.FINE,"Logged with cached fields: " + jsonString);
							} else {
								// STEP 6: No cached data yet - check if business fields are incomplete
								boolean hasIncompleteFields = hasIncompleteBusinessFields(entryAttributes);

								if (hasIncompleteFields) {
									// Buffer this entry - don't log yet
									bufferMessageData(messageId, msgData, entryAttributes);
									AdapterMonitorLogger.logMessage(Level.FINE,
										"Buffered entry for " + messageId + " (waiting for complete business fields)");
								} else {
									// No user attributes configured OR all fields complete - log immediately
									String jsonString = MessageLoggingProcessor.getLogJson(msgData, entryAttributes);
									AdapterMessageLogger.log(jsonString);
									AdapterMonitorLogger.logMessage(Level.FINE,"Logged immediately: " + jsonString);
								}
							}
						}

						// STEP 7: If we processed final/delivered status, cleanup
						boolean hasFinalStatus = false;
						for (MessageData md : list) {
							MessageStatus status = md.getStatus();
							if (status != null &&
								(MessageStatus.DELIVERED.equals(status) ||
								 MessageStatus.NON_DELIVERED.equals(status))) {
								hasFinalStatus = true;
								break;
							}
						}

						if (hasFinalStatus) {
							businessFieldsCache.remove(messageId);
							removeBufferedEntriesForMessage(messageId);
							AdapterMonitorLogger.logMessage(Level.FINE,
								"Cleaned up cache and buffer for " + messageId + " (final status reached)");
						}
					}
				}
			}
		} catch (InvalidParamException e) {
			NewRelic.getAgent().getLogger().log(Level.FINER, e, "Failed to get MonitorData due to InvalidParamException");
		} catch (MessageAccessException e) {
			NewRelic.getAgent().getLogger().log(Level.FINER, e, "Failed to get MonitorData due to MessageAccessException");
		}
		
		long endTime = System.nanoTime();
		NewRelic.recordMetric("SAP/AttributeProcessor/SetAttributesTimer(ms)", (endTime-startTime)/1000000.0f);
		NewRelic.recordMetric("SAP/AttributeProcessor/CacheSize", businessFieldsCache.size());
		NewRelic.recordMetric("SAP/AttributeProcessor/BufferSize", messageDataBuffer.size());
	}
	
	public static void recordMessageAndContext(TransportableMessage message, Map<String, Object> context) {
		TransportableMessage cloned = null;
		try {
			cloned = (TransportableMessage) message.clone();
		} catch (CloneNotSupportedException e) {
		}
		if(cloned == null) {
			cloned = message;
		}
		
		Map<String, Object> contextCopy = new HashMap<String, Object>(context);
		
		AttributeChecker.addDataToQueue(new MessageAndContextHolder(cloned, contextCopy));
	}
	
	@SuppressWarnings("rawtypes")
	protected static Map<String,Map<String,String>> recordObject(Object requestMessage) {
		
		Map<String,Map<String,String>> result = new HashMap<String, Map<String,String>>();
		

		if(requestMessage instanceof Message) {
			Message message = (Message)requestMessage;
			Map<String,String> attributes3 = new HashMap<String, String>();
			for(MessagePropertyKey propertyKey : message.getMessagePropertyKeys()) {
				String value = message.getMessageProperty(propertyKey);
				String name = propertyKey.getPropertyName();
				attributes3.put(name, value);
				AttributeMonitorLogger.addAttribute(propertyKey);
			}
			result.put(MESSAGE_PROPERTIES, attributes3);

			XMLPayload document = message.getDocument();
			try {
				// Validate XML content before parsing
				String xmlText = document != null ? document.getText() : null;
				if(xmlText != null && !xmlText.trim().isEmpty()) {
					// Check for valid XML start (not BOM or other prolog issues)
					String trimmedXml = xmlText.trim();
					if(trimmedXml.startsWith("<") || trimmedXml.startsWith("<?xml")) {
						Document doc = loadXMLFromString(xmlText);
						Map<String,String> attributes = getAttributesFromXML(doc);
						if(attributes != null) {
							result.put(MESSAGE_DOCUMENT, attributes);
						}
					} else {
						NewRelic.getAgent().getLogger().log(Level.FINE, "XML content does not start with valid XML declaration, skipping parse");
					}
				} else {
					NewRelic.getAgent().getLogger().log(Level.FINE, "XML payload is null or empty, skipping parse");
				}

				if(document != null) {
					Map<String,String> attributes2 = new HashMap<String, String>();

					for(String attributeName : document.getAttributeNames()) {
						String value = document.getAttribute(attributeName);
						if(value != null) {
							attributes2.put(attributeName, value);
						}
					}
					result.put(PAYLOAD_ATTRIBUTES, attributes2);
				}



			} catch (Exception e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to parse XML");
			}
			
			if(message instanceof XIMessage) {
				XIMessage xiMessage = (XIMessage)message;
				MessageContext msgContext = xiMessage.getMessageContext();
				Enumeration inKeys = msgContext.getAttributeKeys(0);
				if(inKeys.hasMoreElements()) {
					Map<String, String> inAttributes = new HashMap<String, String>();
					while(inKeys.hasMoreElements()) {
						String name = inKeys.nextElement().toString();
						Object value = msgContext.getAttribute(name, 0);
						if(value != null) {
							inAttributes.put(name, value.toString());
						}
					}
					if(!inAttributes.isEmpty()) {
						result.put(MESSAGECONTEXT_IN_ATTRIBUTES, inAttributes);
					}
				}
				
				Enumeration outKeys = msgContext.getAttributeKeys(1);
				if(outKeys.hasMoreElements()) {
					Map<String, String> outAttributes = new HashMap<String, String>();
					while(outKeys.hasMoreElements()) {
						String name = outKeys.nextElement().toString();
						Object value = msgContext.getAttribute(name, 0);
						if(value != null) {
							outAttributes.put(name, value.toString());
						}
					}
					if(!outAttributes.isEmpty()) {
						result.put(MESSAGECONTEXT_OUT_ATTRIBUTES, outAttributes);
					}
				}
			}

		}
		return result;
	}

	public static Map<String, String> getAttributesFromXML(Document document) {
		Map<String,String> result = new HashMap<String, String>();

		Element root = document.getDocumentElement();
		processNode(root, result);

		return result;
	}

	private static void processNode(Node node, Map<String, String> attributes) {
		String name = node.getNodeName();
		node.getNodeType();
		if(node.hasChildNodes()) {
			NodeList children = node.getChildNodes();
			int length = children.getLength();
			for(int i=0;i<length;i++) {
				Node child = children.item(i);
				short type = child.getNodeType();
				if(type == Node.TEXT_NODE) {
					String value = child.getTextContent();
					String modifiedValue = value.replace("\\n", "").replace("\\t", "").trim();
					if(!modifiedValue.isEmpty()) {
						attributes.put(name.trim(), modifiedValue);
					}
				} else {
					processNode(child, attributes);
				}
			}
		}
	}


	private static Document loadXMLFromString(String xml) throws Exception {
		InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(stream);
	}

	@SuppressWarnings("rawtypes")
	public static void record(ModuleContext moduleContext, ModuleData moduleData) {
		AdapterMonitorLogger.logMessage(Level.FINER, "Recording module data and context");
		if(!AttributeChecker.initialized) {
			AttributeChecker.startChecker();
		}
		ModuleData md = new ModuleData();
		Object principal = moduleData.getPrincipalData();
		if(principal instanceof AbstractMessage) {
			try {
				principal = ((AbstractMessage)principal).clone();
			} catch (CloneNotSupportedException e) {
			}
		}
		md.setPrincipalData(principal);
		Enumeration names = moduleData.getSupplementalDataNames();
		while(names.hasMoreElements()) {
			String name = names.nextElement().toString();
			Object value = moduleData.getSupplementalData(name);
			md.setSupplementalData(name, value);
		}
		
		Hashtable<String,String> ht = new Hashtable<String, String>();
		
		names = moduleContext.getContextDataKeys();
		while(names.hasMoreElements()) {
			String name = names.nextElement().toString();
			String value = moduleContext.getContextData(name);
			if(value != null) {
				ht.put(name, value);
			}
		}
		
		ModuleContext ctx = new ModuleContextImpl(moduleContext.getChannelID(), ht);
		
		AttributeChecker.addDataToQueue(new ModuleDataHolder(md, ctx));
		AdapterMonitorLogger.logMessage(Level.FINER, "Added dataholder to queue");

	}

	private static Map<String,String> findConfiguredAttributes(Map<String,String> currentAttributes) {
		AttributeConfig config = AttributeConfig.getInstance();
		Map<String,String> attributesToReport = new HashMap<String, String>();
		if (config.collectingUserAttributes()) {
			Set<String> toCollect = config.attributesToCollect();
			Set<String> currentKeys = currentAttributes != null ? currentAttributes.keySet() : new HashSet<String>();
			Set<String> modifedKeys = new HashSet<String>();
			HashMap<String, String> keyMapping = new HashMap<String, String>();

			for(String key : currentKeys) {
				String mKey = key.toLowerCase().trim();
				String keyToUse = key;

				String tmp = "modulecontext-";
				if(mKey.startsWith(tmp)) {
					keyToUse = key.substring(tmp.length());
				}
				tmp = "SupplementalData-".toLowerCase();
				if(mKey.startsWith(tmp)) {
					keyToUse = key.substring(tmp.length());
				}
				modifedKeys.add(keyToUse.toLowerCase());
				keyMapping.put(keyToUse.toLowerCase(), key);

			}
					
			for(String attribute : toCollect) {
				String mKey = attribute.toLowerCase().trim();
				String keyToUse = attribute.trim();
				String tmp = "modulecontext-";
				if(mKey.startsWith(tmp)) {
					keyToUse = attribute.substring(tmp.length());
				}
				tmp = "SupplementalData-".toLowerCase();
				if(mKey.startsWith(tmp)) {
					keyToUse = attribute.substring(tmp.length());
				}
				keyToUse = keyToUse.toLowerCase();
				if(modifedKeys.contains(keyToUse)) {
					String mappedKey = keyMapping.get(keyToUse);
					if(mappedKey == null) mappedKey = keyToUse;
					String value = currentAttributes.get(mappedKey);

					attributesToReport.put(attribute, value);
				}

			}

		}

		return attributesToReport;
	}

	// ============================================================
	// HELPER METHODS FOR BUFFERING AND CACHING
	// ============================================================

	/**
	 * Extracts complete business fields from attributes
	 * Only returns fields that have actual values (not "Not_Reported")
	 */
	static Map<String,String> extractCompleteBusinessFields(Map<String,String> attributes) {
		Map<String,String> completeFields = new HashMap<>();
		AttributeConfig config = AttributeConfig.getInstance();
		Set<String> configuredAttributes = config.attributesToCollect();

		if (configuredAttributes == null || configuredAttributes.isEmpty()) {
			return completeFields;
		}

		for (String attributeName : configuredAttributes) {
			String value = attributes.get(attributeName);
			if (value != null && !value.isEmpty() && !"Not_Reported".equals(value)) {
				completeFields.put(attributeName, value);
			}
		}

		return completeFields;
	}

	/**
	 * Checks if business fields are incomplete (missing or "Not_Reported")
	 */
	static boolean hasIncompleteBusinessFields(Map<String,String> attributes) {
		AttributeConfig config = AttributeConfig.getInstance();
		Set<String> configuredAttributes = config.attributesToCollect();

		if (configuredAttributes == null || configuredAttributes.isEmpty()) {
			return false; // No user attributes configured - nothing to wait for
		}

		for (String attributeName : configuredAttributes) {
			String value = attributes.get(attributeName);
			if (value == null || value.isEmpty() || "Not_Reported".equals(value)) {
				return true; // At least one field incomplete
			}
		}

		return false; // All fields complete
	}

	/**
	 * Enriches attributes with cached business fields
	 * Only replaces "Not_Reported" or missing values
	 */
	static void enrichWithCachedFields(
		Map<String,String> attributes,
		Map<String,String> cachedFields
	) {
		for (Map.Entry<String,String> entry : cachedFields.entrySet()) {
			String fieldName = entry.getKey();
			String cachedValue = entry.getValue();
			String currentValue = attributes.get(fieldName);

			if (currentValue == null ||
				currentValue.isEmpty() ||
				"Not_Reported".equals(currentValue)) {
				attributes.put(fieldName, cachedValue);
			}
		}
	}

	/**
	 * Buffer a MessageData entry that has incomplete business fields
	 */
	static void bufferMessageData(String messageId, MessageData msgData, Map<String,String> attributes) {
		BufferedMessageData buffered = new BufferedMessageData(msgData, new HashMap<>(attributes));
		messageDataBuffer.computeIfAbsent(messageId, k -> new CopyOnWriteArrayList<>()).add(buffered);
	}

	/**
	 * Get all buffered entries for a specific MessageId (O(1) operation)
	 */
	static List<BufferedMessageData> getBufferedEntriesForMessage(String messageId) {
		return messageDataBuffer.getOrDefault(messageId, Collections.emptyList());
	}

	/**
	 * Remove all buffered entries for a specific MessageId
	 */
	static void removeBufferedEntriesForMessage(String messageId) {
		messageDataBuffer.remove(messageId);
	}

	/**
	 * Process buffered entries that have timed out (no complete data arrived)
	 * Logs them with incomplete data rather than buffering forever
	 */
	static void processTimedOutBufferedEntries() {
		int processed = 0;
		Iterator<Map.Entry<String, List<BufferedMessageData>>> iterator =
			messageDataBuffer.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, List<BufferedMessageData>> entry = iterator.next();
			List<BufferedMessageData> entries = entry.getValue();

			// Process and remove timed-out entries
			Iterator<BufferedMessageData> entryIterator = entries.iterator();
			while (entryIterator.hasNext()) {
				BufferedMessageData buffered = entryIterator.next();
				if (buffered.isTimedOut()) {
					AdapterMonitorLogger.logMessage(Level.WARNING,
						"Buffered entry timed out, logging with incomplete data: " + entry.getKey());

					String jsonString = MessageLoggingProcessor.getLogJson(
						buffered.messageData,
						buffered.attributes
					);
					AdapterMessageLogger.log(jsonString);

					entryIterator.remove();
					processed++;
				}
			}

			// If all entries for this MessageId processed, remove from map
			if (entries.isEmpty()) {
				iterator.remove();
			}
		}

		if (processed > 0) {
			AdapterMonitorLogger.logMessage(Level.FINE,
				"Processed " + processed + " timed-out buffered entries");
			NewRelic.recordMetric("SAP/AttributeProcessor/BufferedEntriesTimedOut", processed);
		}
	}

	/**
	 * Cleanup expired cache entries (runs every 5 minutes)
	 */
	static void cleanupExpiredCache() {
		int removed = 0;
		Iterator<Map.Entry<String, CachedBusinessFields>> iterator =
			businessFieldsCache.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, CachedBusinessFields> entry = iterator.next();
			if (entry.getValue().isExpired()) {
				iterator.remove();
				removed++;
			}
		}

		if (removed > 0) {
			AdapterMonitorLogger.logMessage(Level.FINE,
				"Cleaned up " + removed + " expired cache entries");
			NewRelic.recordMetric("SAP/AttributeProcessor/CacheEntriesExpired", removed);
		}
	}

	// ============================================================
	// TEST HELPER METHODS (Package-private for unit tests)
	// ============================================================

	/**
	 * Clear cache and buffer (for unit tests)
	 */
	static void clearCacheAndBuffer() {
		businessFieldsCache.clear();
		messageDataBuffer.clear();
	}

	/**
	 * Get cached business fields (for unit tests)
	 */
	static CachedBusinessFields getCachedBusinessFields(String messageId) {
		return businessFieldsCache.get(messageId);
	}

	/**
	 * Cache business fields (for unit tests)
	 */
	static void cacheBusinessFields(String messageId, Map<String, String> fields) {
		businessFieldsCache.put(messageId, new CachedBusinessFields(fields));
	}

	/**
	 * Remove cached business fields (for unit tests)
	 */
	static void removeCachedBusinessFields(String messageId) {
		businessFieldsCache.remove(messageId);
	}

	/**
	 * Find complete value with prefix search (for unit tests)
	 */
	static String findCompleteValue(String attributeName, Map<String,String> attributeMapping) {
		// Try direct lookup first
		String value = attributeMapping.get(attributeName);
		if (value != null && !value.isEmpty() && !"Not_Reported".equals(value)) {
			return value;
		}

		// Try common prefixes
		String[] prefixes = {"modulecontext-", "supplementaldata-", "SupplementalData-", "ModuleContext-"};

		for (String prefix : prefixes) {
			value = attributeMapping.get(prefix + attributeName);
			if (value != null && !value.isEmpty() && !"Not_Reported".equals(value)) {
				return value;
			}

			// Also try lowercase attribute name
			value = attributeMapping.get(prefix + attributeName.toLowerCase());
			if (value != null && !value.isEmpty() && !"Not_Reported".equals(value)) {
				return value;
			}
		}

		return null;
	}

	// ============================================================
	// INNER CLASSES
	// ============================================================

	/**
	 * Cached business fields for a MessageId
	 */
	static class CachedBusinessFields {
		final Map<String, String> fields;
		final long timestamp;
		final long expirationMillis;

		CachedBusinessFields(Map<String, String> fields) {
			this(fields, 600000L); // Default 10 minutes
		}

		CachedBusinessFields(Map<String, String> fields, long expirationMillis) {
			this.fields = fields;
			this.timestamp = System.currentTimeMillis();
			this.expirationMillis = expirationMillis;
		}

		boolean isExpired() {
			return (System.currentTimeMillis() - timestamp) > expirationMillis;
		}
	}

	/**
	 * Buffered message data waiting for complete business fields
	 */
	static class BufferedMessageData {
		final MessageData messageData;
		final Map<String, String> attributes;
		final long bufferedTime;
		final long timeoutMillis;

		BufferedMessageData(MessageData messageData, Map<String, String> attributes) {
			this(messageData, attributes, 30000L); // Default 30 seconds
		}

		BufferedMessageData(MessageData messageData, Map<String, String> attributes, long timeoutMillis) {
			this.messageData = messageData;
			this.attributes = attributes;
			this.bufferedTime = System.currentTimeMillis();
			this.timeoutMillis = timeoutMillis;
		}

		boolean isTimedOut() {
			return (System.currentTimeMillis() - bufferedTime) > timeoutMillis;
		}
	}
}
