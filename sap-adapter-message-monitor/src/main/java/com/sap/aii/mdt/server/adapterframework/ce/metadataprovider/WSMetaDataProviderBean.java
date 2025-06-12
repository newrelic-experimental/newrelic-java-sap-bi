package com.sap.aii.mdt.server.adapterframework.ce.metadataprovider;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.MessageMonitor;

@Weave
public class WSMetaDataProviderBean {

	public WSMetaDataProviderBean() {
		if(!MessageMonitor.initialized) {
			MessageMonitor.initialize();
		}
	}
}
