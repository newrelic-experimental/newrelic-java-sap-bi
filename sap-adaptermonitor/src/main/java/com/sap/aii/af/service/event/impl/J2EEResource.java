package com.sap.aii.af.service.event.impl;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.adaptermonitoring.NRRunnable;

@Weave
public abstract class J2EEResource {

	public void startRunnable(Runnable runner, boolean systemLevel) {
		Token token = NewRelic.getAgent().getTransaction().getToken();
		if(token != null && token.isActive()) {
			NRRunnable wrapper = new NRRunnable(runner, token);
			runner = wrapper;
		} else if(token != null) {
			token.expire();
			token = null;
		}
		Weaver.callOriginal();
	}
}
