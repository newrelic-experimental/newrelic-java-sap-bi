package com.sap.aii.adapter.as2.ra.jca;

import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public abstract class CCIInteraction {

	@Trace
	public Record execute(InteractionSpec ispec, Record input) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","AS2","CCIInteraction","execute");
		return Weaver.callOriginal();
	}
	
	@Trace
	public boolean execute(InteractionSpec ispec, Record input, Record output) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","AS2","CCIInteraction","execute");
		return Weaver.callOriginal();
	}
}
