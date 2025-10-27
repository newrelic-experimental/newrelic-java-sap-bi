package com.sap.aii.af.lib.mp.processor;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adapters.AttributeLoggingUtils;
import com.newrelic.instrumentation.labs.sap.adapters.DataUtils;
import com.sap.aii.af.lib.mp.module.ModuleData;

@Weave(type = MatchType.Interface)
public abstract class ModuleProcessor {

	@Trace(dispatcher = true)
	public ModuleData process(String key, ModuleData objectData) {
		DataUtils.addData(objectData);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","ModuleProcessor",getClass().getSimpleName(),"process");
		HashMap<String, Object> span_attributes = new HashMap<String, Object>();
		DataUtils.addAttributes(objectData, span_attributes);
		traced.addCustomAttributes(span_attributes);

		AttributeLoggingUtils.logAdapterDetails("Enter", null, objectData);
		ModuleData result = Weaver.callOriginal();
		AttributeLoggingUtils.logAdapterDetails("Exit", null, result);
		
		return result;
	}
}
