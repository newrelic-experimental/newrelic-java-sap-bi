package com.newrelic.instrumentation.labs.sap.soap;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.lib.mp.module.ModuleContext;

public class DataUtils implements AgentConfigListener {

	private static Set<String> contextDataAttributes = new HashSet<String>();
	private static ExecutorService executor = Executors.newFixedThreadPool(2);
	private static BlockingQueue<ModuleContext> contexts = new LinkedBlockingQueue<ModuleContext>(1000);
	private static Set<String> attributesToCapture = new HashSet<String>();
	private static String contextDataKey = "SAP.ContextData.attributes";
	
	static {
		executor.submit(new Processor());
		DataUtils utils = new DataUtils();
		Config config = NewRelic.getAgent().getConfig();
		if(config instanceof AgentConfig) {
			utils.configChanged("", (AgentConfig)config);
		}
		ServiceFactory.getConfigService().addIAgentConfigListener(utils);
	}
	
	public static void addContext(ModuleContext context) {
		contexts.add(context);
	}
	
	private DataUtils() {
		
	}
	
	@SuppressWarnings("unchecked")
	public static boolean addAttributes(ModuleContext context, Map<String, Object> attributes) {
		Enumeration<String> dataKeys = context.getContextDataKeys();
		boolean attributesAdded = false;
		while(dataKeys.hasMoreElements()) {
			String key = dataKeys.nextElement();
			if(key != null) {
				if (attributesToCapture.contains(key)) {
					String value = context.getContextData(key);
					if (value != null) {
						attributes.put(key, value);
						attributesAdded = true;
					} 
				}
			}
		}
		return attributesAdded;
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
								if(!contextDataAttributes.contains(key)) {
									contextDataAttributes.add(key);
									SOAPAdapterLogger.logNewAttribute(key);
									NewRelic.getAgent().getLogger().log(Level.FINE,"Add context key {0}",key);
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

	@Override
	public void configChanged(String appName, AgentConfig agentConfig) {
		String attributesToCapture = agentConfig.getValue(contextDataKey);
		if(attributesToCapture != null && !attributesToCapture.isEmpty()) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "found list of module context attributes to capture: {0}", attributesToCapture);
			String[] attributes = attributesToCapture.split(",");
			for(String attribute : attributes) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Will capture modulecontext attribute: {0}", attribute);
				DataUtils.attributesToCapture.add(attribute);
			}
		}
	}
	
}
