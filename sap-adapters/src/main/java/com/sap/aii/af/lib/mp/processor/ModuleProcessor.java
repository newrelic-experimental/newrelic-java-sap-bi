package com.sap.aii.af.lib.mp.processor;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.aii.af.lib.mp.module.ModuleData;

@Weave(type = MatchType.Interface)
public abstract class ModuleProcessor {

	@Trace(dispatcher = true)
	public ModuleData process(String key, ModuleData objectData) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","ModuleProcessor",getClass().getSimpleName(),"process");
		HashMap<String, Object> span_attributes = new HashMap<String, Object>();
		traced.addCustomAttributes(span_attributes);

        return Weaver.callOriginal();
	}
}
