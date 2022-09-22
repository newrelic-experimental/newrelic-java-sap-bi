package com.sap.aii.af.service.impl;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.nr.instrumentation.sap.adaptermonitoring.AdapterCollector;

/*
 * This class is instrumented in order to insure that the AdapterCollector get initialized.
 * It is a class that gets constructed during SAP startup
 */
@Weave
public class ServiceFrameImpl {
	
	@WeaveAllConstructors
	public ServiceFrameImpl() {
		if(!AdapterCollector.initialized) {
			AdapterCollector.init();
		}
	}

}
