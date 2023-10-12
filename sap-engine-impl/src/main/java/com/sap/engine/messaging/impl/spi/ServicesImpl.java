package com.sap.engine.messaging.impl.spi;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.engineimpl.EngineUtils;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;
import com.sap.engine.interfaces.messaging.spi.transport.TransportPackage;

@Weave
public abstract class ServicesImpl {

	public abstract String getConnectionName();

	@Trace(dispatcher=true)
	public void send(TransportableMessage message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, message);
		String cName = getConnectionName();
		if(cName != null && !cName.isEmpty()) {
			EngineUtils.recordValue(attributes, "ConnectionName", cName);
		}
		
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","send");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void store(TransportableMessage message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","store");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void trigger(TransportableMessage message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","trigger");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public TransportableMessage call(TransportableMessage message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","call");
		return Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void receive(TransportableMessage message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","receive");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public TransportableMessage request(TransportableMessage message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","request");
		return Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void deliver(Message message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","deliver");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void deliver(MessagingException message) {
		NewRelic.noticeError(message);
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","deliver");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public TransportPackage transmit(TransportableMessage message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","transmit");
		return Weaver.callOriginal();
	}

	public abstract String getCurrentUser();
}
