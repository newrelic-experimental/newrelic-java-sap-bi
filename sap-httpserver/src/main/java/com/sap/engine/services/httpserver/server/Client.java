package com.sap.engine.services.httpserver.server;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public abstract class Client {

	@Trace
	public void send(final byte[] msg, final int off, final int len) {
		Weaver.callOriginal();
	}

	@Trace
	public void send(final byte[] msg, final int off, final int len, final byte connectionFlag) {
		Weaver.callOriginal();
	}
}
