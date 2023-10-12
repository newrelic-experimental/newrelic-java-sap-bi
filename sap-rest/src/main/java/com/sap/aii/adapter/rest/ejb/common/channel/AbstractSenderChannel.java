package com.sap.aii.adapter.rest.ejb.common.channel;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.rest.RESTUtils;
import com.sap.aii.adapter.xi.ms.XIMessage;

@Weave(type=MatchType.BaseClass)
public abstract class AbstractSenderChannel {

	@Trace
	protected XIMessage send(XIMessage message) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("Endpoint-Address", message.getEndpoint().getAddress());
		attributes.put("Endpoint-Transport", message.getEndpoint().getTransport());
		attributes.put("InterefaceName", message.getInterfaceName());
		attributes.put("MessageKey-Id", message.getMessageKey().getMessageId());

		traced.setMetricName("Custom","SAP","REST","AbstractSenderChannel",getClass().getSimpleName(),"send");
		RESTUtils.reportMessage(message, null);
		return Weaver.callOriginal();
	}
	
	@Trace
	protected void sendAsync(XIMessage message) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","REST","AbstractSenderChannel",getClass().getSimpleName(),"sendAsync");
		Weaver.callOriginal();
	}
	
	@Trace
	protected XIMessage sendSync(XIMessage message) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","REST","AbstractSenderChannel",getClass().getSimpleName(),"sendSync");
		return Weaver.callOriginal();
	}
}
