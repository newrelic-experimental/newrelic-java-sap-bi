package com.newrelic.instrumentation.labs.sap.adapters;

import java.util.Map;
import java.util.Set;

public class LoggingUtils {

	public static void logAttributes(Map<String, Object> attributes) {
		if(attributes == null || attributes.isEmpty()) return;
		Set<String> keys = attributes.keySet();
		StringBuffer sb = new StringBuffer();
		int size = attributes.size();
		int i = 0;
		for(String key : keys) {
			Object value = attributes.get(key);
			if(value != null) {
				sb.append(key+"="+value.toString());
				if(i < size -1) {
					sb.append(',');
				}
			}
			i++;
		}
		AdapterAttributesLogger.getLogger().log(sb.toString());
	}
}
