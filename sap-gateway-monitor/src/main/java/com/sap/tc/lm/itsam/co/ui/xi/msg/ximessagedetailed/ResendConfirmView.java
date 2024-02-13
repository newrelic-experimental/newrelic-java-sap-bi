package com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.sap.gateway.Utils;
import com.sap.tc.lm.itsam.co.ui.xi.msg.ximessagedetailed.wdp.IPrivateResendConfirmView;

@Weave
public class ResendConfirmView {

	public ResendConfirmView(IPrivateResendConfirmView wdThis) {
		if(!Utils.initialized) {
			Utils.init();
		}
	}
}
