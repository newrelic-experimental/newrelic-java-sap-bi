package com.sap.engine.messaging.impl.api;

import com.newrelic.api.agent.weaver.Weave;
import com.nr.instrumentation.sap.engineimpl.MessageLogging;

@Weave
public abstract class MessageFactoryImpl {

	public MessageFactoryImpl(String protocol) {
		if(!MessageLogging.initialized) {
			MessageLogging.init();
		}
	}
}
