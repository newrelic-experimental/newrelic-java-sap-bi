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
			StringBuffer buffer = new StringBuffer(logString);
			String name = logPart.getName();
			if(name != null && !name.isEmpty()) {
				buffer.append("; Name = " + name);
			}
			String id = logPart.getId();
			if(id != null && !id.isEmpty()) {
				buffer.append("; ID = " + id);
			}
			String branchId = logPart.getBranchId();
			if(branchId != null && !branchId.isEmpty()) {
				buffer.append("; BranchID = " + branchId);
			}
			
			MessageProcessingLogger.log(logString);
		}
	}


}