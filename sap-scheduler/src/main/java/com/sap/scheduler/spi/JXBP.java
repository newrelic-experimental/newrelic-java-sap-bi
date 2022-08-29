package com.sap.scheduler.spi;

import java.rmi.RemoteException;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.scheduler.runtime.Job;
import com.sap.scheduler.runtime.JobDefinitionID;
import com.sap.scheduler.runtime.JobID;

@Weave(type=MatchType.Interface)
public abstract class JXBP {

	@Trace
	public JobID executeJob(JobDefinitionID var1, JobParameterWS[] var2, Integer var3) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Scheduler","JXBP",getClass().getSimpleName(),"executeJob");
		JobID jobId = Weaver.callOriginal();
		Job job = null;
		try {
			job = getJob(jobId);
		} catch (RemoteException e) {
		} catch (JXBPException e) {
		}
		if(job != null) {
			NewRelic.getAgent().getTracedMethod().addCustomAttribute("Job-Name", job.getName());
			NewRelic.getAgent().getTracedMethod().addCustomAttribute("Job-Node", job.getNode());
		}
		
		return jobId;
	}

	@Trace
	public JobID executeJob(JobDefinitionID var1, JobParameterWS[] var2, Integer var3, String var4) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Scheduler","JXBP",getClass().getSimpleName(),"executeJob");
		JobID jobId = Weaver.callOriginal();
		Job job = null;
		try {
			job = getJob(jobId);
		} catch (RemoteException e) {
		} catch (JXBPException e) {
		}
		if(job != null) {
			NewRelic.getAgent().getTracedMethod().addCustomAttribute("Job-Name", job.getName());
			NewRelic.getAgent().getTracedMethod().addCustomAttribute("Job-Node", job.getNode());
		}
		
		return jobId;
	}
	
	public abstract Job getJob(JobID var1) throws JXBPException, RemoteException;
}
