package com.sap.aii.af.service.impl;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.nr.instrumentation.sap.adaptermonitoring.AdapterCollector;

@Weave
public abstract class ConnectionService {

	@WeaveAllConstructors
	public ConnectionService() {
		if(!AdapterCollector.initialized) {
			AdapterCollector.init();
		}
	}
}
