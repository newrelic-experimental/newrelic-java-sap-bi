package com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.sap.gateway.GatewayMonitor;
import com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPrivateMPLView;

@Weave
public class MPLView {

	public MPLView(IPrivateMPLView wdThis) {
		if(!GatewayMonitor.initialized) {
			GatewayMonitor.initialize();
		}
	}
}
