package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.engine.interfaces.messaging.api.MessageKey;

public class AttributeProcessor {

	protected static Map<String, Map<String,String>> message_Attributes = new HashMap<String, Map<String,String>>(10);
	protected static Map<String,Date> lastModified = new HashMap<String, Date>();
	private static final long THRESHOLD = 86400 * 1000L;
	private static final int HOURS_BETWEEN_PURGES = 4;
	
	static {
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {cleanAttributeMap();}, HOURS_BETWEEN_PURGES, HOURS_BETWEEN_PURGES, TimeUnit.HOURS);
	}

	private static void cleanAttributeMap() {
		long threshold_time = System.currentTimeMillis() - THRESHOLD;
		Date threshold_date = new Date(threshold_time);
		int removed = 0;
		
		for(String key : lastModified.keySet()) {
			Date last = lastModified.get(key);
			if(last.before(threshold_date)) {
				lastModified.remove(key);
				message_Attributes.remove(key);
				removed++;
			}
		}
		
		if(removed > 0) {
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			attributes.put("NumberOfEntriesPurged", removed);
			NewRelic.getAgent().getInsights().recordCustomEvent("AdapterAttributesPurge", attributes);
		}
	}

	protected static void setAttributes(MessageKey messageKey, Map<String,String> attributes) {
		String msgKey = getMessageKeyString(messageKey);
		Map<String,String> current = message_Attributes.get(msgKey);
		if(current == null) {
			message_Attributes.put(msgKey, attributes);
			lastModified.put(msgKey, new Date());
		} else {
			boolean modified = false;
			for(String key : current.keySet()) {
				String currentValue = current.get(key);
				String newValue = attributes.get(key);
				if(!currentValue.equals(newValue)) {
					modified = true;
					break;
				}
			}
			if(modified) {
				current.putAll(attributes);
				lastModified.put(msgKey, new Date());
				message_Attributes.put(getMessageKeyString(messageKey), current);
			}
			
		}
	}

	public static void record(ModuleContext moduleContext, ModuleData moduleData) {
		if(!AttributeChecker.initialized) {
			AttributeChecker.startChecker();
		}
		AttributeChecker.addDataToQueue(new DataHolder(moduleData, moduleContext));
	}

	public static String getMessageKeyString(MessageKey msgKey) {
		return msgKey.toString();
	}

	public static Map<String,String> getMessageAttributes(MessageKey messageKey) {
		return message_Attributes.get(getMessageKeyString(messageKey));
	}

}
