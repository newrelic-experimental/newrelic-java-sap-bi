package com.sap.aii.adapter.sftp.ra.rar.jca;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.sftp.SFTPUtils;

import java.util.HashMap;
import java.util.Map;

import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

@Weave
public abstract class CCIInteraction {

	@Trace
	public Record execute(InteractionSpec ispec, Record input) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","SFTP","CCIInteraction","execute");
		Map<String, Object> attributes = new HashMap<String, Object>();
		SFTPUtils.addValue(attributes , "InputRecord", input.getRecordName());
		SFTPUtils.addInteractionSpec(attributes, ispec);
		
		Record result = Weaver.callOriginal();
		SFTPUtils.addValue(attributes , "ReturnedRecord", result.getRecordName());
		traced.addCustomAttributes(attributes);
		return result;
	}
	
	@Trace
	public boolean execute(final InteractionSpec ispec, final Record input, final Record output) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","SFTP","CCIInteraction","execute");
		Map<String, Object> attributes = new HashMap<String, Object>();
		SFTPUtils.addValue(attributes , "InputRecord", input.getRecordName());
		SFTPUtils.addInteractionSpec(attributes, ispec);
		SFTPUtils.addValue(attributes , "OutputRecord", output.getRecordName());
		traced.addCustomAttributes(attributes);
		
		return Weaver.callOriginal();
	}
}
