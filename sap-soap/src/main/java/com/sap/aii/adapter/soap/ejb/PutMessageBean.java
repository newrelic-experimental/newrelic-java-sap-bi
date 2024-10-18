package com.sap.aii.adapter.soap.ejb;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.soap.SOAPAdapterLogger;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;

@Weave
public abstract class PutMessageBean {

	@Trace(dispatcher=true)
	public ModuleData process(final ModuleContext moduleContext, final ModuleData inputModuleData) {
		SOAPAdapterLogger.logModuleContext(moduleContext, "com.sap.aii.adapter.soap.ejb.PutMessageBean.process");
		SOAPAdapterLogger.logModuleData(inputModuleData, "com.sap.aii.adapter.soap.ejb.PutMessageBean.process");
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","SOAP","PutMessageBean","process");
		return Weaver.callOriginal();
	}
}
