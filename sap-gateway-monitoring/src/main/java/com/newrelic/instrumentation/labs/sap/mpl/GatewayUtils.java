package com.newrelic.instrumentation.labs.sap.mpl;

import java.util.Date;
import java.util.Map;

import org.apache.camel.Endpoint;

import com.sap.it.op.mpl.MessageProcessingLogPart;
import com.sap.it.op.mpl.TypedMessageProcessingLogKeys;

public class GatewayUtils {

	public static void addAttribute(Map<String, Object> attributes, String key, Object value) {
		if(value != null && attributes != null && key != null && !key.isEmpty()) {
			attributes.put(key, value);
		}
	}
	
	public static void addEndpoint(Map<String,Object> attributes, Endpoint endpoint) {
		
		if(endpoint != null) {
			addAttribute(attributes, "EndpointUri", endpoint.getEndpointUri());
			addAttribute(attributes, "EndpointKey", endpoint.getEndpointKey());
		}
	}
	
	public static void reportMPL(MessageProcessingLogPart logPart) {
		Date startDate = logPart.get(TypedMessageProcessingLogKeys.TK_START);
		Date endDate = logPart.get(TypedMessageProcessingLogKeys.TK_STOP);
		long duration = -1;
		if(endDate != null && startDate != null) {
			duration = endDate.getTime() - startDate.getTime();
		}
		StringBuffer sb = new StringBuffer();
		addKeyValuePair(sb, "Start", startDate);
		addKeyValuePair(sb, "End", endDate);
		addKeyValuePair(sb, "Sender", logPart.get(TypedMessageProcessingLogKeys.TK_SENDER_ID));
		addKeyValuePair(sb, "IntegrationFlow", logPart.get(TypedMessageProcessingLogKeys.TK_CONTEXT_NAME));
		addKeyValuePair(sb, "Status", logPart.get(TypedMessageProcessingLogKeys.TK_OVERALL_STATUS));
		if(duration > 0) {
			addKeyValuePair(sb, "Duration", duration);
		}
		addKeyValuePair(sb, "MessageGuid", logPart.get(TypedMessageProcessingLogKeys.TK_MESSAGE_GUID));
		addKeyValuePair(sb, "CorrelationId", logPart.get(TypedMessageProcessingLogKeys.TK_CORRELATION_ID));
		addKeyValuePair(sb, "TransactionId", logPart.get(TypedMessageProcessingLogKeys.TK_TRANSACTION_ID));
		
		String result = sb.toString();
		if(!result.isEmpty()) {
			GatewayLogger.log(result);
		}
	}
	
	private static void addKeyValuePair(StringBuffer sb, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			sb.append(key);
			sb.append(": ");
			sb.append(value);
			sb.append(", ");
		}
	}
}
