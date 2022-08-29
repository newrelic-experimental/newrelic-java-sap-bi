package com.sap.scheduler.runtime;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.scheduler.api.SchedulerTaskID;
import com.sap.scheduler.runtime.JobDefinitionID;
import com.sap.scheduler.runtime.JobID;
import com.sap.scheduler.runtime.JobParameter;
import com.sap.scheduler.runtime.SchedulerID;

@Weave(type=MatchType.Interface)
public abstract class JobExecutor {

	@Trace
	public JobID executeJob(JobDefinitionID jobDefId, JobParameter[] jobParameters, Integer retentionPeriod,
			JobID parentId, SchedulerID schedulerId, String runAsUser, SchedulerTaskID schedTaskID, String vendorData,
			JobID jobId) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","Scheduler","JobExecutor",getClass().getSimpleName(),"executeJob");
		return Weaver.callOriginal();
	}
	
	public JobID executeJob(JobDefinitionID jobDefId, JobParameter[] jobParameters, Integer retentionPeriod,
			JobID parentId, SchedulerID schedulerId, String user, SchedulerTaskID schedTaskID, String vendorData) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","Scheduler","JobExecutor",getClass().getSimpleName(),"executeJob");
		return Weaver.callOriginal();
	}
}
