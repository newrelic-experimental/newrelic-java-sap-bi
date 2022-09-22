package com.sap.engine.messaging.impl.core;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.sap.message.Utils;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.MessageStatus;
import com.sap.engine.interfaces.messaging.api.MessageType;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;
import com.sap.engine.interfaces.messaging.spi.MessagePriority;
import com.sap.engine.messaging.impl.core.queue.QueueMessage;
import com.sap.engine.messaging.impl.core.queue.ReceiverData;
import com.sap.engine.messaging.impl.core.status.StatusCollectorDataImpl;

@Weave
public abstract class MessageController {

	@Trace(dispatcher = true)
	public void putMessageInQueue(MessageKey messageKey, String connectionName, MessageType messageType,
			MessagePriority messagePriority, StatusCollectorDataImpl data, ReceiverData recvData, boolean adminAction) {
		HashMap<String, Object> attributes = new HashMap<>();
		Utils.addAttribute(attributes, "MessageKey", messageKey);
		Utils.addAttribute(attributes, "ConnectionName", connectionName);
		Utils.addAttribute(attributes, "MessageType", messageType);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		
		Weaver.callOriginal();
	}

	@Trace(dispatcher = true)
	public void putMessageInStore(QueueMessage queueMessage, boolean syncResponse, boolean updatePerformance) {
		HashMap<String, Object> attributes = new HashMap<>();
		Utils.addQueueMessage(attributes, queueMessage);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	public void scheduleMessage(MessageKey messageKey, long scheduleTime, String connectionName,
			MessageType messageType, MessagePriority messagePriority, MessageStatus oldStatus, long transDelTime,
			int timeFailed, ReceiverData recvData, MessagingException Ex) {
		HashMap<String, Object> attributes = new HashMap<>();
		Utils.addAttribute(attributes, "MessageKey", messageKey);
		Utils.addAttribute(attributes, "ConnectionName", connectionName);
		Utils.addAttribute(attributes, "MessageType", messageType);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
}
