package com.sap.aii.af.service.resource;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adaptermonitoring.AdapterUtils;
import com.newrelic.instrumentation.labs.sap.adaptermonitoring.NRRunnable;

@Weave(type=MatchType.Interface)
public abstract class SAPAdapterResources {

	/*
	 * 
	 * Wraps the runnable with a New Relic class and token so that
	 * we can link it back to the original transaction
	 */
	public void startRunnable(Runnable r) {
		NRRunnable wrapper = AdapterUtils.getWrapper(r);
		if(wrapper != null) {
			r = wrapper;
		}
		Weaver.callOriginal();
	}
}
