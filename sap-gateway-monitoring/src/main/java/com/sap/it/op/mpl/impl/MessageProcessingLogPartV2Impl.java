package com.sap.it.op.mpl.impl;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.mpl.processing.GatewayUtils;
import com.sap.it.op.mpl.MessageProcessingLogPartDefaultImpl;

@Weave
public abstract class MessageProcessingLogPartV2Impl extends MessageProcessingLogPartDefaultImpl {

	public void markAsCompleted() {
		GatewayUtils.reportMPL(this);
		Weaver.callOriginal();
	}

}
