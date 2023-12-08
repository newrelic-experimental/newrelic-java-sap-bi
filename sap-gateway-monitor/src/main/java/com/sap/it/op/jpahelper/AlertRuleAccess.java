package com.sap.it.op.jpahelper;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.sap.gateway.GatewayMonitor;

@Weave
public abstract class AlertRuleAccess {

	public AlertRuleAccess() {
		if(!GatewayMonitor.initialized) {
			GatewayMonitor.initialize();
		}		
	}
}
