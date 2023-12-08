package com.sap.it.op.mpl.db;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.sap.gateway.GatewayMonitor;

@Weave
public class DatabaseMessageAccess {

	public DatabaseMessageAccess() {
		if(!GatewayMonitor.initialized) {
			GatewayMonitor.initialize();
		}
	}
}
