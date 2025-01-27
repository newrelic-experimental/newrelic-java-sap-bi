package com.newrelic.instrumentation.labs.sap.adapters.ejb;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.instrumentation.labs.sap.adapters.ejb.attrributeservice.AttributeConfiguration;
import com.newrelic.instrumentation.labs.sap.adapters.ejb.attrributeservice.SAPAttributeService;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.engine.interfaces.messaging.api.ErrorInfo;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessagePropertyKey;
import com.sap.engine.interfaces.messaging.api.Payload;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;
import com.sap.engine.interfaces.messaging.spi.transport.Endpoint;

public class DataUtils {

	private static List<String> contextDataAttributes = new ArrayList<String>();  //new HashSet<String>();
	private static List<String> supplementalAttributes = new ArrayList<String>();
	private static ExecutorService executor = Executors.newFixedThreadPool(3);
	private static BlockingQueue<ModuleContext> contexts = new LinkedBlockingQueue<ModuleContext>(1000);
	private static BlockingQueue<ModuleData> datas = new LinkedBlockingQueue<ModuleData>(1000);
	private static Set<String> ctxAttributesToCapture = new HashSet<String>();
	private static Set<String> principalAttributesToCapture = new HashSet<String>();
	private static Set<String> supplementalAttributesToCapture = new HashSet<String>();
	private static Set<String> defaultMessageAttributes = new HashSet<String>();
	private static Set<MessagePropertyKey> messagePropertyKeys = ConcurrentHashMap.newKeySet();  //new HashSet<MessagePropertyKey>();
	private static Set<MessagePropertyKey> messagePropertyKeysToCapture = new HashSet<MessagePropertyKey>();
	private static List<String> attachmentNames = new ArrayList<String>();
	private static List<String> attachmentAttributes = new ArrayList<String>();
	private static final Set<String> principalAttributesList;

	static {
		executor.submit(new ContextProcessor());
		executor.submit(new DataProcessor());
		SAPAttributeService.INSTANCE.start();

		ctxAttributesToCapture = AttributeConfiguration.getContextAttributes();
		principalAttributesToCapture = AttributeConfiguration.getPrincipalAttributes();
		supplementalAttributesToCapture = AttributeConfiguration.getSupplementalAttributes();
		messagePropertyKeysToCapture = AttributeConfiguration.getMessagePropertiesToCapture();

		defaultMessageAttributes.add("Message-Action");
		defaultMessageAttributes.add("Message-CorrelationId");
		defaultMessageAttributes.add("Message-FromParty");
		defaultMessageAttributes.add("Message-ToParty");
		defaultMessageAttributes.add("Message-FromService");
		defaultMessageAttributes.add("Message-ToService");
		defaultMessageAttributes.add("Message-MessageId");
		defaultMessageAttributes.add("MessageKey-Direction");
		defaultMessageAttributes.add("MessageKey-ID");

		principalAttributesList = new HashSet<String>();
		principalAttributesList.add("Message-Action");
		principalAttributesList.add("Message-CorrelationId");
		principalAttributesList.add("Message-FromParty");
		principalAttributesList.add("Message-FromService");
		principalAttributesList.add("Message-Id");
		principalAttributesList.add("Message-Protocol");
		principalAttributesList.add("Message-SequenceId");
		principalAttributesList.add("Message-ToParty");
		principalAttributesList.add("Message-ToService");
		principalAttributesList.add("Message-AttachmentCount");
		principalAttributesList.add("Message-AttachmentNames");
		principalAttributesList.add("Message-ErrorInfo");
		principalAttributesList.add("Message-Description");
		principalAttributesList.add("Message-RefToMessageId");
		principalAttributesList.add("Message-TimeReceived");
		principalAttributesList.add("Message-TimeSent");

		principalAttributesList.add("TransportableMessage-MessagePriority");
		principalAttributesList.add("TransportableMessage-Retries");
		principalAttributesList.add("TransportableMessage-SequenceNumber");
		principalAttributesList.add("TransportableMessage-PersistUntil");
		principalAttributesList.add("TransportableMessage-ValidUntil");
		principalAttributesList.add("TransportableMessage-VersionNumber");
		principalAttributesList.add("TransportableMessage-ParentId");

		principalAttributesList.add("MessageKey-Direction");
		principalAttributesList.add("MessageKey-ID");
		
		principalAttributesList.add("Endpoint-Address");
		principalAttributesList.add("Endpoint-Transport");

		principalAttributesList.add("Payload-Name");
		principalAttributesList.add("Payload-Description");
		principalAttributesList.add("Payload-ContentType");
		principalAttributesList.add("Payload-AttributeNames");

		for(String attribute : principalAttributesList) {
			EJBAdapterLogger.logNewPrincipalMessageAttribute(attribute);
		}
	}

	public static void addContext(ModuleContext context) {
		contexts.add(context);
	}

	public static void addData(ModuleData data) {
		datas.add(data);
	}

	public static void reset() {
		ctxAttributesToCapture = AttributeConfiguration.getContextAttributes();
		principalAttributesToCapture = AttributeConfiguration.getPrincipalAttributes();
		supplementalAttributesToCapture = AttributeConfiguration.getSupplementalAttributes();
	}

	private DataUtils() {

	}

	@SuppressWarnings("unchecked")
	public static void addAttributes(ModuleContext context, Map<String, Object> attributes) {
		if(context == null) return;
		if(AttributeConfiguration.isCollectContextChannelId()) {
			attributes.put("ChannelId", context.getChannelID());
		}
		Enumeration<String> dataKeys = context.getContextDataKeys();
		while(dataKeys.hasMoreElements()) {
			String key = dataKeys.nextElement();
			if(key != null) {
				if (ctxAttributesToCapture.contains(key)) {
					String value = context.getContextData(key);
					if (value != null) {
						attributes.put(key, value);
					} 
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static void addAttributes(ModuleData moduleData, Map<String, Object> attributes) {
		if(moduleData == null) return;
		if (AttributeConfiguration.isSupplementalEnabled()) {
			Enumeration supplementalNames = moduleData.getSupplementalDataNames();
			while (supplementalNames.hasMoreElements()) {
				String name = supplementalNames.nextElement().toString();
				if (supplementalAttributesToCapture.contains(name)) {
					Object value = moduleData.getSupplementalData(name);
					boolean valid = validate(name, value, attributes);
					if (!valid) {
						NewRelic.getAgent().getLogger().log(Level.FINEST,
								"Invalid, Failed to add attribute {0} with value {1}", name, value);
					}
				}
			} 
		}
		if(AttributeConfiguration.isPrincipalEnabled()) {
			Object principalData = moduleData.getPrincipalData();
			if (principalData != null) {
				if (principalData instanceof Message) {
					Message msg = (Message) principalData;
					if (AttributeConfiguration.isPrincipalDefaultsEnabled()) {
						AdaptersUtils.addMessage(attributes, msg);
						AdaptersUtils.addMessageKey(attributes, msg.getMessageKey());
					}
					if(!messagePropertyKeysToCapture.isEmpty()) {
						for(MessagePropertyKey key : messagePropertyKeysToCapture) {
							String value = msg.getMessageProperty(key);
							if(value != null && !value.isEmpty()) {
								validate("MessageProperty-"+key.getPropertyNamespace()+":"+key.getPropertyName(), value, attributes);
							}
						}
					}
					int attachmentCount = msg.countAttachments();
					if(principalAttributesToCapture.contains("Message-AttachmentCount")) {
						validate("Message-AttachmentCount", attachmentCount, attributes);
					}
					if(attachmentCount > 0) {
						Iterator iterator = msg.getAttachmentIterator();
						Set<String> names = new HashSet<String>();
						while(iterator.hasNext()) {

							Object next = (Payload)iterator.next();
							if(next != null && next instanceof Payload) {
								Payload payload = (Payload)next;
								if(payload != null) {
									names.add(payload.getName());
								}
							}
						}
						if(!names.isEmpty()) {
							if(principalAttributesToCapture.contains("Message-AttachmentNames")) {
								validate("Message-AttachmentNames", names.toString(), attributes);
							}
							for(String name : names) {
								Payload attachment = msg.getAttachment(name);
								List<String> attributesToCapture = AttributeConfiguration.getAttachmentAttributes(name);
								if (attributesToCapture != null) {
									for (String attrName : attributesToCapture) {
										String value = attachment.getAttribute(attrName);
										validate("Attachment-" + name + "-attribute-" + attrName, value, attributes);
									} 
								}
							}
						}

					}
					for(String attribute : principalAttributesToCapture) {
						if(attribute.equalsIgnoreCase("Message-Action")) {
							validate(attribute, msg.getAction().toString(), attributes);
						} else if(attribute.equalsIgnoreCase("Message-CorrelationId")) {
							validate(attribute, msg.getCorrelationId(), attributes);
						} else if(attribute.equalsIgnoreCase("Message-FromParty")) {
							validate(attribute, msg.getFromParty().toString(), attributes);
						} else if(attribute.equalsIgnoreCase("Message-FromService")) {
							validate(attribute, msg.getFromService().toString(), attributes);
						} else if(attribute.equalsIgnoreCase("Message-Id")) {
							validate(attribute, msg.getMessageId(), attributes);
						} else if(attribute.equalsIgnoreCase("Message-Protocol")) {
							validate(attribute, msg.getProtocol(), attributes);
						} else if(attribute.equalsIgnoreCase("Message-SequenceId")) {
							validate(attribute, msg.getSequenceId(), attributes);
						} else if(attribute.equalsIgnoreCase("Message-ToParty")) {
							validate(attribute, msg.getToParty().toString(), attributes);
						} else if(attribute.equalsIgnoreCase("Message-ToService")) {
							validate(attribute, msg.getToService().toString(), attributes);
						} else if(attribute.equalsIgnoreCase("Message-ErrorInfo")) {
							ErrorInfo errorInfo = msg.getErrorInfo();
							if(errorInfo != null) {
								String[] supported = errorInfo.getSupportedAttributeNames();
								for(String attr : supported) {
									validate("ErrorInfo-"+attr, errorInfo.getAttribute(attr), attributes);
								}
							}
						} else if(attribute.equalsIgnoreCase("Message-Description")) {
							validate(attribute, msg.getDescription(), attributes);
						} else if(attribute.equalsIgnoreCase("Message-RefToMessageId")) {
							validate(attribute, msg.getRefToMessageId(), attributes);
						} else if(attribute.equalsIgnoreCase("Message-TimeReceived")) {
							validate(attribute, msg.getTimeReceived(), attributes);
						} else if(attribute.equalsIgnoreCase("Message-TimeSent")) {
							validate(attribute, msg.getTimeReceived(), attributes);
						} else if(attribute.equalsIgnoreCase("Message-MessageClass")) {
							validate(attribute, msg.getMessageClass().toString(), attributes);
						}
						if(msg instanceof TransportableMessage) {
							TransportableMessage tMsg = (TransportableMessage)msg;

							if(attribute.equalsIgnoreCase("TransportableMessage-MessagePriority")) {
								validate(attribute, tMsg.getMessagePriority().toString(), attributes);
							} else if(attribute.equalsIgnoreCase("TransportableMessage-Retries")) {
								validate(attribute, tMsg.getRetries(), attributes);
							} else if(attribute.equalsIgnoreCase("TransportableMessage-SequenceNumber")) {
								validate(attribute, tMsg.getSequenceNumber(), attributes);
							} else if(attribute.equalsIgnoreCase("TransportableMessage-PersistUntil")) {
								validate(attribute, tMsg.getPersistUntil(), attributes);
							} else if(attribute.equalsIgnoreCase("TransportableMessage-ValidUntil")) {
								validate(attribute, tMsg.getValidUntil(), attributes);
							} else if(attribute.equalsIgnoreCase("TransportableMessage-VersionNumber")) {
								validate(attribute, tMsg.getVersionNumber(), attributes);
							} else if(attribute.equalsIgnoreCase("TransportableMessage-ParentId")) {
								validate(attribute, tMsg.getParentId(), attributes);
							} else if(attribute.equalsIgnoreCase("Endpoint-Address")) {
								Endpoint endPt = tMsg.getEndpoint();
								if (endPt != null) {
									validate(attribute, endPt.getAddress(), attributes);
								}
							} else if(attribute.equalsIgnoreCase("Endpoint-Transport")) {
								validate(attribute, tMsg.getEndpoint().getTransport(), attributes);
							}


						}
					}
				}

			}

		}
	}


	private static boolean validate(String key, Object value, Map<String, Object> attributes) {


		if (value instanceof String) {
			attributes.put(key, value);
			return true;
		}

		if (value instanceof Number || value instanceof Boolean || value instanceof AtomicBoolean) {
			attributes.put(key, value);
			return true;
		}

		if(value instanceof Map) {
			Map<?,?> map = (Map<?, ?>)value;
			for(Object mapKey : map.keySet()) {
				Object mapValue = map.get(mapKey);
				String keyValue = key + "-" + mapKey.toString();
				attributes.put(keyValue, mapValue);
			}
			return true;
		}

		NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to add attribute {0} to span attributes because the value type (1} is not a valid type", key, value.getClass());
		return false;
	}

	private static class ContextProcessor implements Runnable {

		@SuppressWarnings("unchecked")
		@Override
		public void run() {

			while(true) {
				try {
					ModuleContext context = contexts.poll(10, TimeUnit.SECONDS);
					if(context != null) {
						Set<String> added = new HashSet<String>();
						Enumeration<String> keys = context.getContextDataKeys();
						while(keys.hasMoreElements()) {
							String key = keys.nextElement();
							if(key != null && !key.isEmpty()) {
								if (!contextDataAttributes.contains(key)) {
									added.add(key);
								}
							}
						}

						for(String key : added) {
							if (!contextDataAttributes.contains(key)) {
								NewRelic.getAgent().getLogger().log(Level.FINE, "(EJB) Add context key {0}", key);
								contextDataAttributes.add(key);
								EJBAdapterLogger.logNewAttribute(key);
							}
						}

					}
				} catch (InterruptedException e) {
					NewRelic.getAgent().getLogger().log(Level.FINEST, e, "Module Context Checker was interuptted");
				}

			}
		}

	}

	private static class DataProcessor implements Runnable {

		@SuppressWarnings("unchecked")
		@Override
		public void run() {

			while(true) {
				try {
					ModuleData data = datas.poll(10, TimeUnit.SECONDS);
					if(data != null) {
						Enumeration<String> keys = data.getSupplementalDataNames();
						Set<String> added = new HashSet<String>();
						while(keys.hasMoreElements()) {
							String key = keys.nextElement();
							if(key != null && !key.isEmpty()) {
								if (!supplementalAttributes.contains(key)) {
									added.add(key);
								}
							}
						}
						for(String key : added) {
							if(!supplementalAttributes.contains(key)) {
								NewRelic.getAgent().getLogger().log(Level.FINE, "(EJB) Add supplemental key {0}",key);
								supplementalAttributes.add(key);
								EJBAdapterLogger.logNewSupplementalAttribute(key);
							}
						}
						Object principalDataObject = data.getPrincipalData();
						if(principalDataObject != null) {
							if(principalDataObject instanceof Message) {
								Message msg = (Message)principalDataObject;
								Set<MessagePropertyKey> propertyKeys = msg.getMessagePropertyKeys();
								Set<MessagePropertyKey> addedKeys = new HashSet<MessagePropertyKey>();
								
								for(MessagePropertyKey key : propertyKeys) {
									if (!messagePropertyKeys.contains(key)) {
										addedKeys.add(key);
									}
								}
								for(MessagePropertyKey key : addedKeys) {
									if(messagePropertyKeys.contains(key)) {
										NewRelic.getAgent().getLogger().log(Level.FINE, "(EJB) Add MessagePropertyKey {0}", key);
										messagePropertyKeys.add(key);
										EJBAdapterLogger.logNewPrincipalAttribute("MessagePropertyKey",key.toString());
									}
								}
								@SuppressWarnings("rawtypes")
								Iterator attachmentIterator = msg.getAttachmentIterator();
								while(attachmentIterator.hasNext()) {
									Object attachmentObj = attachmentIterator.next();
									if(attachmentObj != null) {
										if(attachmentObj instanceof Payload) {
											Payload payload = (Payload)attachmentObj;
											String payloadName = payload.getName();
											if(payloadName != null && !payloadName.isEmpty()) {
												if (!attachmentNames.contains(payloadName)) {
													attachmentNames.add(payloadName);
													EJBAdapterLogger.logNewPrincipalAttribute("Attachment/Payload Name", payloadName);
												}
											}
											Set<String> attrNames = payload.getAttributeNames();
											for(String attr : attrNames) {
												if(payloadName == null || payloadName.isEmpty()) {
													payloadName = "UnknownPayload";
												}
												String attrKey = payloadName + "-" + attr;
												if (!attachmentAttributes.contains(attrKey)) {
													attachmentAttributes.add(attrKey);
													EJBAdapterLogger.logNewPrincipalAttribute("Attachment-Attribute", "PayloadName=" + payloadName + ",AttributeName=" + attr);
												}
											}
										}
									}

								}
							}
						}
					}
				} catch (InterruptedException e) {
					NewRelic.getAgent().getLogger().log(Level.FINEST, e, "Module Supplemental Checker was interuptted");
				}

			}
		}

	}


}