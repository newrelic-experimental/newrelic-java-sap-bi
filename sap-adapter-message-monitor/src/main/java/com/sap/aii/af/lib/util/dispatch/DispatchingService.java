package com.sap.aii.af.lib.util.dispatch;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.Interface)
public abstract class DispatchingService {

	@Trace
	public String send(byte[] var1) {
		return Weaver.callOriginal();
	}
}
