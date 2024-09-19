package com.newrelic.instrumentation.labs.sap.mpl;

import java.util.Map;

import com.sap.it.op.mpl.MessageProcessingLogPart;

public class MessageProcessingUtils {

	public static void addAttribute(Map<String, Object> attributes, String key, Object value) {
		if(value != null && attributes != null && key != null && !key.isEmpty()) {
			attributes.put(key, value);
		}
	}

	public static void reportMPL(MessageProcessingLogPart logPart) {
		String logString = logPart.toLogString();
		if(logString != null && !logString.isEmpty()) {
			MessageProcessingLogger.log(logString);
		}
	}


}
