package com.sap.aii.af.sdk.xi.net;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransportType;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.Interface)
public abstract class ClientInOut {
	
	@Trace(dispatcher = true)
	public TransportMessage call(TransportMessage inMesssage, Endpoint endpoint) {
		SAPAFHeaders inHeaders = new SAPAFHeaders(inMesssage);
		NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.Other, inHeaders);
		TransportMessage outMessage = Weaver.callOriginal();
		SAPAFHeaders outHeaders = new SAPAFHeaders(outMessage);
		NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(outHeaders);
		return outMessage;
	}

}
