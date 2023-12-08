package com.sap.it.op.mpl;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.sap.gateway.GatewayMonitor;

@Weave
public class MPLPersistBean {

	public MPLPersistBean() {
		if(!GatewayMonitor.initialized) {
			GatewayMonitor.initialize();
		}
	}
}
