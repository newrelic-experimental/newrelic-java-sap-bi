package com.sap.it.op.mpl.impl;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.mpl.MessageProcessingUtils;
import com.sap.it.op.mpl.MessageProcessingLogPartDefaultImpl;

@Weave
public abstract class MessageProcessingLogPartV2Impl extends MessageProcessingLogPartDefaultImpl {

	public void markAsCompleted() {
		MessageProcessingUtils.reportMPL(this);
		Weaver.callOriginal();
	}

}
