package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.deps.com.google.gson.Gson;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;
import com.sap.engine.interfaces.messaging.api.MessagePropertyKey;

public class AttributeMonitorLogger implements AgentConfigListener  {
	
	public static boolean initialized = false;
	private static Logger LOGGER = null;;
	private static NRLabsLoggerHandler handler = null;
	protected static boolean enabled = true;
	protected static final String ATTRIBUTE_LOG_FILENAME = "SAP.attributemonitor.log_file_name";
	protected static final String ATTRIBUTE_LOG_ENABLED = "SAP.attributemonitor.enabled";
	protected static final String ATTRIBUTE_LOG_ROLLOVER_INTERVAL = "SAP.attributemonitor.log_file_interval";
	protected static final String ATTRIBUTE_LOG_ROLLOVER_SIZE = "SAP.attributemonitor.log_size_limit";
	protected static final String ATTRIBUTE_LOG_ROLLOVER_SIZE2 = "SAP.attributemonitor.log_file_size";
	protected static final String _ATTRIBUTE_LOG_MAX_FILES = "SAP.attributemonitor.log_file_count";
	protected static final String log_file_name = "AttributeMonitor.log";
	private static final Set<String> attributes = new LinkedHashSet<String>();
	private static AttributeLoggingConfig currentConfig = null;
	private static AttributeMonitorLogger instance = null;

	public static AttributeLoggingConfig getAttributeConfig(Config agentConfig) {
		AttributeLoggingConfig attributeConfig = new AttributeLoggingConfig();

		Boolean enabled_value = agentConfig.getValue(ATTRIBUTE_LOG_ENABLED);
		if(enabled_value != null) {
			attributeConfig.setEnabled(enabled_value);
		}

		Integer rolloverMinutes = agentConfig.getValue(ATTRIBUTE_LOG_ROLLOVER_INTERVAL);
		if(rolloverMinutes != null) {
			attributeConfig.setRolloverMinutes(rolloverMinutes);
		}

		Integer maxFiles = agentConfig.getValue(_ATTRIBUTE_LOG_MAX_FILES);
		if(maxFiles != null) {
			attributeConfig.setMaxLogFiles(maxFiles);
		}

		String rolloverSize = agentConfig.getValue(ATTRIBUTE_LOG_ROLLOVER_SIZE);
		if(rolloverSize != null && !rolloverSize.isEmpty()) {
			attributeConfig.setRolloverSize(rolloverSize);
		} else {
			rolloverSize = agentConfig.getValue(ATTRIBUTE_LOG_ROLLOVER_SIZE2);
			if(rolloverSize != null && !rolloverSize.isEmpty()) {
				attributeConfig.setRolloverSize(rolloverSize);
			}
		}

		String filename = agentConfig.getValue(ATTRIBUTE_LOG_FILENAME);
		if(filename != null && !filename.isEmpty()) {
			attributeConfig.setAttributeLog(filename);
		}


		return attributeConfig;
	}

	private AttributeMonitorLogger() {
		ServiceFactory.getConfigService().addIAgentConfigListener(this);
	}
	public static void init() {
		if(instance == null) {
			instance = new AttributeMonitorLogger();
		}
		if(initialized) return;

		if(currentConfig == null) {
			currentConfig = getAttributeConfig(NewRelic.getAgent().getConfig());
		}
		enabled = currentConfig.isEnabled();
		if(!enabled) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Not initializing Attribute Monitor Log because it is not enabled in newrelic.yml");
			return;
		}

		HashMap<String,Object> currentSettings = currentConfig.getCurrentSettings();
		if(currentSettings != null) {
			NewRelic.getAgent().getInsights().recordCustomEvent("AttributeMonitorLog", currentSettings);
		}
		String rolloverSize = currentConfig.getRolloverSize();
		long rollover_size = Utils.getSize(rolloverSize);

		String attributeLogFileName = currentConfig.getAttributeLog();

		File attributeLogFile = new File(attributeLogFileName);
		if(!attributeLogFile.exists()) {
			File parent = attributeLogFile.getParentFile();
			if(!parent.exists()) {
				boolean result = parent.mkdirs();
				if(result) {
					NewRelic.getAgent().getLogger().log(Level.FINE, "Created directories needed for attribute log files: {0})", attributeLogFile);
				} else {
					NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to created directories needed for attribute log files: {0})", attributeLogFile);
				}
			}
		} else {
			boolean deleted = attributeLogFile.delete();
			if(deleted) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Deleted existing attribute log file: {0}", attributeLogFile);
			}
		}

		int maxFiles = currentConfig.getMaxLogFiles();

		int rolloverMinutes = currentConfig.getRolloverMinutes();

		if(handler == null) {
			try {
				handler = new NRLabsLoggerHandler(attributeLogFile.getPath(), rolloverMinutes, rollover_size, maxFiles);
				handler.setFormatter(new NRLabsLogFormatter());
			} catch (SecurityException | IOException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create log for {0}",attributeLogFile.getPath());
			}
        }
		
		if(LOGGER == null) {
			LOGGER = Logger.getLogger("AttributeMonitorLog");
			LOGGER.addHandler(handler);
		}
		
		initialized = true;
	}
	
	public static synchronized void addAttribute(MessagePropertyKey attribute) {
		if(!enabled) return;
		
		if(!initialized) {
			init();
		}
 
		String key = AttributeProcessor.MESSAGE_PROPERTIES + "-" + attribute.getPropertyName();
		if(!attributes.contains(key)) {
			attributes.add(key);
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("Source", AttributeProcessor.MESSAGE_PROPERTIES);
			map.put("Property-Name", attribute.getPropertyName());
			map.put("Property-Namespace", attribute.getPropertyNamespace());
			Gson gson = new Gson();
			String json = gson.toJson(map);
			logMessage(json);
		}
		
	}
	
	public static synchronized void addAttribute(String attribute, String source) {
		if(!enabled) return;
		
		if(!initialized) {
			init();
		}
		String key = source + "-" + attribute;
		if(!attributes.contains(key)) {
			attributes.add(key);
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("Source", source);
			map.put("Attribute", attribute);
			Gson gson = new Gson();
			String json = gson.toJson(map);
			logMessage(json);
		}
	}

	public static void logMessage(String message) {
		if(message != null && !message.isEmpty()) {
			LOGGER.log(Level.INFO, message);
		}
	}

	@Override
	public void configChanged(String s, AgentConfig agentConfig) {
		AttributeLoggingConfig newConfig = getAttributeConfig(agentConfig);
		if(!(newConfig.equals(currentConfig))) {
			currentConfig = newConfig;
			enabled = newConfig.isEnabled();
			init();
		}
	}
}
