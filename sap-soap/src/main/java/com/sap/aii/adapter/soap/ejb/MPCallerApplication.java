package com.sap.aii.adapter.soap.ejb;


import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.sdk.xi.mo.MessageContext;

@Weave
public abstract class MPCallerApplication {

	@Trace(dispatcher=true)
	public MessageContext perform(MessageContext inmc) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","SOAP","MPCallerApplication","perform");
		return Weaver.callOriginal();
	}
	
	@Trace
	ModuleData callModuleProcessor(ModuleData inputModuleData, String midstr) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","SOAP","MPCallerApplication","callModuleProcessor");
		return Weaver.callOriginal();
	}
}
