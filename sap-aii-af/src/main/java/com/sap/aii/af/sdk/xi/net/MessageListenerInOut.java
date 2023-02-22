package com.sap.aii.af.sdk.xi.net;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransportType;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.Interface)
public abstract class MessageListenerInOut {

	@Trace(dispatcher = true)
	public TransportMessage onMessage(TransportMessage inMessage) {
		SAPAFHeaders inHeaders = new SAPAFHeaders(inMessage);
		NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.Other, inHeaders);
		TransportMessage outMessage = Weaver.callOriginal();
		SAPAFHeaders outHeaders = new SAPAFHeaders(outMessage);
		NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(outHeaders);
		return outMessage;
	}
}
