package com.sap.engine.messaging.impl.core.service;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;
import com.sap.engine.messaging.impl.core.MessageController;

@Weave
public abstract class Send extends AbstractAsyncCommand {

	
	public Send(MessageController controller, TransportableMessage transportableMessage, String connectionName) {
		super(controller, transportableMessage, connectionName);
	}
	
	@Trace
	public void execute() {
		Weaver.callOriginal();
	}
}
