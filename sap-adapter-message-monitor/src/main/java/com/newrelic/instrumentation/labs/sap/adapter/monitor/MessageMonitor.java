package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.newrelic.agent.deps.com.google.gson.Gson;
import com.newrelic.api.agent.Logger;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.mdt.api.data.BooleanAttribute;
import com.sap.aii.mdt.api.data.DurationAttribute;
import com.sap.aii.mdt.api.data.MessageInterface;
import com.sap.aii.mdt.api.data.MessageParty;
import com.sap.aii.mdt.server.adapterframework.ws.AdapterFilter;
import com.sap.aii.mdt.server.adapterframework.ws.AdapterFrameworkData;
import com.sap.aii.mdt.server.adapterframework.ws.BusinessAttribute;
import com.sap.aii.mdt.server.adapterframework.ws.MessageSearchReturnValue;
import com.sap.aii.mdt.server.adapterframework.ws.OperationFailedException;
import com.sap.aii.mdt.server.adapterframework.ws.esiext.XIWSMessageMonitoring;

public class MessageMonitor implements Runnable {

	public static boolean initialized = false;
	private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
	private static MessageMonitor INSTANCE = null;
	private static ScheduledFuture<?> scheduledFuture = null;
	private static Date last = new Date(System.currentTimeMillis() - 5L * 60L * 1000L);
	private static long frequency = 3;
	private static long delay = 1;

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
		Date start = new Date();

		AdapterFilter filter = new AdapterFilter();
		filter.setFromTime(last);
		filter.setToTime(start);

		Integer maxMessages = new Integer(1000);

		try {
			MessageSearchReturnValue msgList = XIWSMessageMonitoring.getMessageList(filter, maxMessages);
			AdapterFrameworkData[] adapterFWData = msgList.getList();
			NewRelic.recordMetric("SAP/AdapterMessageMonitor/AdapterFrameworkData",adapterFWData != null ? adapterFWData.length : 0);

			for(AdapterFrameworkData data : adapterFWData) {
				String jsonString = getLogJson(data);				
				AdapterMessageLogger.log(jsonString);
			}

		} catch (OperationFailedException e) {
			LOGGER.log(Level.FINE, e, "Failed to get message list");
		}

		last = start;

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


	private static String getLogJson(AdapterFrameworkData data) {
		LinkedHashMap<String, Object> attributes = new LinkedHashMap<String, Object>();
		AttributeConfig config = AttributeConfig.getInstance();

		if(config.collectApplicationComponent()) {
			String appComp = data.getApplicationComponent();
			addToMap("ApplicationComponent", appComp, attributes);
		}

		if(config.collectConnectionName()) {
			String connName = data.getConnectionName();
			addToMap("Connection", connName, attributes);
		}

		if(config.collectCorrelationId()) {
			String corrId = data.getCorrelationID();
			addToMap("CorrelationID", corrId, attributes);
		}
		if(config.collectDirection()) {
			String direction = data.getDirection();
			addToMap("Direction", direction, attributes);
		}
		if(config.collectDuration()) {
			DurationAttribute duration = data.getDuration();
			addToMap("Duration", duration.getDuration(), attributes);
		}
		if(config.collectEndpoint()) {
			String endPt = data.getEndpoint();
			addToMap("Endpoint", endPt, attributes);
		}
		if(config.collectEndTime()) {
			Date endTime = data.getEndTime();
			addToMap("EndTime", endTime, attributes);
		}
		if(config.collectErrorCategory()) {
			String errorCat = data.getErrorCategory();
			addToMap("ErrorCategory", errorCat, attributes);
		}
		if(config.collectErrorCode()) {
			String errorCode = data.getErrorCode();
			addToMap("ErrorCode", errorCode, attributes);
		}
		if(config.collectErrorLabel()) {
			int errLabel = data.getErrorLabel();
			addToMap("ErrorLabel", errLabel, attributes);
		}
		MessageInterface msgInterface = data.getInterface();
		LinkedHashMap<String, Object> interfaceAttrs = getMapForMessageInterface(msgInterface);
		if(interfaceAttrs != null && !interfaceAttrs.isEmpty()) {
			attributes.putAll(interfaceAttrs);
		}
		if(config.collectPersistent()) {
			boolean isPerst = data.getIsPersistent();
			addToMap("IsPersistent", isPerst, attributes);
		}
		if(config.collectMessageId()) {
			String msgId = data.getMessageID();
			addToMap("MessageId", msgId, attributes);
		}
		if(config.collectMessageKey()) {
			String msgKey = data.getMessageKey();
			addToMap("MessageKey", msgKey, attributes);
		}
		if(config.collectMessagePriority()) {
			int msgPriority = data.getMessagePriority();
			addToMap("MessagePriority", msgPriority, attributes);
		}
		if(config.collectMessageType()) {
			String msgType = data.getMessageType();
			addToMap("MessageType", msgType, attributes);
		}
		if(config.collectNodeId()) {
			int nodeId = data.getNodeId();
			addToMap("NodeId", nodeId, attributes);
		}
		if(config.collectParentId()) {
			String parentID = data.getParentID();
			addToMap("ParentId", parentID, attributes);
		}
		if(config.collectPassport()) {
			String passport = data.getPassport();
			addToMap("Passport", passport, attributes);
		}
		if(config.collectPersistUntil()) {
			Date persistUntil = data.getPersistUntil();
			addToMap("PersistUntil", persistUntil, attributes);
		}
		if(config.collectProtocol()) {
			String protocol = data.getProtocol();
			addToMap("Protocol", protocol, attributes);
		}
		if(config.collectQualityOfService()) {
			String qos = data.getQualityOfService();
			addToMap("QualityOfService", qos, attributes);
		}
		MessageInterface receiverInterface = data.getReceiverInterface();
		LinkedHashMap<String, Object> recAttrs = getMapForReceiverMessageInterface(receiverInterface);
		if(recAttrs != null && !recAttrs.isEmpty()) {
			attributes.putAll(recAttrs);
		}

		if(config.collectReceiverName()) {
			String receiverName = data.getReceiverName();
			addToMap("ReceiverName", receiverName, attributes);
		}
		if(config.collectReceiverParty_all()) {
			MessageParty recParty = data.getReceiverParty();

			addToMap("ReceiverParty-Name", recParty.getName(), attributes);
			addToMap("ReceiverParty-Agency", recParty.getAgency(), attributes);
			addToMap("ReceiverParty-Schema", recParty.getSchema(), attributes);
		} else {
			// check if we need to collect one of the attributes
			MessageParty recParty = data.getReceiverParty();
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
			String refId = data.getReferenceID();
			addToMap("ReferenceID", refId, attributes);
		}
		if(config.collectRetries()) {
			int retries = data.getRetries();
			addToMap("Retries", retries, attributes);
		}
		if(config.collectRetryInterval()) {
			long retryInterval = data.getRetryInterval();
			addToMap("RetryInterval", retryInterval, attributes);
		}
		if(config.collectRootID()) {
			String rootId = data.getRootID();
			addToMap("RootID", rootId, attributes);
		}
		if(config.collectScenarioIdentifier()) {
			String scenerioIdent = data.getScenarioIdentifier();
			addToMap("ScenarioIdentifier", scenerioIdent, attributes);
		}
		if(config.collectScheduleTime()) {
			Date scheduleTime = data.getScheduleTime();
			addToMap("ScheduleTime", scheduleTime, attributes);
		}
		MessageInterface senderInterface = data.getSenderInterface();
		LinkedHashMap<String, Object> senderAttrs = getMapForSenderMessageInterface(senderInterface);
		if(senderAttrs != null && !senderAttrs.isEmpty()) {
			attributes.putAll(senderAttrs);
		}
		if(config.collectSenderName()) {
			String senderName = data.getSenderName();
			addToMap("SenderName", senderName, attributes);
		}
		if(config.collectSenderParty_all()) {
			MessageParty senderParty = data.getSenderParty();
			addToMap("SenderParty-Name", senderParty.getName(), attributes);
			addToMap("SenderParty-Agency", senderParty.getAgency(), attributes);
			addToMap("SenderParty-Schema", senderParty.getSchema(), attributes);
		} else {
			MessageParty senderParty = data.getSenderParty();
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
			String seqId = data.getSequenceID();
			addToMap("SequenceID", seqId, attributes);
		}
		if(config.collectSequenceNumber()) {
			Long seqNum = data.getSequenceNumber();
			addToMap("SequenceNumber", seqNum, attributes);
		}
		if(config.collectSerializationContext()) {
			String serializationCtx = data.getSerializationContext();
			addToMap("SerializationContext", serializationCtx, attributes);
		}
		if(config.collectServiceDef()) {
			String serviceDef = data.getServiceDefinition();
			addToMap("ServiceDef", serviceDef, attributes);
		}
		if(config.collectSize()) {
			long size = data.getSize();
			addToMap("Size", size, attributes);
		}
		if(config.collectSoftwareComponent()) {
			String swComponent = data.getSoftwareComponent();
			addToMap("SoftwareComponent", swComponent, attributes);
		}
		if(config.collectStartTime()) {
			Date startTime = data.getStartTime();
			addToMap("StartTime", startTime, attributes);
		}
		if(config.collectStatus()) {
			String status = data.getStatus();
			addToMap("Status", status, attributes);
		}
		if(config.collectTimesFailed()) {
			long timesFailed = data.getTimesFailed();
			addToMap("TimesFailed", timesFailed, attributes);
		}
		if(config.collectTransport()) {
			String transport = data.getTransport();
			addToMap("Transport", transport, attributes);
		}
		if(config.collectValidUntil()) {
			Date validUntil = data.getValidUntil();
			addToMap("ValidUntil", validUntil, attributes);
		}
		if(config.collectVersion()) {
			String version = data.getVersion();
			addToMap("Version", version, attributes);
		}
		if(config.collectBusinessMessage()) {
			BooleanAttribute busMsg = data.getBusinessMessage();
			if(busMsg != null) {
				addToMap("BusinessMessage", busMsg.getValue(), attributes);
			}
		}

		BusinessAttribute[] busAttrs = data.getBusinessAttributes();
		if(busAttrs != null) {
			for(BusinessAttribute busAttr : busAttrs) {
				addToMap("BusinessAttribute-"+busAttr.getName(), busAttr.getValue(), attributes);
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
					}
				} else {
					attributes.put(key, value);
				} 
			} else {
				attributes.put(key, "Not_Reported");
			}
		}

	}

}
