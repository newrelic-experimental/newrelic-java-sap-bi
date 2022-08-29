package com.sap.aii.adapter.soap.ejb;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;

@Weave
public abstract class PutMessageBean {

	@Trace(dispatcher=true)
	public ModuleData process(final ModuleContext moduleContext, final ModuleData inputModuleData) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","SOAP","PutMessageBean","process");
		return Weaver.callOriginal();
	}
}
