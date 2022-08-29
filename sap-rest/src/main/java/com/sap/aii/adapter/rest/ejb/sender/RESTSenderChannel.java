package com.sap.aii.adapter.rest.ejb.sender;

import java.util.HashMap;
import java.util.Map;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.TracedMethod;
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
		attributes.put("Endpoint", getEndpoint());
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.addCustomAttributes(attributes);
		traced.setMetricName("Custom","SAP","REST","RESTSenderChannel","service");
		return Weaver.callOriginal();
	}
	
	@Weave
	public static class HTTPResult {
		
	}
}
