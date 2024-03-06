package com.sap.tc.webdynpro.progmodel.gci;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.newrelic.instrumentation.sap.gateway_2.GatewayPublicMonitor;
import com.newrelic.instrumentation.sap.gateway_2.Utils;

@Weave(type = MatchType.Interface)
public abstract class IGCICustomController {
	
	@WeaveAllConstructors
	public IGCICustomController() {
		if(!Utils.initialized) {
			Utils.init();
		}
		GatewayPublicMonitor.addIGCICustomController(this);
	}

}
