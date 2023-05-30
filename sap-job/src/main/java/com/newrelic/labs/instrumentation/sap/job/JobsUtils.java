package com.newrelic.labs.instrumentation.sap.job;

import com.sap.aii.af.lib.scheduler.Job;
import com.sap.aii.af.lib.scheduler.Task;

public class JobsUtils {
	

	public static String getJobName(Job job) {
		String jobName = job.getName();
		if(jobName == null || jobName.isEmpty()) {
			Task task = job.getTask();
			if(task != null) {
				jobName = task.getClass().getSimpleName();
			} else {
				jobName = "UnidentifiedTask";
			}
		}
		return jobName;
	}

}
