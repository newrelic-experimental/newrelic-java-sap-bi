package com.sap.aii.adapter.rest.ejb.sender;

import java.util.HashMap;
import java.util.Map;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.TransactionNamePriority;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public abstract class RESTSenderChannel {
	
	public abstract String getEndpoint();

	
	public HTTPResult service(String method, String requestURL, String path, byte[] data, Map<String, String> headers, String query, Map<String, String> returnHeaders) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("Method", method);
		attributes.put("Path", path);
		attributes.put("RequestURL", requestURL);
		attributes.put("Query", query);
		String endpoint = getEndpoint();
		attributes.put("Endpoint", endpoint);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.addCustomAttributes(attributes);
		String[] names = endpoint != null && !endpoint.isEmpty() ? new String[] {"Custom","SAP","REST","RESTSenderChannel","service",endpoint} : new String[] {"Custom","SAP","REST","RESTSenderChannel","service"};
		traced.setMetricName(names);
		if(endpoint != null && !endpoint.isEmpty()) {
			NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.CUSTOM_LOW, false, "SAP-Rest", "SAP","Rest",endpoint);
		}
		return Weaver.callOriginal();
	}
	
	@Weave
	public static class HTTPResult {
		
	}
}
