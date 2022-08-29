package com.sap.aii.af.service.event.impl.worker.sync;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.service.event.api.Event;

@Weave(type=MatchType.Interface)
public abstract class SyncWorker {

	@Trace(dispatcher=true)
	public Event[] work() {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","SyncWorker",getClass().getSimpleName(),"work");
		return Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	public Event[] startWork() {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","SyncWorker",getClass().getSimpleName(),"startWork");
		return Weaver.callOriginal();
	}

}
