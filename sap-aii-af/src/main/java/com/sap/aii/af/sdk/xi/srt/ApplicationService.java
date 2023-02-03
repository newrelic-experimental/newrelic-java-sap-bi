package com.sap.aii.af.sdk.xi.srt;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.sdk.xi.mo.MessageContext;

@Weave(type = MatchType.Interface)
public abstract class ApplicationService {

	@Trace
	public MessageContext perform(MessageContext var1) {
		return Weaver.callOriginal();
	}
}
