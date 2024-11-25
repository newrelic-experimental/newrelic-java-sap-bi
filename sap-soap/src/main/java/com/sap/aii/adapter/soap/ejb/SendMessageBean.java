package com.sap.aii.adapter.soap.ejb;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.soap.DataUtils;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;

@Weave
public abstract class SendMessageBean {

	@Trace(dispatcher=true)
	public ModuleData process(ModuleContext moduleContext, ModuleData inputModuleData) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","SOAP","SendMessageBean","process");
		HashMap<String, Object> attributes = new HashMap<String, Object>();

		DataUtils.addContext(moduleContext);
		boolean added = DataUtils.addAttributes(moduleContext, attributes);
		if(added) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		return Weaver.callOriginal();
	}
}
