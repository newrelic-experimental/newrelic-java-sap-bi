package com.sap.engine.interfaces.messaging.api.listener;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.engine.EngineLogger;
import com.newrelic.instrumentation.labs.sap.engine.EngineUtils;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave(type=MatchType.Interface)
public abstract class MessageListener {

	@Trace(dispatcher=true)
	public void onMessage(Message message) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		traced.addCustomAttributes(attributes);
		traced.setMetricName("Custom","SAP","MessageListener",getClass().getSimpleName(),"onMessage");
		EngineLogger.logMessage(message, getClass().getName() + ".onMessage");
		Weaver.callOriginal();
	}
}
