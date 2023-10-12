package com.sap.engine.interfaces.webservices.esp;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.webservices.Utils;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;

@Weave(type=MatchType.Interface)
public abstract class ImplementationContainer {

	@SuppressWarnings("rawtypes")
	@Trace
	public Object invokeMethod(String methodName, Class[] parameterClasses, Object[] parameters, ConfigurationContext ctx) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","WebServices","ImplementationContainer","invokeMethod");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		Utils.addValue(attributes, "MethodName", methodName);
		if(ctx != null) {
			Utils.addValue(attributes, "ConfigContext-Name", ctx.getName());
			Utils.addValue(attributes, "ConfigContext-Path", ctx.getPath());
		}
		traced.addCustomAttributes(attributes);
		return Weaver.callOriginal();
	}
}
