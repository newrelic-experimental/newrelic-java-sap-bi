package com.sap.aii.adapter.soap.ejb;

import java.util.Hashtable;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.sdk.xi.mo.MessageContext;

@SuppressWarnings("rawtypes")
@Weave
public abstract class SMIMEExtension {

	@Trace(dispatcher=true)
	public void invokeOnRequest(MessageContext inmc, Hashtable extcntxt) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","SOAP","SMIMEExtension","invokeOnRequest");
		Weaver.callOriginal();
	}
	
	@Trace(dispatcher=true)
	public void invokeOnResponse(MessageContext inmc, Hashtable extcntxt) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","SOAP","SMIMEExtension","invokeOnResponse");
		Weaver.callOriginal();
	}
	
}
