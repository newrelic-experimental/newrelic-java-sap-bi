package com.sap.it.op.agent.mpl.sink;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.mpl.MessageProcessingUtils;
import com.sap.it.op.mpl.MessageProcessingLogPart;

@Weave(type = MatchType.BaseClass)
public abstract class AbstractMessageProcessingLogSink {

	protected void putMpl(MessageProcessingLogPart logPart) {
		MessageProcessingUtils.reportMPL(logPart);
		Weaver.callOriginal();
	}
}
