package com.newrelic.instrumentation.labs.sap.engine;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import com.newrelic.api.agent.Logger;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Transaction;
import com.newrelic.api.agent.TransportType;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.exception.MessageFormatException;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;
import com.sap.engine.interfaces.messaging.spi.transport.TransportHeaders;

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

	public static void dumpHeaders(Message message) {
		if(message == null) return;
		
		Logger logger = NewRelic.getAgent().getLogger();
		
		if(message instanceof TransportableMessage) {
			try {
				TransportableMessage tMessage = (TransportableMessage)message;
				TransportHeaders tHeaders = tMessage.getTransportHeaders();
				if(tHeaders != null) {
					logger.log(Level.FINE, "Transportable Message Headers");
					Map<?,?> map = tHeaders.toMap();
					for(Object key : map.keySet()) {
						logger.log(Level.FINE, "\tKey: {0}, Value: {1}", key, map.get(key));
					}
				} else {
					logger.log(Level.FINE, "Transportable Message Headers are null");
				}
			} catch (MessageFormatException e) {
				logger.log(Level.FINE, e, "Failed to retrieve headers from message: {0}", message);
			}
		} else {
			NRMessageHeaders headers = new NRMessageHeaders();
			logger.log(Level.FINE, "Message is not TransportableMessage, it is {0}", message);
			logger.log(Level.FINE, "Dumping NR related message properties");
			headers.loadInboundHeaders(message);
			Collection<String> headerNames = headers.getHeaderNames();
			if(headerNames == null || headerNames.isEmpty()) {
				logger.log(Level.FINE, "No headers were found");
			} else {
				for(String name : headerNames) {
					String value = headers.getHeader(name);
					logger.log(Level.FINE, "\tKey: {0}, Value: {1}", name,value);
				}
			}
			
		}
	}

}
