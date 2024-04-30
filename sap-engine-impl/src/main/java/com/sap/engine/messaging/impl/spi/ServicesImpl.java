package com.sap.engine.messaging.impl.spi;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransportType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.engineimpl.EngineUtils;
import com.newrelic.instrumentation.labs.sap.engineimpl.NRTransportHeaders;
import com.newrelic.instrumentation.labs.sap.engineimpl.NRTransportMessageHeaders;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;
import com.sap.engine.interfaces.messaging.spi.transport.TransportPackage;

@Weave
public abstract class ServicesImpl {

	public abstract String getConnectionName();

	@Trace
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
		NRTransportMessageHeaders headers = new NRTransportMessageHeaders(message);
		NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(headers);
		Weaver.callOriginal();
	}

	@Trace
	public void store(TransportableMessage message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","store");
		NRTransportMessageHeaders headers = new NRTransportMessageHeaders(message);
		NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(headers);
		Weaver.callOriginal();
	}

	@Trace
	public void trigger(TransportableMessage message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","trigger");
		NRTransportMessageHeaders headers = new NRTransportMessageHeaders(message);
		NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(headers);
		Weaver.callOriginal();
	}

	@Trace
	public TransportableMessage call(TransportableMessage message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","call");
		NRTransportMessageHeaders headers = new NRTransportMessageHeaders(message);
		NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(headers);
		TransportableMessage resultMessage = Weaver.callOriginal();
		NRTransportMessageHeaders inHeaders = new NRTransportMessageHeaders(resultMessage);
		NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.Other, inHeaders);
		return resultMessage;
	}

	@Trace
	public void receive(TransportableMessage message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","receive");
		NRTransportMessageHeaders inHeaders = new NRTransportMessageHeaders(message);
		NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.Other, inHeaders);
		Weaver.callOriginal();
	}

	@Trace
	public TransportableMessage request(TransportableMessage message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","request");
		NRTransportMessageHeaders headers = new NRTransportMessageHeaders(message);
		NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(headers);
		TransportableMessage resultMessage = Weaver.callOriginal();
		NRTransportMessageHeaders inHeaders = new NRTransportMessageHeaders(resultMessage);
		NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.Other, inHeaders);
		return resultMessage;
	}

	@Trace
	public void deliver(Message message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","deliver");
		Weaver.callOriginal();
	}

	@Trace
	public void deliver(MessagingException message) {
		NewRelic.noticeError(message);
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","deliver");
		Weaver.callOriginal();
		if(EngineUtils.HEADERS_SET.get()) {
			EngineUtils.HEADERS_SET.set(false);
		}
	}

	@Trace(dispatcher=true)
	public TransportPackage transmit(TransportableMessage message) {
		HashMap<String, Object> attributes = new HashMap<>();
		EngineUtils.recordTranportMessage(attributes, message);
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","Services","transmit");
		NRTransportMessageHeaders headers = new NRTransportMessageHeaders(message);
		NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(headers);
		TransportPackage resultPackage =  Weaver.callOriginal();
		NRTransportHeaders inHeaders = new NRTransportHeaders(resultPackage.getHeaders());
		NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.Other, inHeaders);
		return resultPackage;
	}

	public abstract String getCurrentUser();
}
