package com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.sap.gateway.GatewayPublicMonitor;
import com.newrelic.instrumentation.sap.gateway.Utils;
import com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPrivateIGWController;
import com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPublicIGWController.IContextNode;

@Weave
public class IGWController {

	public IGWController(IPrivateIGWController wdThis) {
		if(!Utils.initialized) {
			Utils.init();
		}
		IContextNode ctx = wdThis != null ? wdThis.wdGetContext() : null;
		GatewayPublicMonitor.addIContextNode(ctx);
	}
}
