package com.sap.aii.af.lib.mp.processor;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.lib.mp.module.ModuleData;

@Weave(type=MatchType.Interface)
public abstract class ModuleProcessorLocal {

	@Trace
	public ModuleData process(String objectKey, ModuleData inputModuleDat) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","ModuleProcessorLocal",getClass().getSimpleName(),"process");
		traced.addCustomAttribute("ObjectKey", objectKey);
		return Weaver.callOriginal();
	}
}
