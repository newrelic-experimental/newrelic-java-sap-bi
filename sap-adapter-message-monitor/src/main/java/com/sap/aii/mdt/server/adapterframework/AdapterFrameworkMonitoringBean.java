package com.sap.aii.mdt.server.adapterframework;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.MessageMonitor;

@Weave
public abstract class AdapterFrameworkMonitoringBean {

	public AdapterFrameworkMonitoringBean() {
		if(!MessageMonitor.initialized) {
			MessageMonitor.initialize();
		}
	}
}
