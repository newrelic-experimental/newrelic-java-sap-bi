package com.sap.engine.messaging.impl.core.service;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.engineimpl.EngineUtils;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;
import com.sap.engine.messaging.impl.core.MessageController;

@Weave
public abstract class Receive extends AbstractAsyncCommand {

	public Receive(MessageController controller, TransportableMessage message, String connectionName) {
		super(controller, message, connectionName);
	}
	
	@Trace(dispatcher = true)
	public void execute() {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, transportableMessage);
		EngineUtils.recordValue(attributes, "ConnectionName", connectionName);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		
		Weaver.callOriginal();
	}
}
