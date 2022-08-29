package com.sap.aii.adapter.soap.ejb;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;

@Weave
public abstract class XISOAPAdapterBean {

	@Trace(dispatcher=true)
	public ModuleData process(ModuleContext moduleContext, ModuleData inputModuleData) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","SOAP","XISOAPAdapterBean","process");
		return Weaver.callOriginal();
	}
}
