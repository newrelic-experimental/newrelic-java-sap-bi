package com.sap.engine.interfaces.messaging.api;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.engine.EngineUtils;

@Weave(type=MatchType.Interface)
public abstract class Connection {

	
	public abstract String getName();

	@Trace(dispatcher=true)
	public void send(Message message) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"send");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void send(Message message, MessageProcessingFeatures var2) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"send");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void send(Message message, boolean var2) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"send");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void send(Message message, long var2) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"send");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void send(Message message, long var2, boolean var4) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"send");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public Message call(Message message, long var2) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"call");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		return Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public Message call(Message message, long var2, MessageProcessingFeatures var4) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"call");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		return Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public Message call(Message message, long var2, boolean var4) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"call");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		return Weaver.callOriginal();
	}

}
