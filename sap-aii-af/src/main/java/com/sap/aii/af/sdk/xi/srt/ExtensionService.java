package com.sap.aii.af.sdk.xi.srt;

import java.util.Hashtable;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.sdk.xi.mo.MessageContext;

@SuppressWarnings("rawtypes")
@Weave(type = MatchType.Interface)
public abstract class ExtensionService {

	@Trace
	public void invokeOnRequest(MessageContext var1, Hashtable var2) {
		Weaver.callOriginal();
	}

	@Trace
	public void invokeOnResponse(MessageContext var1, Hashtable var2) {
		Weaver.callOriginal();
	}
}
