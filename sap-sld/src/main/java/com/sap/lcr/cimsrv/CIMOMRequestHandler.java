package com.sap.lcr.cimsrv;

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
}
