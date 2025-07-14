package com.sap.aii.af.lib.util.dispatch;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.MessageMonitor;

@Weave(type = MatchType.Interface)
public abstract class DispatchingService {

	@Trace
	public String send(byte[] var1) {
		if(!MessageMonitor.initialized) {
			MessageMonitor.initialize();
		}
		return Weaver.callOriginal();
	}
}
