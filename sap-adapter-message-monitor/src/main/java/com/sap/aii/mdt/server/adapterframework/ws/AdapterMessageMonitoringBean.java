package com.sap.aii.mdt.server.adapterframework.ws;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.MessageMonitor;

@Weave
public class AdapterMessageMonitoringBean {

	public AdapterMessageMonitoringBean() {
		if(!MessageMonitor.initialized) {
			MessageMonitor.initialize();
		}
	}

}
