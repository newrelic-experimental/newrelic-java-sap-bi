package com.sap.aii.af.lib.mp.module;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type=MatchType.Interface)
public abstract class ModuleLocal {

	@Trace(dispatcher=true)
	public ModuleData process(ModuleContext context, ModuleData moduleData) throws ModuleException {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","ModuleLocal",getClass().getSimpleName(),"process");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
        return Weaver.callOriginal();
	}
}
