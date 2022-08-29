package com.sap.engine.services.webservices.espbase.server;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.webservices.Utils;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;

@Weave(type=MatchType.Interface)
public abstract class TransportBinding {

	public Message createFaultMessage(Throwable t, ProviderContextHelper ctxHelper) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		Utils.populateWithProviderContextHelper(attributes, ctxHelper);
		try {
			String action = getAction(ctxHelper);
			Utils.addValue(attributes, "Action", action);
		} catch (RuntimeProcessException e) {
		}
		NewRelic.noticeError(t,attributes);
		return Weaver.callOriginal();
	}

	@Trace
	public void sendResponseMessage(ProviderContextHelper ctxHelper, int var2) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","WebServices","TransportBinding",getClass().getSimpleName(),"sendResponseMessage");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		Utils.populateWithProviderContextHelper(attributes, ctxHelper);
		try {
			String action = getAction(ctxHelper);
			Utils.addValue(attributes, "Action", action);
		} catch (RuntimeProcessException e) {
		}
		traced.addCustomAttributes(attributes);
		Weaver.callOriginal();
	}

	@Trace
	public void sendServerError(Throwable t, ProviderContextHelper ctxHelper) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","WebServices","TransportBinding",getClass().getSimpleName(),"sendServerError");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		Utils.populateWithProviderContextHelper(attributes, ctxHelper);
		try {
			String action = getAction(ctxHelper);
			Utils.addValue(attributes, "Action", action);
		} catch (RuntimeProcessException e) {
		}
		traced.addCustomAttributes(attributes);
		NewRelic.noticeError(t,attributes);
		Weaver.callOriginal();
	}

	@Trace
	public void sendAsynchronousResponse(ProviderContextHelper ctxHelper) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","WebServices","TransportBinding",getClass().getSimpleName(),"sendAsynchronousResponse");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		Utils.populateWithProviderContextHelper(attributes, ctxHelper);
		try {
			String action = getAction(ctxHelper);
			Utils.addValue(attributes, "Action", action);
		} catch (RuntimeProcessException e) {
		}
		traced.addCustomAttributes(attributes);
		Weaver.callOriginal();
	}

	public abstract String getAction(ProviderContextHelper var1) throws RuntimeProcessException;

	public void sendMessageOneWay(String endpointURL, Message msg, String action) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","WebServices","TransportBinding",getClass().getSimpleName(),"sendMessageOneWay");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		Utils.addValue(attributes, "Action", action);
		Utils.addValue(attributes, "EndpointURL", endpointURL);
		traced.addCustomAttributes(attributes);
		Weaver.callOriginal();
	}

}
