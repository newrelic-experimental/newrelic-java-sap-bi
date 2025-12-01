package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;

public class AdapterMessageLogger implements AgentConfigListener {

	public static boolean initialized = false;
	private static Logger LOGGER;
	protected static final String ADAPTERMSG_LOG_FILENAME = "SAP.adaptermessagelog.log_file_name";
	protected static final String ADAPTERMSG_LOG_ROLLOVER_INTERVAL = "SAP.adaptermessagelog.log_file_interval";
	protected static final String ADAPTERMSG_LOG_ROLLOVER_SIZE = "SAP.adaptermessagelog.log_size_limit";
	protected static final String ADAPTERMSG_LOG_ROLLOVER_SIZE2 = "SAP.adaptermessagelog.log_file_size";
	protected static final String ADAPTERMSG_LOG_MAX_FILES = "SAP.adaptermessagelog.log_file_count";
	protected static final String ADAPTERMSG_LOG_ENABLED = "SAP.adaptermessagelog.enabled";
	protected static final String ADAPTER_MONITOR_LOGGING = "SAP.adaptermonitor.monitorlogging.enabled";
	protected static final String log_file_name = "adapter_message.log";
	private static MessageLoggingConfig currentConfig = null;
	private static AdapterMessageLogger INSTANCE = null;
	private static boolean enabled = true;
	
	private static NRLabsHandler handler;
	
	
	public static void log(String message) {
		if(!initialized) {
			init();
		}
		if(enabled) {
			LOGGER.log(Level.INFO, message);
		}
	}
	
	private AdapterMessageLogger () {
		ServiceFactory.getConfigService().addIAgentConfigListener(this);
		NewRelic.getAgent().getLogger().log(Level.FINE, "Registered AdapterMessageLogger as an agent configuration listener");
	}
	
	protected static MessageLoggingConfig getCurrentConfig() {
		if(currentConfig == null) {
			currentConfig = getConfig(NewRelic.getAgent().getConfig());
		}
		return currentConfig;
	}
	
	public static void init() {
		
		if(INSTANCE == null) {
			INSTANCE = new AdapterMessageLogger();
		}
		
		if(initialized) return;
		
		if(currentConfig == null) {
			Config agentConfig = NewRelic.getAgent().getConfig();
			currentConfig = getConfig(agentConfig);
		}
		enabled = currentConfig.isEnabled();
		
		if(!enabled) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Not initializing Adapter Message Log because it is not enabled in newrelic.yml");
			
			return;
		}

		HashMap<String, Object> currentSettings = currentConfig.getCurrentSettings();
		if(currentSettings != null) {
			NewRelic.getAgent().getInsights().recordCustomEvent("MessageAdapterLog", currentSettings);
		}
		
		int rolloverMinutes = currentConfig.getRolloverMinutes();
		if(rolloverMinutes == 0) {
			rolloverMinutes = 60;
		}

		String rolloverSize = currentConfig.getRolloverSize();
		long size = Utils.getSize(rolloverSize);
		String adapterLogFileName = currentConfig.getAdapterLog();
		// Ensure that the parent directory exists and if not attempt to create it
		File file = new File(adapterLogFileName);
		File parent = file.getParentFile();
		if(!parent.exists()) {
			boolean result = parent.mkdirs();
			if(result) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Created directories needed for adapter message log files: {0})", adapterLogFileName);
			} else {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to created directories needed for adapter message log files: {0})", adapterLogFileName);			
			}
		}

		int maxFiles = currentConfig.getMaxLogFiles();

		if(handler != null) {
			handler.flush();
			handler.close();
			if(LOGGER != null) {
				LOGGER.removeHandler(handler);
			}
			handler = null;
		}
		
		try {
			handler = new NRLabsHandler(adapterLogFileName, rolloverMinutes, size, maxFiles);
			handler.setFormatter(new NRLabsFormatter());
		} catch (SecurityException | IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to handler for {0}", adapterLogFileName);
		}

        if (LOGGER == null) {
			try {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Building Adapter Message Log File, name: {0}, size: {1}, maxfiles: {2}", adapterLogFileName, size, maxFiles);
				LOGGER = Logger.getLogger("AdapterMessageLog");
			} catch (SecurityException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create Adapter Message log file at {0}", adapterLogFileName);
			} 
		}

		if(LOGGER != null && handler != null) {
			LOGGER.addHandler(handler);
		}

		initialized = true;
	}
	
	public static MessageLoggingConfig getConfig(Config agentConfig) {
		MessageLoggingConfig messageLoggingConfig = new MessageLoggingConfig();
		Integer rolloverMinutes = agentConfig.getValue(ADAPTERMSG_LOG_ROLLOVER_INTERVAL);

		if(rolloverMinutes != null) {
			messageLoggingConfig.setRolloverMinutes(rolloverMinutes);
		}

		Integer maxFile = agentConfig.getValue(ADAPTERMSG_LOG_MAX_FILES);
		if(maxFile != null) {
			messageLoggingConfig.setMaxLogFiles(maxFile);
		}

		String rolloverSize = agentConfig.getValue(ADAPTERMSG_LOG_ROLLOVER_SIZE);
		if(rolloverSize != null && !rolloverSize.isEmpty()) {
			messageLoggingConfig.setRolloverSize(rolloverSize);
		} else {

			rolloverSize = agentConfig.getValue(ADAPTERMSG_LOG_ROLLOVER_SIZE2);
			if(rolloverSize != null && !rolloverSize.isEmpty()) {
				messageLoggingConfig.setRolloverSize(rolloverSize);
			}
		}

		String filename = agentConfig.getValue(ADAPTERMSG_LOG_FILENAME);
		if(filename != null && !filename.isEmpty()) {
			messageLoggingConfig.setAdapterLog(filename);
		}

		boolean enabled = agentConfig.getValue(ADAPTERMSG_LOG_ENABLED, Boolean.TRUE);
		messageLoggingConfig.setEnabled(enabled);
		
		Boolean monitorLggging = agentConfig.getValue(ADAPTER_MONITOR_LOGGING);
		if(monitorLggging != null) {
			AdapterMonitorLogger.enabled = monitorLggging;
		}
		
		return messageLoggingConfig;

	}

	@Override
	public void configChanged(String appName, AgentConfig agentConfig) {
		NewRelic.getAgent().getLogger().log(Level.FINE, "In AdapterMessageLogger, processing agent configuration change");
		MessageLoggingConfig newConfig = getConfig(agentConfig);
		if(!(newConfig.equals(currentConfig))) {
			currentConfig = newConfig;
			init();
		}
		Boolean monitorLggging = agentConfig.getValue(ADAPTER_MONITOR_LOGGING);
		if(monitorLggging != null) {
			AdapterMonitorLogger.enabled = monitorLggging;
		}
	}

}
