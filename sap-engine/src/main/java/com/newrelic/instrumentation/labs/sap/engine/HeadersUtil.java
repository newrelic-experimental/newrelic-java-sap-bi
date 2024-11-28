package com.newrelic.instrumentation.labs.sap.engine;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Transaction;
import com.newrelic.api.agent.TransportType;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;

public class HeadersUtil {
	
	public static void addHeaders(Message message) {
		Transaction transaction = NewRelic.getAgent().getTransaction();
		
		if(message instanceof TransportableMessage) {
			TransportableMessage tMessage = (TransportableMessage)message;
			NRTransportMessageHeaders headers = new NRTransportMessageHeaders(tMessage);
			transaction.insertDistributedTraceHeaders(headers);
		} else {
			NRMessageHeaders headers = new NRMessageHeaders();
			transaction.insertDistributedTraceHeaders(headers);
			headers.populateOutboundHeaders(message);
		}
	}
	
	public static void getHeaders(Message message) {

		Transaction transaction = NewRelic.getAgent().getTransaction();
		
		if(message instanceof TransportableMessage) {
			TransportableMessage tMessage = (TransportableMessage)message;
			NRTransportMessageHeaders headers = new NRTransportMessageHeaders(tMessage);
			transaction.acceptDistributedTraceHeaders(TransportType.Other, headers);
		} else {
			NRMessageHeaders headers = new NRMessageHeaders();
			headers.loadInboundHeaders(message);
			transaction.acceptDistributedTraceHeaders(TransportType.Other, headers);
		}

	}


}
