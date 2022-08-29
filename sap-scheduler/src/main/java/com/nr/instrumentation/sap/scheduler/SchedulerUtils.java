package com.nr.instrumentation.sap.scheduler;

import java.util.Map;

public class SchedulerUtils {

	
	public static void addValue(Map<String, Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}
}
