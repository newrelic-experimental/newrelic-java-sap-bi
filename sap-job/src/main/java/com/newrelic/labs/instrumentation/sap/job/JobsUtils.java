package com.newrelic.labs.instrumentation.sap.job;

import java.util.regex.Pattern;

import com.sap.aii.af.lib.scheduler.Job;
import com.sap.aii.af.lib.scheduler.Task;

public class JobsUtils {
	
	private static final Pattern pattern = Pattern.compile("[0-9a-f]+");
	
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
		int index = jobName.indexOf('_');
		if(index > -1) {
			String temp = jobName.substring(index+1);
			if(pattern.matcher(temp).matches()) {
				return jobName.substring(0, index) + "_xxxxx";
			}
		}
		return jobName;
	}

}
