package com.sap.lcr.cimsrv.requests;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.lcr.cimsrv.CIMOMRequestHandler;
import com.sap.sld.api.wbem.cim.CIMElementList;
import com.sap.sld.api.wbem.cim.CIMMessage.CIMRequest;

@Weave(type = MatchType.BaseClass)
public abstract class AbstractRequestExecutor {

	@Trace
	public CIMElementList<?> execute(CIMOMRequestHandler var1, CIMRequest request) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","CIM","RequestExecutor",getClass().getSimpleName(),"execute",request.getName());
		return Weaver.callOriginal();
	}
}
