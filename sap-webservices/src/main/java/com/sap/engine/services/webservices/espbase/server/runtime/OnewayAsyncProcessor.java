package com.sap.engine.services.webservices.espbase.server.runtime;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.engine.frame.ApplicationServiceContext;

@Weave
public abstract class OnewayAsyncProcessor {
	
	@NewField
	private Token token = null;
	
	public OnewayAsyncProcessor(RuntimeProcessingEnvironment rpe, ProviderContextHelperImpl providerContext,
			ApplicationServiceContext  appServiceContext) {
		
	}

	@Trace
	public boolean process() {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","WebServices","OnewayProcessor",getClass().getSimpleName(),"process");
		Token t = NewRelic.getAgent().getTransaction().getToken();
		if(t != null && t.isActive()) {
			token = t;
		} else if(t != null) {
			t.expire();
			t = null;
		}
		return Weaver.callOriginal();
	}
	
	@Trace(async=true)
	public void run() {
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		Weaver.callOriginal();
	}
}
