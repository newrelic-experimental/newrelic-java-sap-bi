package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
		long startTime = System.nanoTime();

		List<MessageKey> msgKeys = new ArrayList<MessageKey>();
		msgKeys.addAll(attributeMappings.keySet());
		String[] msgKeysArray = new String[msgKeys.size()];
		int index = 0;
		
		for(MessageKey key : msgKeys) {
			msgKeysArray[index] = key.getMessageId();
			index++;
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


			for(MessageKey msgKey : msgKeys) {
				Map<String,String> attributeMapping = attributeMappings.get(msgKey);
				if(attributeMapping != null && !attributeMapping.isEmpty()) {
					Map<String,String> attributesToReport = findConfiguredAttributes(attributeMapping);
					if(attributesToReport == null || attributesToReport.isEmpty()) {
						continue;
					}
					
					List<MessageData> list = dataMapping.get(msgKey);
					if (list != null) {
						for (MessageData msgData : list) {
							String jsonString = MessageLoggingProcessor.getLogJson(msgData, attributesToReport);
							AdapterMessageLogger.log(jsonString);
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
				Document doc = loadXMLFromString(document.getText());
				Map<String,String> attributes = getAttributesFromXML(doc);
				if(attributes != null) {
					result.put(MESSAGE_DOCUMENT, attributes);
				}
				Map<String,String> attributes2 = new HashMap<String, String>();
				
				for(String attributeName : document.getAttributeNames()) {
					String value = document.getAttribute(attributeName);
					if(value != null) {
						attributes2.put(attributeName, value);
					}
				}
				result.put(PAYLOAD_ATTRIBUTES, attributes2);
				
				
				
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
}
