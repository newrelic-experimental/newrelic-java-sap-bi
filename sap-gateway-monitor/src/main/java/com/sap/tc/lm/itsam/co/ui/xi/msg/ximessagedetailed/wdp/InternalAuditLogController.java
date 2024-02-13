package com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.sap.gateway.GatewayPublicMonitor;
import com.newrelic.instrumentation.sap.gateway.Utils;
import com.sap.tc.webdynpro.progmodel.gci.IGCICustomController;

@Weave
public class InternalAuditLogController {

	public InternalAuditLogController(IGCICustomController alterEgo) {
		if(!Utils.initialized) {
			Utils.init();
		}
		GatewayPublicMonitor.addIGCICustomController(alterEgo);
	}
}
