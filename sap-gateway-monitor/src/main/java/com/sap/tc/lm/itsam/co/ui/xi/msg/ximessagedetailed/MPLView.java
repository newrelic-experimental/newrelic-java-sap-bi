package com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.sap.gateway.GatewayMPLViewMonitor;
import com.newrelic.instrumentation.sap.gateway.Utils;
import com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPrivateMPLView;

@Weave
public class MPLView {

	public MPLView(IPrivateMPLView wdThis) {
		if(!Utils.initialized) {
			Utils.init();
		}
		GatewayMPLViewMonitor.addIPrivateMPLView(wdThis);
	}
}
