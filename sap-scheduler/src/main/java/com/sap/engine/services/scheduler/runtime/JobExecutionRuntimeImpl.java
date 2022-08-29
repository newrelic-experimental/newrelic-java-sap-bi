package com.sap.engine.services.scheduler.runtime;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.scheduler.api.SchedulerTaskID;
import com.sap.scheduler.runtime.Job;
import com.sap.scheduler.runtime.JobDefinitionID;
import com.sap.scheduler.runtime.JobID;
import com.sap.scheduler.runtime.JobParameter;
import com.sap.scheduler.runtime.SchedulerID;

@Weave
public abstract class JobExecutionRuntimeImpl {

	@Trace
	private JobID executeJobForAllMethods(JobDefinitionID jobDefId, JobParameter[] jobParameters,
			Integer retentionPeriod, JobID parentId, SchedulerID schedulerId, String runAsUser,
			SchedulerTaskID schedTaskId, String vendorData, JobID jobId) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","Scheduler","JobExecutionRuntimeImpl","executeJob");
		Job job = getJob(jobId);
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("Job-Name", job.getName());
		attributes.put("Job-Node", job.getNode());
		attributes.put("Job-ID", job.getId());
		traced.addCustomAttributes(attributes);
		return Weaver.callOriginal();
	}
	
	public abstract Job getJob(JobID jobid);
}
