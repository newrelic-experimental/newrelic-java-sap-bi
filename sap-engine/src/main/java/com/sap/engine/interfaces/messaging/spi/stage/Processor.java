package com.sap.engine.interfaces.messaging.spi.stage;

import java.util.HashMap;
import java.util.Map;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.engine.EngineUtils;
import com.sap.engine.interfaces.messaging.spi.Services;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;

@Weave(type=MatchType.Interface)
public abstract class Processor {

	@Trace(dispatcher=true)
	public void processMessage(Services services, TransportableMessage message, Map<String, Object> context) {
		
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Processor",getClass().getSimpleName(),"processMessage");
		Map<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
//		Endpoint endpoint = message.getEndpoint();
//		if(endpoint != null) {
//			EngineUtils.addEndpoint(attributes, endpoint);
//		}
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
}
