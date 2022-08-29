package com.sap.engine.services.webservices.espbase.server.runtime;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public abstract class OnewaySyncProcessor {

	@Trace
	public boolean process() {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","WebServices","OnewayProcessor",getClass().getSimpleName(),"process");
		return Weaver.callOriginal();
	}
}
