package com.sap.it.op.agent.collector.camel;

import org.apache.camel.Exchange;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.mpl.GatewayUtils;
import com.sap.it.commons.data.Pair;
import com.sap.it.op.mpl.MessageProcessingLogPart;

@Weave
public abstract class MplAccessor {

	public Pair<MessageProcessingLogPart, Boolean> grantMessageProcessingLogForModelStepId(Exchange exchange, String modelStepId) {
		Pair<MessageProcessingLogPart, Boolean> result = Weaver.callOriginal();
		GatewayUtils.reportMPL(result.getFirst());
		return result;
	}
}
