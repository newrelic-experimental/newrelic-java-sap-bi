package com.sap.lcr.cimsrv.requests;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.lcr.cimsrv.CIMOMRequestHandler;
import com.sap.sld.api.wbem.cim.CIMElementList;
import com.sap.sld.api.wbem.cim.CIMMessage.CIMRequest;
import com.sap.sld.api.wbem.client.WBEMOperation.ResultType;

@Weave(type=MatchType.BaseClass)
public abstract class AbstractRequestExecutor {
	
	public abstract String getWorkerKey();
	public abstract ResultType getResultType();

	@Trace
	public CIMElementList<?> execute(CIMOMRequestHandler var1, CIMRequest var2) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","CIM","RequestExecutor",getClass().getSimpleName());
		traced.addCustomAttribute("Operation-Name", getWorkerKey());
		traced.addCustomAttribute("Operation-ReturnType", getResultType().name());
		return Weaver.callOriginal();
	}
}
