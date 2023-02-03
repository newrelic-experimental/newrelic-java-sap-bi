package com.sap.aii.adapter.rest.ejb.common.channel;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.rest.RESTUtils;
import com.sap.aii.adapter.xi.ms.XIMessage;

@Weave(type=MatchType.BaseClass)
public abstract class AbstractReceiverChannel {

	@Trace(dispatcher=true)
	public XIMessage receive(XIMessage message) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","REST","AbstractReceiverChannel",getClass().getSimpleName(),"receive");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		RESTUtils.addAction(attributes, message.getAction());
		RESTUtils.addMessageKey(attributes, message.getMessageKey());
		RESTUtils.addValue(attributes, "CorrelationId", message.getCorrelationId());
		RESTUtils.addValue(attributes, "InterfaceName", message.getInterfaceName());
		RESTUtils.addValue(attributes, "CorrelationId", message.getProtocol());
		RESTUtils.addParty(attributes, message.getFromParty(), "From");
		RESTUtils.addParty(attributes, message.getToParty(), "To");
		RESTUtils.addService(attributes, message.getFromService(), "From");
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		long startTime = System.currentTimeMillis();
		RESTUtils.addService(attributes, message.getToService(), "To");
		XIMessage resultMsg = Weaver.callOriginal();
		long endTime = System.currentTimeMillis();
		RESTUtils.reportMessage(message, endTime-startTime);
		return resultMsg;
	}
	
	@Trace
	protected void channel_receiveAsync(XIMessage p0) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","REST","AbstractReceiverChannel",getClass().getSimpleName(),"channel_receiveAsync");
		Weaver.callOriginal();
	}

	@Trace
	protected XIMessage channel_receiveSync(XIMessage p0) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","REST","AbstractReceiverChannel",getClass().getSimpleName(),"channel_receiveSync");
		return Weaver.callOriginal();
	}

}
