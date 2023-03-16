package com.sap.aii.af.sdk.xi.net;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.Interface)
public abstract class ClientIn {

	@Trace
	public void send(TransportMessage message, Endpoint endpoint) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","AF","ClientIn",getClass().getSimpleName(),"send");
		SAPAFHeaders headers = new SAPAFHeaders(message);
		NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(headers);
		Weaver.callOriginal();
	}
}
