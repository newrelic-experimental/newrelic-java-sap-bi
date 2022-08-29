package com.sap.engine.messaging.impl.api;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.nr.instrumentation.sap.engineimpl.MessageLogging;

@Weave
public abstract class ConnectionImpl {

	@WeaveAllConstructors
	public ConnectionImpl() {
		if(!MessageLogging.initialized) {
			MessageLogging.init();
		}
	}
}
