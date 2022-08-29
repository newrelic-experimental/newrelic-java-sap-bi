package com.sap.aii.af.service.api;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.adaptermonitoring.AdapterUtils;
import com.sap.engine.interfaces.messaging.api.Connection;

@Weave
public abstract class XIAccess {
	
	private Connection conn = Weaver.callOriginal();

	@Trace
	public Payload call(Payload payload, String msgToParty, String msgFromParty, String msgToService,
			String msgFromService, String msgInterface, String msgInterfaceNamespace) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","XIAccess",conn.getName(),"call");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		AdapterUtils.addValue(attributes, "InterfaceNamespace", msgInterfaceNamespace);
		AdapterUtils.addValue(attributes, "FromParty", msgFromParty);
		AdapterUtils.addValue(attributes, "Interface", msgInterface);
		AdapterUtils.addValue(attributes, "FromService", msgFromService);
		AdapterUtils.addValue(attributes, "ToParty", msgToParty);
		AdapterUtils.addValue(attributes, "ToService", msgToService);
		traced.addCustomAttributes(attributes);
		return Weaver.callOriginal();
	}
	
	@Trace
	public void send(Payload payload, String msgToParty, String msgFromParty, String msgToService,
			String msgFromService, String msgInterface, String msgInterfaceNamespace) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","XIAccess",conn.getName(),"send");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		AdapterUtils.addValue(attributes, "InterfaceNamespace", msgInterfaceNamespace);
		AdapterUtils.addValue(attributes, "FromParty", msgFromParty);
		AdapterUtils.addValue(attributes, "Interface", msgInterface);
		AdapterUtils.addValue(attributes, "FromService", msgFromService);
		AdapterUtils.addValue(attributes, "ToParty", msgToParty);
		AdapterUtils.addValue(attributes, "ToService", msgToService);
		traced.addCustomAttributes(attributes);
		Weaver.callOriginal();	
	}
}
