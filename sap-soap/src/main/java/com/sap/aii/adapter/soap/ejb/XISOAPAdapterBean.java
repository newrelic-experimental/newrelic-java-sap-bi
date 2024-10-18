package com.sap.aii.adapter.soap.ejb;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.soap.SOAPAdapterLogger;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;

@Weave
public abstract class XISOAPAdapterBean {

	@Trace(dispatcher=true)
	public ModuleData process(ModuleContext moduleContext, ModuleData inputModuleData) {
		SOAPAdapterLogger.logModuleContext(moduleContext, "com.sap.aii.adapter.soap.ejb.XISOAPAdapterBean.process");
		SOAPAdapterLogger.logModuleData(inputModuleData, "com.sap.aii.adapter.soap.ejb.XISOAPAdapterBean.process");
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","SOAP","XISOAPAdapterBean","process");
		return Weaver.callOriginal();
	}
}
