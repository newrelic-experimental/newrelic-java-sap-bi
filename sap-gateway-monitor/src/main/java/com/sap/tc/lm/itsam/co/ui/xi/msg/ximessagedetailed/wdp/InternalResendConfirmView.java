package com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.sap.gateway.Utils;
import com.sap.tc.webdynpro.progmodel.gci.IGCIView;

@Weave
public class InternalResendConfirmView {

	public InternalResendConfirmView(IGCIView alterEgo) {
		if(!Utils.initialized) {
			Utils.init();
		}
	}
}
