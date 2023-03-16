package com.sap.engine.services.ejb3.runtime.impl;

import java.lang.reflect.Method;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransactionNamePriority;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.BaseClass)
public abstract class DefaultEJBProxyInvocationHandler {

	@Trace(dispatcher = true)
	public Object invoke(Object proxy, Method method, Object[] args, boolean throwCausedByExcOfAppExc) {
		NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.FRAMEWORK_HIGH, false, "EJBInvocation", method.getDeclaringClass().getName(),method.getName());
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","EJBInvocation", method.getDeclaringClass().getName(),method.getName());
		return Weaver.callOriginal();
	}
}
