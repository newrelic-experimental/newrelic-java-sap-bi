package com.sap.aii.af.lib.mp.module;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adapters.AdapterModuleLogger;

@Weave(type=MatchType.Interface)
public abstract class Module {

	@Trace
	public ModuleData process(ModuleContext context, ModuleData moduleData) throws ModuleException {
		String classname = getClass().getName();
		String source = classname + ".process";
		AdapterModuleLogger.logModuleContext(context, source);
		AdapterModuleLogger.logModuleData(moduleData,  source);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","Module",getClass().getSimpleName(),"process");
		ModuleData returnModuleData = Weaver.callOriginal();
		return returnModuleData;
	}
}
