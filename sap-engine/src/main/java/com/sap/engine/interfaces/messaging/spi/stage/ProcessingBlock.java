package com.sap.engine.interfaces.messaging.spi.stage;

import java.util.HashMap;
import java.util.Map;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.engine.EngineUtils;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.spi.Services;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;



@Weave(type=MatchType.Interface)
public abstract class ProcessingBlock {

	@Trace
	public TransportableMessage[] process(Services services, TransportableMessage message, Map<String, Object> var3) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		
		traced.setMetricName("Custom","ProcessingBlock",getClass().getSimpleName(),getName(),"process");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		traced.addCustomAttributes(attributes);
		return Weaver.callOriginal();
	}

	public abstract String getName();
}
