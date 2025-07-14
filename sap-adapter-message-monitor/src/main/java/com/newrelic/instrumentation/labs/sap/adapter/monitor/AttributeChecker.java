package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;

public class AttributeChecker extends Thread {

	public static boolean initialized = false;
	
	private static BlockingQueue<DataHolder> queue = new LinkedBlockingQueue<DataHolder>();
	
	public static void addDataToQueue(DataHolder holder) {
		queue.add(holder);
	}
	
	public static void startChecker() {
		AttributeChecker checker = new AttributeChecker();
		checker.start();
		initialized = true;
	}
	
	private AttributeChecker() {
	}


	@SuppressWarnings("rawtypes")
	@Override
	public void run() {
		
		while(true) {
			try {
				DataHolder holder = queue.take();
				ModuleContext moduleContext = holder.moduleContext;
				ModuleData moduleData = holder.moduleData;
				long start = System.currentTimeMillis();
				Map<String, String> values = new LinkedHashMap<String, String>();
				Object principalData = moduleData.getPrincipalData();
				MessageKey messageKey = null;
				if(principalData != null) {
					if(principalData instanceof Message) {
						Message message = (Message)principalData;
						messageKey = message.getMessageKey();
					}
				}
				if (messageKey != null) {

					Enumeration keys = moduleContext.getContextDataKeys();
					while (keys.hasMoreElements()) {
						String key = keys.nextElement().toString();
						String value = moduleContext.getContextData(key);
						if (value != null) {
							values.put(key, value);
						}
					}
				}
				Enumeration supplementalNames = moduleData.getSupplementalDataNames();
				while(supplementalNames.hasMoreElements()) {
					String key = supplementalNames.nextElement().toString();
					Object value = moduleData.getSupplementalData(key);
					values.put(key, value.toString());
				}
				if(messageKey != null && values != null && !values.isEmpty()) {
					AttributeProcessor.setAttributes(messageKey, values);
				}
				long end = System.currentTimeMillis();
				NewRelic.recordMetric("/SAP/AttributeProcess/TimeToProcess", end-start);
			} catch (InterruptedException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Error occurred trying to take holder from queue");
			}
			
		}
		
	}
	
}
