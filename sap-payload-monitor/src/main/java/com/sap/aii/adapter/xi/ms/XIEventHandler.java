package com.sap.aii.adapter.xi.ms;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.payload.PayloadProcessor;
import com.sap.engine.interfaces.messaging.api.Action;
import com.sap.engine.interfaces.messaging.api.Party;
import com.sap.engine.interfaces.messaging.api.Service;
import com.sap.engine.interfaces.messaging.spi.Services;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;
import com.sap.engine.interfaces.messaging.spi.transport.Credential;
import com.sap.engine.interfaces.messaging.spi.transport.Endpoint;
import com.sap.engine.interfaces.messaging.spi.transport.TransportPackage;

@Weave
public class XIEventHandler {
	
	@Trace
	public TransportableMessage onCall(Services services, TransportableMessage message) {
		PayloadProcessor.processMessage(message, " on input to onCall");
		TransportableMessage result =  Weaver.callOriginal();
		PayloadProcessor.processMessage(result, " on output to onCall");
		return result;
	}
	
	@Trace
	public TransportableMessage onCreateMessage(Party fromParty, Party toParty, Service fromService, Service toService, Action action) {
		TransportableMessage result =  Weaver.callOriginal();
		PayloadProcessor.processMessage(result, " on output to onCreateMessage");
		return result;
	}
	
	@Trace
	public TransportableMessage onCreateMessage(TransportPackage pkg, Endpoint endpoint, Credential credential) {
		TransportableMessage result =  Weaver.callOriginal();
		PayloadProcessor.processMessage(result, " on output to onCreateMessage");
		return result;
	}
	
	@Trace
	public TransportableMessage onCreateMessage(Party fromParty, Party toParty, Service fromService, Service toService, Action action, String messageId) {
		TransportableMessage result =  Weaver.callOriginal();
		PayloadProcessor.processMessage(result, " on output to onCreateMessage");
		return result;
	}
	
	@Trace
	public void onDeliver(Services serv, TransportableMessage m, long timeOfLastDelivery, int deliveryCounter) {
		PayloadProcessor.processMessage(m, " on input to onDeliver");
		Weaver.callOriginal();
	}
	
	@Trace
	public TransportableMessage onRequest(Services services, TransportableMessage message)  {
		PayloadProcessor.processMessage(message, " on input to onRequest");
		TransportableMessage result =  Weaver.callOriginal();
		PayloadProcessor.processMessage(result, " on output to onRequest");
		return result;
	}
	
	@Trace
	public void onSend(Services services, TransportableMessage message) {
		PayloadProcessor.processMessage(message, " on input to onSend");
		Weaver.callOriginal();
	}
	
	@Trace
	public void onTransmit(Services services, TransportableMessage tMessage) {
		PayloadProcessor.processMessage(tMessage, " on input to onTransmit");
		Weaver.callOriginal();
	}
}
