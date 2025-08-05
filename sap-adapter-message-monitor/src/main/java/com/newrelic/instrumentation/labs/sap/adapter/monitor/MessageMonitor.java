package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.newrelic.agent.deps.com.google.gson.Gson;
import com.newrelic.api.agent.Logger;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.data.MessageInterface;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.data.MessageParty;
import com.sap.engine.interfaces.messaging.api.APIAccess;
import com.sap.engine.interfaces.messaging.api.APIAccessFactory;
import com.sap.engine.interfaces.messaging.api.Action;
import com.sap.engine.interfaces.messaging.api.DeliverySemantics;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.MessageStatus;
import com.sap.engine.interfaces.messaging.api.Party;
import com.sap.engine.interfaces.messaging.api.Service;
import com.sap.engine.interfaces.messaging.api.exception.MessageFormatException;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;
import com.sap.engine.interfaces.messaging.api.message.MessageAccess;
import com.sap.engine.interfaces.messaging.api.message.MessageAccessException;
import com.sap.engine.interfaces.messaging.api.message.MessageData;
import com.sap.engine.interfaces.messaging.api.message.MessageDataFilter;
import com.sap.engine.interfaces.messaging.api.message.MonitorData;
import com.sap.engine.interfaces.messaging.spi.KeyFields;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;
//import com.sap.engine.interfaces.messaging.spi.transport.Endpoint;
import com.sap.engine.interfaces.messaging.spi.transport.TransportHeaders;

public class MessageMonitor implements Runnable {

	public static boolean initialized = false;
	private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
	private static MessageMonitor INSTANCE = null;
	private static ScheduledFuture<?> scheduledFuture = null;
	private static Date last = new Date(System.currentTimeMillis() - 5L * 60L * 1000L);
	private static long frequency = 3;
	private static long delay = 1;
	private static final String NOT_REPORTED = "Not_Reported";
	private static final String EMPTY_STRING = "Empty_String";
	private static APIAccess apiAccess = null;

	private static APIAccess getAPIAccess() {
		if(apiAccess == null) {
			try {
				apiAccess = APIAccessFactory.getAPIAccess();
			} catch (MessagingException e) {
			}
		}
		return apiAccess;
	}

	private MessageMonitor() {
	}

	public static void changeFrequncy(long newFreq) {
		if(newFreq == frequency) return;

		frequency = newFreq;
		if(scheduledFuture != null) {
			scheduledFuture.cancel(true);
		}
		scheduledFuture = executor.scheduleAtFixedRate(INSTANCE, delay, frequency, TimeUnit.MINUTES);
		NewRelic.getAgent().getLogger().log(Level.FINE, "Scheduled MessageMonitor to run every {0} minutes after an initial delay of {1} minutes", frequency, delay);
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		NewRelic.recordMetric("SAP/AdapterMessageMonitor/Enter",1.0f);
		Logger LOGGER = NewRelic.getAgent().getLogger();
		Date end = new Date();


		try {
			MessageAccess messageAccess = getAPIAccess().getMessageAccess();
			MessageDataFilter filter = messageAccess.createMessageDataFilter();
			filter.setStartDate(last);
			filter.setEndDate(end);

			MonitorData monitorData = messageAccess.getMonitorData(filter);
			LinkedList<MessageData> messageDataList = monitorData.getMessageData();
			int count = messageDataList.size();
			NewRelic.recordMetric("SAP/AdapterMessageMonitor/MessagesToProcess",count);

			for(MessageData data : messageDataList) {
				MessageKey msgKey = data.getMessageKey();
				Map<String, String> messageAttributes = AttributeProcessor.getMessageAttributes(msgKey);
				int size = messageAttributes != null ? messageAttributes.size() : 0;
				NewRelic.recordMetric("SAP/AdapterMessageMonitor/AttributesRetrieved",size);
				String jsonString = getLogJson(data, messageAttributes);
				AdapterMessageLogger.log(jsonString);
			}

		} catch (Exception e) {
			LOGGER.log(Level.FINE, e, "Failed to get message list");
		}

		last = end;

		NewRelic.recordMetric("SAP/AdapterMessageMonitor/Exit", 1.0f);
		long endTime = System.currentTimeMillis();
		NewRelic.recordResponseTimeMetric("SAP/AdapterMessageMonitor/ProcessingTime", endTime-startTime);
	}

	public static void initialize() {
		if(!initialized) {
			if(INSTANCE == null) {
				INSTANCE = new MessageMonitor();
			}
			MessageLoggingConfig config = AdapterMessageLogger.getCurrentConfig();
			if(frequency != config.getFrequency()) {
				frequency = config.getFrequency();
			}
			if(delay != config.getDelay()) {
				delay = config.getDelay();
			}
			if(INSTANCE != null) {
				scheduledFuture = executor.scheduleAtFixedRate(INSTANCE, delay, frequency, TimeUnit.MINUTES);
				Thread shutdown = new Thread(() -> scheduledFuture.cancel(true));
				Runtime.getRuntime().addShutdownHook(shutdown);
			}
			initialized = true;
			NewRelic.getAgent().getLogger().log(Level.FINE, "{0} has been scheduled to run after delay of {1} minutes and will run every {2} minutes", INSTANCE, delay, frequency);
		}
	}


	private static String getLogJson(MessageData msgdata, Map<String,String> currentAttributes) {
		LinkedHashMap<String, Object> attributes = new LinkedHashMap<String, Object>();
		AttributeConfig config = AttributeConfig.getInstance();
		MessageKey messageKey = msgdata.getMessageKey();
		Message message = null;
		
		String softComponent = "";
		String appComponent = "";
		String svcDefinition = "";

		try {
			message = getAPIAccess().getMessageAccess().getMessage(messageKey);
			if(message instanceof TransportableMessage) {
				TransportableMessage tMessage = (TransportableMessage)message;
				TransportHeaders transportHeaders = tMessage.getTransportHeaders();
				svcDefinition = (String)transportHeaders.getHeader("ms:ServiceDefinition");
				appComponent = (String)transportHeaders.getHeader("ms:ApplicationComponent");
				softComponent = (String)transportHeaders.getHeader("ms:SoftwareComponent");

			}
		} catch (MessageAccessException | MessageFormatException e) {
			NewRelic.getAgent().getLogger().log(Level.FINER, "Failed to get message due to Message Exception for MessageKey{0}, error message: {1)}", messageKey, e.getMessage());
		}

		if(config.collectApplicationComponent()) {			
			addToMap("ApplicationComponent", appComponent, attributes);
		}

		if(config.collectBusinessMessage()) {
			Boolean busMsg = Boolean.FALSE;
			addToMap("BusinessMessage", busMsg, attributes);
		}

		if(config.collectCancelable()) {
			MessageStatus status = msgdata.getStatus();
			Boolean cancelable = null;
			if(status == null) {
				cancelable = Boolean.FALSE;
			} else {
				cancelable = !status.equals(MessageStatus.HOLDING) && !status.equals(MessageStatus.TO_BE_DELIVERED)
						&& !status.equals(MessageStatus.WAITING) && !status.equals(MessageStatus.NON_DELIVERED)
						? Boolean.FALSE
								: Boolean.TRUE;
			}
			addToMap("Cancelable", cancelable, attributes);
		}

		if(config.collectConnectionName()) {
			String connName = msgdata.getConnectionName();
			addToMap("ConnectionName", connName, attributes);
		}

		if(config.collectCorrelationId()) {
			String corrId = msgdata.getCorrelationID();
			addToMap("CorrelationID", corrId, attributes);
		}

		if(config.collectDirection()) {
			String direction = msgdata.getMessageKey().getDirection().toString();
			addToMap("Direction", direction, attributes);
		}

		if(config.collectDuration()) {
			Date startTime = msgdata.getSentReceiveTime();
			Date endTime = msgdata.getTransmitDeliverTime();
			Long duration = null;
			if(startTime != null && endTime != null && endTime.after(startTime)) {
				duration = endTime.getTime() - startTime.getTime();
			}
			addToMap("Duration", duration, attributes);
		}

		if(config.collectEditable()) {
			MessageStatus status = msgdata.getStatus();
			Boolean editable = null;
			if(status == null) {
				editable = Boolean.FALSE;
			} else {
				editable = MessageStatus.NON_DELIVERED.equals(status) ? "XI".equalsIgnoreCase(msgdata.getProtocol()) ? Boolean.TRUE : Boolean.FALSE : Boolean.FALSE;
			}
			addToMap("Editiable", editable, attributes);
		}

		if(config.collectEndpoint()) {
			addToMap("Endpoint", msgdata.getAddress(), attributes);
		}

		if(config.collectEndTime()) {
			addToMap("EndTime", msgdata.getTransmitDeliverTime(), attributes);
		}

		if(config.collectErrorCategory()) {
			String errorCat = msgdata.getErrorCategory();
			addToMap("ErrorCategory", errorCat, attributes);
		}

		if(config.collectErrorCode()) {
			String errorCode = msgdata.getErrorCode();
			addToMap("ErrorCode", errorCode, attributes);
		}

		if(config.collectErrorLabel()) {
			//			int errLabel = data.getErrorLabel();
			addToMap("ErrorLabel", NOT_REPORTED, attributes);
		}

		if(config.collectHeaders()) {
			String headers = msgdata.getHeaders();
			addToMap("Headers", headers, attributes);
		}

		KeyFields keyFields = msgdata.getKeyFields();
		MessageInterface msgInterface = getMessageInterface(keyFields);


		LinkedHashMap<String, Object> interfaceAttrs = getMapForMessageInterface(msgInterface);
		if(interfaceAttrs != null && !interfaceAttrs.isEmpty()) {
			attributes.putAll(interfaceAttrs);
		}

		if(config.collectIsPersistent()) {
			boolean isPerst =  msgdata.isPersistent();
			addToMap("IsPersistent", isPerst, attributes);
		}

		if(config.collectMessageId()) {
			String msgId = msgdata.getMessageID();
			addToMap("MessageId", msgId, attributes);
		}

		if(config.collectMessageKey()) {
			String msgKey = msgdata.getMessageKey().toString();
			addToMap("MessageKey", msgKey, attributes);
		}

		if(config.collectMessagePriority()) {
			int msgPriority = msgdata.getMessagePriority().getValue();
			addToMap("MessagePriority", msgPriority, attributes);
		}

		if(config.collectMessageType()) {
			String msgType = msgdata.getType().toString();
			addToMap("MessageType", msgType, attributes);
		}

		if(config.collectNodeId()) {
			int nodeId = msgdata.getNodeId();
			addToMap("NodeId", nodeId, attributes);
		}

		if(config.collectParentId()) {
			String parentID = msgdata.getParentID();
			addToMap("ParentId", parentID, attributes);
		}

		if(config.collectPassport()) {
			String passport = msgdata.getPassport();
			addToMap("Passport", passport, attributes);
		}

		if(config.collectPassportConnectionCounter()) {
			int connectionCounter = msgdata.getPassportConnectionCounter();
			addToMap("PassportConnectionCounter", connectionCounter, attributes);
		}

		if(config.collectPassportConnectionID()) {
			String connectionCounterID = msgdata.getPassportConnectionID();
			addToMap("PassportConnectionID", connectionCounterID, attributes);
		}

		if(config.collectPassportPreviousComponent()) {
			String previousComp = msgdata.getPassportPreviousComponent();
			addToMap("PassportPreviousComponent", previousComp, attributes);
		}

		if(config.collectPassportRootContextID()) {
			String rootID = msgdata.getPassportRootContextID();
			addToMap("PassportRootContextID", rootID, attributes);
		}

		if(config.collectPassportTID()) {
			String tid = msgdata.getPassportTID();
			addToMap("PassportTID", tid, attributes);
		}

		if(config.collectPayloadPermissionWarning()) {
			//boolean payloadWarning = data.getPayloadPermissionWarning();
			addToMap("PayloadPermissionWarning", NOT_REPORTED, attributes);
		}

		if(config.collectPersistUntil()) {
			Date persistUntil = new Date(msgdata.getPersistUntil());

			addToMap("PersistUntil", persistUntil, attributes);
		}
		if(config.collectProtocol()) {
			String protocol = msgdata.getProtocol();
			addToMap("Protocol", protocol, attributes);
		}

		if(config.collectQualityOfService()) {
			String qos = null;

			DeliverySemantics deliverySematics = msgdata.getDeliverySemantics();
			if(deliverySematics != null) {
				qos = deliverySematics.toString();
			}
			addToMap("QualityOfService", qos, attributes);
		}

		MessageInterface receiverInterface = msgInterface;
		LinkedHashMap<String, Object> recAttrs = getMapForReceiverMessageInterface(receiverInterface);
		if(recAttrs != null && !recAttrs.isEmpty()) {
			attributes.putAll(recAttrs);
		}

		if(config.collectReceiverName()) {
			String receiverName = msgInterface != null ? msgInterface.getReceiverComponent() : null;
			addToMap("ReceiverName", receiverName, attributes);
		}

		if(config.collectReceiverParty_all()) {
			MessageParty recParty = new MessageParty(keyFields.getToParty().getName());

			addToMap("ReceiverParty-Name", recParty.getName(), attributes);
			addToMap("ReceiverParty-Agency", recParty.getAgency(), attributes);
			addToMap("ReceiverParty-Schema", recParty.getSchema(), attributes);
		} else {
			MessageParty recParty = new MessageParty(keyFields.getToParty().getName());
			// check if we need to collect one of the attributes
			if(config.collectReceiverParty_name()) {
				addToMap("ReceiverParty-Name", recParty.getName(), attributes);
			}
			if(config.collectReceiverParty_agency()) {
				addToMap("ReceiverParty-Agency", recParty.getAgency(), attributes);
			}
			if(config.collectReceiverParty_schema()) {
				addToMap("ReceiverParty-Schema", recParty.getSchema(), attributes);
			}
		}

		if(config.collectReferenceID()) {
			String refId = msgdata.getRefToMsgID();
			addToMap("ReferenceID", refId, attributes);
		}

		if(config.collectRetries()) {
			int retries = msgdata.getRetries();
			addToMap("Retries", retries, attributes);
		}

		if(config.collectRetryInterval()) {
			long retryInterval = msgdata.getRetryInterval();
			addToMap("RetryInterval", retryInterval, attributes);
		}

		if(config.collectRootID()) {
			String rootId = msgdata.getRootID();
			addToMap("RootID", rootId, attributes);
		}

		if(config.collectScenarioIdentifier()) {
			String scenerioIdent = msgdata.getScenarioIdentifier();
			addToMap("ScenarioIdentifier", scenerioIdent, attributes);
		}

		if(config.collectScheduleTime()) {
			Date scheduleTime = msgdata.getNextDeliveryTime();
			addToMap("ScheduleTime", scheduleTime, attributes);
		}

		MessageInterface senderInterface = msgInterface;
		LinkedHashMap<String, Object> senderAttrs = getMapForSenderMessageInterface(senderInterface);
		if(senderAttrs != null && !senderAttrs.isEmpty()) {
			attributes.putAll(senderAttrs);
		}

		if(config.collectSenderName()) {
			String senderName = msgInterface != null ? msgInterface.getReceiverParty() : null;
			addToMap("SenderName", senderName, attributes);
		}

		if(config.collectSenderParty_all()) {
			MessageParty senderParty = new MessageParty(keyFields.getFromParty().getName());
			addToMap("SenderParty-Name", senderParty.getName(), attributes);
			addToMap("SenderParty-Agency", senderParty.getAgency(), attributes);
			addToMap("SenderParty-Schema", senderParty.getSchema(), attributes);
		} else {
			MessageParty senderParty = new MessageParty(keyFields.getFromParty().getName());
			if(config.collectSenderParty_name()) {
				addToMap("SenderParty-Name", senderParty.getName(), attributes);
			}
			if(config.collectSenderParty_agency()) {
				addToMap("SenderParty-Agency", senderParty.getAgency(), attributes);
			}
			if(config.collectSenderParty_schema()) {
				addToMap("SenderParty-Schema", senderParty.getSchema(), attributes);
			}
		}

		if(config.collectSequenceID()) {
			String seqId = msgdata.getSequenceID();
			addToMap("SequenceID", seqId, attributes);
		}

		if(config.collectSequenceNumber()) {
			Long seqNum = msgdata.getSequenceNumber();
			addToMap("SequenceNumber", seqNum, attributes);
		}

		if(config.collectSerializationContext()) {
			String serializationCtx = msgdata.getSerializationContext();
			addToMap("SerializationContext", serializationCtx, attributes);
		}

		if(config.collectServiceDefinition()) {
			addToMap("ServiceDefinition", svcDefinition, attributes);
		}

		if(config.collectSize()) {
			long size = msgdata.getMessageSize();
			addToMap("Size", size, attributes);
		}

		if(config.collectSoftwareComponent()) {
			addToMap("SoftwareComponent", softComponent, attributes);
		}

		if(config.collectStartTime()) {
			Date startTime = msgdata.getSentReceiveTime();
			addToMap("StartTime", startTime, attributes);
		}

		if(config.collectStatus()) {
			String status = msgdata.getStatus().toString();
			addToMap("Status", status, attributes);
		}

		if(config.collectTimesFailed()) {
			long timesFailed = msgdata.getTimesFailed();
			addToMap("TimesFailed", timesFailed, attributes);
		}

		if(config.collectTransport()) {
			String transport = msgdata.getTransport().toString();
			addToMap("Transport", transport, attributes);
		}

		if(config.collectValidUntil()) {
			Date validUntil = new Date(msgdata.getValidUntil());
			addToMap("ValidUntil", validUntil, attributes);
		}

		if(config.collectVersion()) {
			int version = msgdata.getVersionNumber();
			addToMap("Version", version, attributes);
		}

		if(config.collectWasEdited()) {
			boolean wasEdited = msgdata.wasEdited();
			addToMap("WasEdited", wasEdited, attributes);
		}


		if(config.collectingUserAttributes()) {
			if(currentAttributes != null && !currentAttributes.isEmpty()) {
				Set<String> keys = currentAttributes.keySet();
				for(String key : keys) {
					String mKey = key.toLowerCase().trim();
					String tmp = "modulecontext-";
					if(mKey.startsWith(tmp)) {
						mKey = mKey.replace(tmp, "");
					}
					tmp = "SupplementalData-".toLowerCase();
					if(mKey.startsWith(tmp)) {
						mKey = mKey.replace(tmp, "");
					}
					
					if (config.collectUserAttribute(mKey)) {
						String value = currentAttributes.get(key.toLowerCase());
						if (value == null)
							value = NOT_REPORTED;
						addToMap("Attribute-" + key.trim(), value, attributes);
					}

				}
			}
		}
		Gson gson = new Gson();

		return gson.toJson(attributes);
	}

	private static LinkedHashMap<String,Object> getMapForMessageInterface(MessageInterface msgInterface) {
		AttributeConfig config = AttributeConfig.getInstance();
		if (msgInterface != null) {
			LinkedHashMap<String,Object> map = new LinkedHashMap<String, Object>();
			if(config.collectInterface_all() || config.collectInterface_name()) {
				addToMap("MessageInterface-Name", msgInterface.getName(), map);
			}
			if(config.collectInterface_all() || config.collectInterface_namespace()) {
				addToMap("MessageInterface-Namespace", msgInterface.getNamespace(), map);
			}
			if(config.collectInterface_all() || config.collectInterface_receiverComponent()) {
				addToMap("MessageInterface-ReceiverComponent", msgInterface.getReceiverComponent(), map);
			}
			if(config.collectInterface_all() || config.collectInterface_receiverParty()) {
				addToMap("MessageInterface-ReceiverParty", msgInterface.getReceiverParty(), map);
			}
			if(config.collectInterface_all() || config.collectInterface_senderComponent()) {
				addToMap("MessageInterface-SenderComponent", msgInterface.getSenderComponent(), map);
			}
			if(config.collectInterface_all() || config.collectInterface_senderParty()) {
				addToMap("MessageInterface-SenderParty", msgInterface.getSenderParty(), map);
			}
			return map;
		}
		return null;
	}


	private static LinkedHashMap<String,Object> getMapForReceiverMessageInterface(MessageInterface msgInterface) {
		AttributeConfig config = AttributeConfig.getInstance();
		if (msgInterface != null) {
			LinkedHashMap<String,Object> map = new LinkedHashMap<String, Object>();
			if(config.collectReceiverInterface_all() || config.collectReceiverInterface_name()) {
				addToMap("ReceiverMessageInterface-Name", msgInterface.getName(), map);
			}
			if(config.collectReceiverInterface_all() || config.collectReceiverInterface_namespace()) {
				addToMap("ReceiverMessageInterface-Namespace", msgInterface.getNamespace(), map);
			}
			if(config.collectReceiverInterface_all() || config.collectReceiverInterface_receiverComponent()) {
				addToMap("ReceiverMessageInterface-ReceiverComponent", msgInterface.getReceiverComponent(), map);
			}
			if(config.collectReceiverInterface_all() || config.collectReceiverInterface_receiverParty()) {
				addToMap("ReceiverMessageInterface-ReceiverParty", msgInterface.getReceiverParty(), map);
			}
			if(config.collectReceiverInterface_all() || config.collectReceiverInterface_senderComponent()) {
				addToMap("ReceiverMessageInterface-SenderComponent", msgInterface.getSenderComponent(), map);
			}
			if(config.collectReceiverInterface_all() || config.collectReceiverInterface_senderParty()) {
				addToMap("ReceiverMessageInterface-SenderParty", msgInterface.getSenderParty(), map);
			}
			return map;
		}
		return null;
	}

	private static LinkedHashMap<String,Object> getMapForSenderMessageInterface(MessageInterface msgInterface) {
		AttributeConfig config = AttributeConfig.getInstance();
		if (msgInterface != null) {
			LinkedHashMap<String,Object> map = new LinkedHashMap<String, Object>();
			if(config.collectSenderInterface_all() || config.collectSenderInterface_name()) {
				addToMap("SenderMessageInterface-Name", msgInterface.getName(), map);
			}
			if(config.collectSenderInterface_all() || config.collectSenderInterface_namespace()) {
				addToMap("SenderMessageInterface-Namespace", msgInterface.getNamespace(), map);
			}
			if(config.collectSenderInterface_all() || config.collectSenderInterface_receiverComponent()) {
				addToMap("SenderMessageInterface-ReceiverComponent", msgInterface.getReceiverComponent(), map);
			}
			if(config.collectSenderInterface_all() || config.collectSenderInterface_receiverParty()) {
				addToMap("SenderMessageInterface-ReceiverParty", msgInterface.getReceiverParty(), map);
			}
			if(config.collectSenderInterface_all() || config.collectSenderInterface_senderComponent()) {
				addToMap("SenderMessageInterface-SenderComponent", msgInterface.getSenderComponent(), map);
			}
			if(config.collectSenderInterface_all() || config.collectSenderInterface_senderParty()) {
				addToMap("SenderMessageInterface-SenderParty", msgInterface.getSenderParty(), map);
			}
			return map;
		}
		return null;
	}

	private static void addToMap(String key, Object value, Map<String, Object> attributes) {
		if(key != null && !key.isEmpty()) {
			if (value != null) {
				if (value instanceof String) {
					String sValue = (String) value;
					if (!sValue.isEmpty()) {
						attributes.put(key, sValue);
					} else {
						attributes.put(key, EMPTY_STRING);
					}
				} else {
					attributes.put(key, value);
				} 
			} else {
				attributes.put(key, NOT_REPORTED);
			}
		}

	}

	private static MessageInterface getMessageInterface(KeyFields keyFields) {
		Action action = keyFields.getAction();
		if(action != null) {
			MessageInterface receiverInterface = new MessageInterface(action.getType(),action.getName());
			Party senderParty = keyFields.getFromParty();
			receiverInterface.setSenderParty(senderParty.toString());
			Party receiverParty = keyFields.getToParty();
			receiverInterface.setReceiverParty(receiverParty.toString());
			Service senderService = keyFields.getFromService();
			receiverInterface.setSenderComponent(senderService.toString());
			Service receiverService = keyFields.getToService();
			receiverInterface.setReceiverComponent(receiverService.toString());
			return receiverInterface;
		}
		return null;
	}

}
