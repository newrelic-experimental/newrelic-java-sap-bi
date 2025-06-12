package com.sap.aii.mdt.server.compmoni;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.MessageMonitor;

@Weave
public class ComponentMonitoringBean {

	public ComponentMonitoringBean() {
		if(!MessageMonitor.initialized) {
			MessageMonitor.initialize();
		}
	}
}
