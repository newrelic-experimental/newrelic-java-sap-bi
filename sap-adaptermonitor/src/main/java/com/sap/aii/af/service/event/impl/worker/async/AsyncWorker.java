package com.sap.aii.af.service.event.impl.worker.async;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type=MatchType.Interface)
public abstract class AsyncWorker {

	@Trace(dispatcher=true)
	public void work() {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","AsyncWorker",getClass().getSimpleName(),"work");
		Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public void startWork() {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","AsyncWorker",getClass().getSimpleName(),"startWork");
		Weaver.callOriginal();
	}

}
