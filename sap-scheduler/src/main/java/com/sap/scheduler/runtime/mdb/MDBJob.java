package com.sap.scheduler.runtime.mdb;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.scheduler.runtime.JobContext;

@Weave(type=MatchType.Interface)
public abstract class MDBJob {

	@Trace
	public void onJob(JobContext jobContext) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","Scheduler","JobExecutor",getClass().getSimpleName(),"onJob");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("Job-Name", jobContext.getJob().getName());
		attributes.put("Job-Node", jobContext.getJob().getNode());
		attributes.put("Job-SubmitDate", jobContext.getJob().getSubmitDate());
		traced.addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
}
