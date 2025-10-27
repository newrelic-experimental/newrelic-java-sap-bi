package com.newrelic.instrumentation.labs.sap.mpl;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.sap.it.op.mpl.MessageProcessingLogPart;

public class MessageProcessingUtils {

	private static boolean collectName = true;
	private static boolean collectId = true;
	private static boolean collectBranchId = true;
	private static boolean initailized = false;

	public static void addAttribute(Map<String, Object> attributes, String key, Object value) {
		if(value != null && attributes != null && key != null && !key.isEmpty()) {
			attributes.put(key, value);
		}
	}

	private static void initialize(Object obj) {
		if(obj != null) {
			Class<?> clazz = obj.getClass();
			try {
				Method method = clazz.getMethod("getName", new Class<?>[] {});
				NewRelic.getAgent().getLogger().log(Level.INFO, "Found Method {0} on Class {1}",method.getName(), clazz);
				collectName = true;
			} catch (Exception e) {
				collectName = false;
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Unable to find method getName on MessageProcessingLogPart");
			}
			try {
				Method method = clazz.getMethod("getId", new Class<?>[] {});
				NewRelic.getAgent().getLogger().log(Level.INFO, "Found Method {0} on Class {1}",method.getName(), clazz);
				collectId = true;
			} catch (Exception e) {
				collectId = false;
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Unable to find method getId on MessageProcessingLogPart");
			}
			try {
				Method method = clazz.getMethod("getBranchId", new Class<?>[] {});
				NewRelic.getAgent().getLogger().log(Level.INFO, "Found Method {0} on Class {1}",method.getName(), clazz);
				collectBranchId = true;
			} catch (Exception e) {
				collectBranchId = false;
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Unable to find method getBranchId on MessageProcessingLogPart");
			}
			initailized = true;
		}
	}

	public static void reportMPL(MessageProcessingLogPart logPart) {
		if(!initailized) {
			initialize(logPart);
		}
		String logString = logPart.toLogString();

		if(logString != null && !logString.isEmpty()) {
			StringBuffer buffer = new StringBuffer(logString);
			if (collectName) {
				String name = logPart.getName();
				if (name != null && !name.isEmpty()) {
					buffer.append("; Name = " + name);
				}
			}
			if (collectId) {
				String id = logPart.getId();
				if (id != null && !id.isEmpty()) {
					buffer.append("; ID = " + id);
				}
			}
			if(collectBranchId) {
				String branchId = logPart.getBranchId();
				if(branchId != null && !branchId.isEmpty()) {
					buffer.append("; BranchID = " + branchId);
				}
			}
			MessageProcessingLogger.log(buffer.toString());
		}
	}


}