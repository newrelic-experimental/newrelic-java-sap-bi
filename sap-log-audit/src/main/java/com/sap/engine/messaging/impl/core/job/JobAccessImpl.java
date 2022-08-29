package com.sap.engine.messaging.impl.core.job;

import com.newrelic.api.agent.weaver.Weave;
import com.nr.instrumentation.sap.auditlogging.Logger;

@Weave
public abstract class JobAccessImpl {
	
	public JobAccessImpl() {
		if(!Logger.initialized) {
			Logger.init();
		}
	}

}
