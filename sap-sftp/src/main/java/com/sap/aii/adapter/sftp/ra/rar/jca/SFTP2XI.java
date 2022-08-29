package com.sap.aii.adapter.sftp.ra.rar.jca;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public abstract class SFTP2XI {

	@Trace(dispatcher=true)
	public void invoke() {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","SFTP","SFTP2XI","invoke");
		Weaver.callOriginal();
	}
}
