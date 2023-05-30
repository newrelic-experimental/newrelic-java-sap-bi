package com.sap.aii.af.lib.scheduler;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransactionNamePriority;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.labs.instrumentation.sap.job.JobsUtils;

@Weave(type = MatchType.BaseClass)
public abstract class JobBroker {

	@Trace(dispatcher = true)
	public void invoke(Job job) {
		String jobName = JobsUtils.getJobName(job);
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","JobBroker","run",jobName);
		NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.FRAMEWORK_HIGH, false, "Job", "Job",jobName);
		Weaver.callOriginal();
	}


	@Weave
	protected static class Worker {

		private Job job = Weaver.callOriginal();

		@Trace(dispatcher = true)
		public void run() {
			String jobName = JobsUtils.getJobName(job);
			NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","JobWorker","run",jobName);
			NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.FRAMEWORK_HIGH, false, "Job", "Job",jobName);
			Weaver.callOriginal();
		}
	}
}
