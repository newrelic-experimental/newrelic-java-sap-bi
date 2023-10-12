package com.sap.aii.mdt.itsam.mbeans.performanceMonitor.mbean;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.newrelic.instrumentation.labs.sap.StatsCollector;

@Weave
public abstract class SAP_ITSAMXIAEPerfServiceWrapper {

	
	@WeaveAllConstructors
	public SAP_ITSAMXIAEPerfServiceWrapper() {
		if(!StatsCollector.intialized) {
			StatsCollector.initialize();
		}
	}
}
