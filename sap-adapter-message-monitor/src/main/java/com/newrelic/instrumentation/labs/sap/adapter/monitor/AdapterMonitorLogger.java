package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;

public class AdapterMonitorLogger {
	
	public static boolean initialized = false;
	private static Logger LOGGER = null;;
	private static NRLabsLoggerHandler handler = null;
	protected static final String ADAPTER_MONITOR_LOG_FILENAME = "SAP.adaptermonitorlog.log_file_name";
	protected static final String ADAPTER_MONITOR_LOG_ROLLOVER_INTERVAL = "SAP.adaptermonitorlog.log_file_interval";
	protected static final String ADAPTER_MONITOR_LOG_ROLLOVER_SIZE = "SAP.adaptermonitorlog.log_size_limit";
	protected static final String ADAPTER_MONITOR_LOG_MAX_FILES = "SAP.adaptermonitorlog.log_file_count";
	protected static final String ADAPTER_MONITOR_LOG_ENABLED = "SAP.adaptermonitorlog.enabled";
	protected static final String ADAPTER_MONITOR_LOG_LEVEL = "SAP.adaptermonitorlog.loglevel";
	protected static String log_file_name = "AdapterMonitor.log";
	protected static boolean enabled = true;
	private static AdapterMonitorConfig currentConfig = null;
	private AdapterMonitorLogger() {}
	private static Level current_level = Level.INFO;

	public static void init() {
		
		if(initialized) return;

		if(currentConfig == null) {
			currentConfig = getConfig(NewRelic.getAgent().getConfig());
		}

		String rolloverSize = currentConfig.getRolloverSize();
		long rollover_size = Utils.getSize(rolloverSize);

		String monitorLogFileName = currentConfig.getAdapterMonitorLog();
		File logFile = new File(monitorLogFileName);
		Utils.createDirectoriesIfNotExists(logFile);

		int maxFiles = currentConfig.getMaxLogFiles();

		if(handler == null) {
			try {
				handler = new NRLabsLoggerHandler(logFile.getPath(), 0, rollover_size, maxFiles);
				handler.setFormatter(new NRLabsLogFormatter());
			} catch (SecurityException | IOException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create log for {0}",logFile.getPath());
			}
        }
		
		if(LOGGER == null) {
			LOGGER = Logger.getLogger("AdapterMonitorLog");
			LOGGER.addHandler(handler);
		}

		current_level = currentConfig.getLevel();
		
		initialized = true;
	}

	public static AdapterMonitorConfig getConfig(Config agentConfig) {
		AdapterMonitorConfig config = new AdapterMonitorConfig();
		Boolean b = agentConfig.getValue(ADAPTER_MONITOR_LOG_ENABLED);
		if(b != null) {
			enabled = b;
			config.setEnabled(enabled);
		}

		String rollOverSize = agentConfig.getValue(ADAPTER_MONITOR_LOG_ROLLOVER_SIZE);
		if(rollOverSize != null) {
			config.setRolloverSize(rollOverSize);
		}

		String filename =  agentConfig.getValue(ADAPTER_MONITOR_LOG_FILENAME);
		if(filename != null) {
			config.setAdapterMonitorLog(filename);
		}

		Integer maxFiles = agentConfig.getValue(ADAPTER_MONITOR_LOG_MAX_FILES);
		if(maxFiles != null) {
			config.setMaxLogFiles(maxFiles);
		}

		Integer rolloverMinutes =  agentConfig.getValue(ADAPTER_MONITOR_LOG_ROLLOVER_INTERVAL);
		if(rolloverMinutes != null) {
			config.setRolloverMinutes(rolloverMinutes);
		}

		String log_level = agentConfig.getValue(ADAPTER_MONITOR_LOG_LEVEL);
		if(log_level != null) {
			config.setLevel(Level.parse(log_level));
		}
		return config;
	}

	public static void logMessage(Level level, String message) {
		int levelIntValue =  level.intValue();
		int currentLevelIntValue =  current_level.intValue();
		if(levelIntValue >= currentLevelIntValue) {
			LOGGER.log(level, message);
		}
	}

	public static void logMessage(String message) {
		if(!enabled) return;
		
		if(!initialized) {
			init();
		}
		if(message != null && !message.isEmpty()) {
			LOGGER.log(Level.INFO, message);
		}
	}
	
	public static void logError(Throwable t) {
		if(!enabled) return;
		if(!initialized) {
			init();
		}
		if(t != null) {
			LOGGER.log(Level.WARNING, "Error occurred", t);
		}
	}
	
	public static void logErrorWithMessage(String message,Throwable t) {
		if(!enabled) return;
		if(!initialized) {
			init();
		}
		if(t != null) {
			LOGGER.log(Level.WARNING, message, t);
		}
	}
	
}
