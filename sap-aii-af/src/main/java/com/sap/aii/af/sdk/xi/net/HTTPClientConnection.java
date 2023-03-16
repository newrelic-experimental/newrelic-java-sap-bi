package com.sap.aii.af.sdk.xi.net;

import com.newrelic.api.agent.HttpParameters;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.sap.af.SAPAFUtils;
import com.sap.aii.af.sdk.xi.util.URI;

@Weave
public abstract class HTTPClientConnection {
	
	@NewField
	java.net.URI uriToUse;

	public HTTPClientConnection(URI url) {
		uriToUse = SAPAFUtils.convert(url);
	}
	
	@Trace
	protected synchronized Object call(Object obj) {
		HttpParameters params = HttpParameters.library("SAP-AF").uri(uriToUse).procedure("call").noInboundHeaders().build();
		NewRelic.getAgent().getTracedMethod().reportAsExternal(params);
		return Weaver.callOriginal();
	}
}
