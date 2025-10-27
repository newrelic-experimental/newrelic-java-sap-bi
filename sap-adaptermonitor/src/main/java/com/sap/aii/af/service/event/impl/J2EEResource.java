package com.sap.aii.af.service.event.impl;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adaptermonitoring.AdapterUtils;
import com.newrelic.instrumentation.labs.sap.adaptermonitoring.NRRunnable;

@Weave
public abstract class J2EEResource {

	public void startRunnable(Runnable runner, boolean systemLevel) {
		NRRunnable wrapper = AdapterUtils.getWrapper(runner);
		if(wrapper != null) {
			runner = wrapper;
		}
		Weaver.callOriginal();
	}
}
