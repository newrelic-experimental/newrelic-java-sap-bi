package com.sap.engine.interfaces.webservices.runtime;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type=MatchType.Interface)
public abstract class ServletDispatcher {

	@Trace
	public void doGet(Object req, Object res, Object servlet) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","WebServices","ServletDispatcher",getClass().getSimpleName(),"doGet");
		Weaver.callOriginal();
	}
	
	@Trace
	public void doHead(Object req, Object res, Object servlet) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","WebServices","ServletDispatcher",getClass().getSimpleName(),"doHead");
		Weaver.callOriginal();
	}
	
	@Trace
	public void doPost(Object req, Object res, Object servlet) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","WebServices","ServletDispatcher",getClass().getSimpleName(),"doPost");
		Weaver.callOriginal();
	}
}
