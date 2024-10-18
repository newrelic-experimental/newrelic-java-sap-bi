package com.sap.engine.interfaces.messaging.api;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.engine.EngineLogger;
import com.newrelic.instrumentation.labs.sap.engine.EngineUtils;
import com.newrelic.instrumentation.labs.sap.engine.HeadersUtil;

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
		if(!EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(true);
			HeadersUtil.addHeaders(message);
			HeadersUtil.dumpHeaders(message);
		}
		Weaver.callOriginal();
		if(EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(false);
		}
	}

	@Trace(dispatcher=true)
	public void send(Message message, MessageProcessingFeatures var2) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"send");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		if(!EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(true);
			HeadersUtil.addHeaders(message);
			HeadersUtil.dumpHeaders(message);
		}
		Weaver.callOriginal();
		if(EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(false);
		}
	}

	@Trace(dispatcher=true)
	public void send(Message message, boolean var2) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"send");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		if(!EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(true);
			HeadersUtil.addHeaders(message);
			HeadersUtil.dumpHeaders(message);
		}
		Weaver.callOriginal();
		if(EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(false);
		}
	}

	@Trace(dispatcher=true)
	public void send(Message message, long var2) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"send");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		if(!EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(true);
			HeadersUtil.addHeaders(message);
			HeadersUtil.dumpHeaders(message);
		}
		Weaver.callOriginal();
		if(EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(false);
		}
	}

	@Trace(dispatcher=true)
	public void send(Message message, long var2, boolean var4) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"send");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		if(!EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(true);
			HeadersUtil.addHeaders(message);
			HeadersUtil.dumpHeaders(message);
		}
		Weaver.callOriginal();
		if(EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(false);
		}
	}

	@Trace(dispatcher=true)
	public Message call(Message message, long var2) {
		EngineLogger.logMessage(message, getClass().getName() + ".call, name = " + getName());
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"call");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		if(!EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(true);
			HeadersUtil.addHeaders(message);
			HeadersUtil.dumpHeaders(message);
		}
		Message reply = Weaver.callOriginal();
		if(EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(false);
		}
		return reply;
	}

	@Trace(dispatcher=true)
	public Message call(Message message, long var2, MessageProcessingFeatures var4) {
		EngineLogger.logMessage(message, getClass().getName() + ".call, name = " + getName());
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"call");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		if(!EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(true);
			HeadersUtil.addHeaders(message);
			HeadersUtil.dumpHeaders(message);
		}
		Message reply = Weaver.callOriginal();
		return reply;
	}

	@Trace(dispatcher=true)
	public Message call(Message message, long var2, boolean var4) {
		EngineLogger.logMessage(message, getClass().getName() + ".call, name = " + getName());
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Connection",getClass().getSimpleName(),getName(),"call");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		EngineUtils.addMessage(attributes, message);
		MessageKey msgKey = message.getMessageKey();
		EngineUtils.addMessageKey(attributes, msgKey);
		if(!EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(true);
			HeadersUtil.addHeaders(message);
			HeadersUtil.dumpHeaders(message);
		}
		Message reply = Weaver.callOriginal();
		if(EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(false);
		}
		return reply;
	}

}
