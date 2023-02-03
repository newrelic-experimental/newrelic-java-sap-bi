package com.sap.aii.af.sdk.xi.net;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransportType;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.Interface)
public abstract class MessageListenerIn {

	@Trace(dispatcher = true)
	public void onMessage(TransportMessage message) {
		SAPAFHeaders headers = new SAPAFHeaders(message);
		NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.Other, headers);
		Weaver.callOriginal();
	}
}
