package com.sap.it.op.mpl.archive;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.sap.gateway.GatewayMonitor;

@Weave
public class ArchiveMessageAccess {

	public ArchiveMessageAccess() {
		if(!GatewayMonitor.initialized) {
			GatewayMonitor.initialize();
		}
	}
}
