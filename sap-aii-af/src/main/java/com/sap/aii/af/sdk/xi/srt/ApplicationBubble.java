package com.sap.aii.af.sdk.xi.srt;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.sdk.xi.mo.MessageContext;

@Weave
public abstract class ApplicationBubble {

	@Trace
	public MessageContext onMessage(MessageContext inmc) {
		return Weaver.callOriginal();
	}
}
