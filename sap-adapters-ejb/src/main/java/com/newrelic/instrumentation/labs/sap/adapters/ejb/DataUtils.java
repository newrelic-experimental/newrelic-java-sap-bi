package com.newrelic.instrumentation.labs.sap.adapters.ejb;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.instrumentation.labs.sap.adapters.ejb.attrributeservice.AttributeConfiguration;
import com.newrelic.instrumentation.labs.sap.adapters.ejb.attrributeservice.SAPAttributeService;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;

public class DataUtils {

	private static Set<String> contextDataAttributes = new HashSet<String>();
	//	private static Set<String> principalAttributes = new HashSet<String>();
	private static Set<String> supplementalAttributes = new HashSet<String>();
	private static ExecutorService executor = Executors.newFixedThreadPool(3);
	private static BlockingQueue<ModuleContext> contexts = new LinkedBlockingQueue<ModuleContext>(1000);
	private static BlockingQueue<ModuleData> datas = new LinkedBlockingQueue<ModuleData>(1000);
	private static Set<String> ctxAttributesToCapture = new HashSet<String>();
	//	private static Set<String> principalAttributesToCapture = new HashSet<String>();
	private static Set<String> supplementalAttributesToCapture = new HashSet<String>();

	static {
		executor.submit(new Processor());
		executor.submit(new DataProcessor());
		SAPAttributeService.INSTANCE.start();

		ctxAttributesToCapture = AttributeConfiguration.getContextAttributes();
		//		principalAttributes = AttributeConfiguration.getPrincipalAttributes();
		supplementalAttributesToCapture = AttributeConfiguration.getSupplementalAttributes();
	}

	public static void addContext(ModuleContext context) {
		contexts.add(context);
	}

	public static void addData(ModuleData data) {
		datas.add(data);
	}

	public static void reset() {
		ctxAttributesToCapture = AttributeConfiguration.getContextAttributes();
		//		principalAttributes = AttributeConfiguration.getPrincipalAttributes();
		supplementalAttributesToCapture = AttributeConfiguration.getSupplementalAttributes();
	}

	private DataUtils() {

	}

	@SuppressWarnings("unchecked")
	public static void addAttributes(ModuleContext context, Map<String, Object> attributes) {
		if(context == null) return;
		Enumeration<String> dataKeys = context.getContextDataKeys();
		while(dataKeys.hasMoreElements()) {
			String key = dataKeys.nextElement();
			if(key != null) {
				if (ctxAttributesToCapture.contains(key)) {
					String value = context.getContextData(key);
					if (value != null) {
						attributes.put(key, value);
					} 
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static void addAttributes(ModuleData moduleData, Map<String, Object> attributes) {
		if(moduleData == null) return;
		Enumeration supplementalNames = moduleData.getSupplementalDataNames();
		while(supplementalNames.hasMoreElements()) {
			String name = supplementalNames.nextElement().toString();
			if(supplementalAttributesToCapture.contains(name)) {
				Object value = moduleData.getSupplementalData(name);
				boolean valid = validate(name, value, attributes);
				if(!valid) {
					NewRelic.getAgent().getLogger().log(Level.FINE, "Invalid, Failed to add attribute {0} with value {1}", name, value);
				}
			}
		}
	}

	private static boolean validate(String key, Object value, Map<String, Object> attributes) {
		if (value instanceof String) {
			attributes.put(key, value);
			return true;
		}

		if (value instanceof Number || value instanceof Boolean || value instanceof AtomicBoolean) {
			attributes.put(key, value);
			return true;
		}

		if(value instanceof Map) {
			Map<?,?> map = (Map<?, ?>)value;
			for(Object mapKey : map.keySet()) {
				Object mapValue = map.get(mapKey);
				String keyValue = key + "-" + mapKey.toString();
				attributes.put(keyValue, mapValue);
			}
			return true;
		}

		NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to add attribute {0} to span attributes because the value type (1} is not a valid type", key, value.getClass());
		return false;
	}

	private static class Processor implements Runnable {

		@SuppressWarnings("unchecked")
		@Override
		public void run() {

			while(true) {
				try {
					ModuleContext context = contexts.poll(10, TimeUnit.SECONDS);
					if(context != null) {
						Enumeration<String> keys = context.getContextDataKeys();
						while(keys.hasMoreElements()) {
							String key = keys.nextElement();
							if(key != null && !key.isEmpty()) {
								synchronized (contextDataAttributes) {
									if (!contextDataAttributes.contains(key)) {
										contextDataAttributes.add(key);
										EJBAdapterLogger.logNewAttribute(key);
										NewRelic.getAgent().getLogger().log(Level.FINE, "Add context key {0}", key);
									}
								}
							}
						}

					}
				} catch (InterruptedException e) {
					NewRelic.getAgent().getLogger().log(Level.FINEST, e, "Module Context Checker was interuptted");
				}

			}
		}

	}

	private static class DataProcessor implements Runnable {

		@SuppressWarnings("unchecked")
		@Override
		public void run() {

			while(true) {
				try {
					ModuleData data = datas.poll(10, TimeUnit.SECONDS);
					if(data != null) {
						Enumeration<String> keys = data.getSupplementalDataNames();
						while(keys.hasMoreElements()) {
							String key = keys.nextElement();
							if(key != null && !key.isEmpty()) {
								if(!supplementalAttributes.contains(key)) {
									supplementalAttributes.add(key);
									EJBAdapterLogger.logNewSupplementalAttribute(key);
									NewRelic.getAgent().getLogger().log(Level.FINE,"Add supplemental key {0}",key);
								}
							}
						}

					}
				} catch (InterruptedException e) {
					NewRelic.getAgent().getLogger().log(Level.FINEST, e, "Module Supplemental Checker was interuptted");
				}

			}
		}

	}



}
