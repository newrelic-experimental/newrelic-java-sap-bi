package com.sap.engine.messaging.impl.core;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.labs.sap.auditlogging.Logger;

@Weave
public abstract class MessageController {
	
	public MessageController() {
		if(!Logger.initialized) {
			Logger.init();
		}
	}

}