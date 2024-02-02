package com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.sap.gateway.GatewayMonitor;
import com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPrivateMainView;

@Weave
public class MainView {

	public MainView(IPrivateMainView wdThis) {
		if(!GatewayMonitor.initialized) {
			GatewayMonitor.initialize();
		}
	}
}