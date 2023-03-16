package com.sap.engine.services.ejb3.runtime.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.sap.ejb3.Utils;
import com.sap.engine.services.ejb3.runtime.InstanceIdentity;

@Weave(type = MatchType.BaseClass)
public abstract class AbstractInvocationContext {
	
	public abstract Method getMethod();
	public abstract InstanceIdentity getInstanceIdentity();
	
	@Trace
	protected Object proceedFinal() {
		Method m = getMethod();
		if(m != null) {
			String className = m.getDeclaringClass().getName();
			String methodName = m.getName();
			NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","EJB3",getClass().getSimpleName(),"proceedFinal",className,methodName);
		}
		Map<String, Object> attributes = new HashMap<>();
		Utils.addInstanceIdentity(attributes, getInstanceIdentity());
		if(!attributes.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		}
		return Weaver.callOriginal();
	}

}
