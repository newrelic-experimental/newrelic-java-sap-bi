package com.sap.aii.af.lib.mp.module;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.AttributeProcessor;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.MessageMonitor;

@Weave(type = MatchType.Interface)
public abstract class Module {

	public ModuleData process(ModuleContext moduleContext, ModuleData moduleData) {
		if(!MessageMonitor.initialized) {
			MessageMonitor.initialize();
		}
		AttributeProcessor.record(moduleContext, moduleData);
		return Weaver.callOriginal();
	}
}
