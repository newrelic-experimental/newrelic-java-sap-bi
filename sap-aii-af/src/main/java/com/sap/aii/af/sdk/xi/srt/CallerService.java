package com.sap.aii.af.sdk.xi.srt;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.sdk.xi.mo.MessageContext;
import com.sap.aii.af.sdk.xi.util.URI;

@Weave(type = MatchType.Interface)
public abstract class CallerService {

	@Trace
	public MessageContext call(MessageContext var1, URI var2) {
		
		return Weaver.callOriginal();
	}
}
