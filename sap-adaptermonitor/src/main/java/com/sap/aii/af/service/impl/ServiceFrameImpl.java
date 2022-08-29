package com.sap.aii.af.service.impl;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.nr.instrumentation.sap.adaptermonitoring.AdapterCollector;

@Weave
public class ServiceFrameImpl {
	
	@WeaveAllConstructors
	public ServiceFrameImpl() {
		if(!AdapterCollector.initialized) {
			AdapterCollector.init();
		}
	}

}
