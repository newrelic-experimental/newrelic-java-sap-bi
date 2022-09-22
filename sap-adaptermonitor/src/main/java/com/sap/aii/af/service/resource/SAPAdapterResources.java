package com.sap.aii.af.service.resource;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.adaptermonitoring.NRRunnable;

@Weave(type=MatchType.Interface)
public abstract class SAPAdapterResources {

	/*
	 * 
	 * Wraps the runnable with a New Relic class and token so that
	 * we can link it back to the original transaction
	 */
	public void startRunnable(Runnable r) {
		Token token = NewRelic.getAgent().getTransaction().getToken();
		if(token != null && token.isActive()) {
			NRRunnable wrapper = new NRRunnable(r, token);
			r = wrapper;
		} else if(token != null) {
			token.expire();
			token = null;
		}
		Weaver.callOriginal();
	}
}
