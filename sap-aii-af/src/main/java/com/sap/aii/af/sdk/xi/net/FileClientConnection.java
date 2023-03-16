package com.sap.aii.af.sdk.xi.net;

import com.newrelic.api.agent.GenericParameters;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.sap.af.SAPAFUtils;
import com.sap.aii.af.sdk.xi.util.URI;

@Weave
public abstract class FileClientConnection {
	
	protected URI target = Weaver.callOriginal();

	@Trace(dispatcher = true)
	protected void putObject(Object obj) {
		GenericParameters params = GenericParameters.library("SAP-AF").uri(SAPAFUtils.convert(target)).procedure("putObject").build();
		NewRelic.getAgent().getTracedMethod().reportAsExternal(params);
		Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	protected Object getObject(int type) {
		GenericParameters params = GenericParameters.library("SAP-AF").uri(SAPAFUtils.convert(target)).procedure("getObject").build();
		NewRelic.getAgent().getTracedMethod().reportAsExternal(params);
		return Weaver.callOriginal();
	}
}
