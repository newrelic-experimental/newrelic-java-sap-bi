package com.sap.it.op.mpl;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.sap.gateway_2.GatewayLogger;

@Weave
public abstract class MPLPersistBean {
	
	public void onStartup() {
		if(!GatewayLogger.initialized) {
			GatewayLogger.initialize();
		}
	}

}
