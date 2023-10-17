package com.sap.aii.af.service.api;

import java.io.Serializable;
import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adaptermonitoring.AdapterUtils;

@Weave
public abstract class AdapterAccess {

	@Trace
	public Payload call(Payload payload, String msgFromService, String msgInterface, String msgInterfaceNamespace) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		
		traced.setMetricName("Custom","SAP","AdapterAccess","call",msgFromService,msgInterface);
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		AdapterUtils.addValue(attributes, "MessageFromService", msgFromService);
		AdapterUtils.addValue(attributes, "MessageInterface", msgInterface);
		AdapterUtils.addValue(attributes, "MessageInterfaceNamespace", msgInterfaceNamespace);
		traced.addCustomAttributes(attributes);
		return Weaver.callOriginal();
	}
	
	@Trace
	public Serializable execute(Payload payload, String msgFromService, String msgInterface, String msgInterfaceNamespace) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","AdapterAccess","execute",msgFromService,msgInterface);
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		AdapterUtils.addValue(attributes, "MessageFromService", msgFromService);
		AdapterUtils.addValue(attributes, "MessageInterface", msgInterface);
		AdapterUtils.addValue(attributes, "MessageInterfaceNamespace", msgInterfaceNamespace);
		traced.addCustomAttributes(attributes);
		return Weaver.callOriginal();
	}
	
	@Trace
	public void send(Payload payload, String msgFromService, String msgInterface, String msgInterfaceNamespace) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","AdapterAccess","send",msgFromService,msgInterface);
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		AdapterUtils.addValue(attributes, "MessageFromService", msgFromService);
		AdapterUtils.addValue(attributes, "MessageInterface", msgInterface);
		AdapterUtils.addValue(attributes, "MessageInterfaceNamespace", msgInterfaceNamespace);
		traced.addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
	
	
}
