package com.sap.lcr.cimsrv;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.cim.lib.objmgr.core.IsolationLevel;
import com.sap.sld.api.wbem.cim.CIMMessage;

@Weave
public abstract class CIMOMRequestHandler {

	@Trace
	CIMMessage executeRequestMessage(CIMMessage requestMessage, boolean asTransaction, IsolationLevel isolationLevel) {
		return Weaver.callOriginal();
	}
	
	@Trace
	private CIMMessage.CIMResponse executeRequest(final CIMMessage.CIMRequest request) {
		String requestName = request.getName();
		if(requestName != null && !requestName.isEmpty()) {
			NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","CIM","CIMOMRequestHandler","executeRequest",requestName);
		} else {
			NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","CIM","CIMOMRequestHandler","executeRequest");
		}
		return Weaver.callOriginal();
	}
}
