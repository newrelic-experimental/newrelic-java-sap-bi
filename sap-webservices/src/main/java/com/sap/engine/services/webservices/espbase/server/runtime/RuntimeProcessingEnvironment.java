package com.sap.engine.services.webservices.espbase.server.runtime;

import java.util.Hashtable;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.engine.interfaces.webservices.runtime.Transport;
import com.sap.engine.interfaces.webservices.esp.Message;

@Weave
public abstract class RuntimeProcessingEnvironment {

	@SuppressWarnings("rawtypes")
	@Trace
	public boolean process(Transport transport, Hashtable metaData) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","WebServices","RuntimeProcessingEnvironment","process");
		traced.addCustomAttribute("TransportID", transport.getTransportID());
		traced.addCustomAttribute("EntryPointID", transport.getEntryPointID());
		return Weaver.callOriginal();
	}
	
	@Trace
	void process0(ProviderContextHelperImpl context) {
		Weaver.callOriginal();
	}
	
	@Trace
	public void sendMessageOneWay(String endpointURL, Message msg, String action)  {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","WebServices","RuntimeProcessingEnvironment","process");
		traced.addCustomAttribute("EndpointURL", endpointURL);
		traced.addCustomAttribute("Action", action);
		Weaver.callOriginal();
	}
}
