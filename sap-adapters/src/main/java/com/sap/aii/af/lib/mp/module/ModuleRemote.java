package com.sap.aii.af.lib.mp.module;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adapters.DataUtils;

@Weave(type=MatchType.Interface)
public abstract class ModuleRemote {

	@Trace
	public ModuleData process(ModuleContext context, ModuleData moduleData) throws ModuleException {
		DataUtils.addContext(context);
		DataUtils.addData(moduleData);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","ModuleRemote",getClass().getSimpleName(),"process");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		DataUtils.addAttributes(context, attributes);
		DataUtils.addAttributes(moduleData, attributes);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		ModuleData returnModuleData = Weaver.callOriginal();
		return returnModuleData;
	}
}
