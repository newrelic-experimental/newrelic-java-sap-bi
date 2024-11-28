package com.sap.engine.messaging.impl.core.queue;

import java.sql.ResultSet;

import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.labs.sap.engineimpl.NRSAPHeaders;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.MessageType;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;

@Weave
public abstract class QueueMessage {
	
	public abstract TransportableMessage getTransportableMessage();	
	public abstract MessageKey getMessageKey();
	
	@NewField
	public NRSAPHeaders nr_headers = null;
	
	public QueueMessage(TransportableMessage transportableMessage, MessageType messageType, String connectionName) {
	}

	public QueueMessage(TransportableMessage transportableMessage, ResultSet rs) {
		
	}
	
	
}
