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
	protected static final String ADAPTERMSGLOGFILENAME = "SAP.adaptermessagelog.log_file_name";
	protected static final String ADAPTERMSGLOGROLLOVERINTERVAL = "SAP.adaptermessagelog.log_file_interval";
	protected static final String ADAPTERMSGLOGROLLOVERSIZE = "SAP.adaptermessagelog.log_size_limit";
	protected static final String ADAPTERMSGLOGROLLOVERSIZE2 = "SAP.adaptermessagelog.log_file_size";
	protected static final String ADAPTERMSGLOGMAXFILES = "SAP.adaptermessagelog.log_file_count";
	protected static final String ADAPTERMSGLOGMENABLED = "SAP.adaptermessagelog.enabled";
	protected static final String ADAPTERMSGLOGFREQUENCY = "SAP.adaptermessagelog.frequency";
	protected static final String ADAPTERMSGLOGDELAY = "SAP.adaptermessagelog.delay";
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
		StringBuffer sb = new StringBuffer();
		int length = rolloverSize.length();
		for(int i = 0; i < length; i++) {
			char c = rolloverSize.charAt(i);
			if(Character.isDigit(c)) {
				sb.append(c);
			}
		}
		
		long size = Long.parseLong(sb.toString());
		char end = rolloverSize.charAt(length-1);
		switch (end) {
		case 'K':
			size *= 1024L;
			break;
		case 'M':
			size *= 1024L*1024L;
			break;
		case 'G':
			size *= 1024L*1024L*1024L;
			break;
		}
		
		// disallow less than 10K
		if(size < 10 * 1024L) {
			size =  10 * 1024L;
		}
		
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
		} catch (SecurityException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to handler for {0}", adapterLogFileName);
		} catch (IOException e) {
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
		Integer rolloverMinutes = agentConfig.getValue(ADAPTERMSGLOGROLLOVERINTERVAL);

		if(rolloverMinutes != null) {
			messageLoggingConfig.setRolloverMinutes(rolloverMinutes);
		}

		Integer maxFile = agentConfig.getValue(ADAPTERMSGLOGMAXFILES);
		if(maxFile != null) {
			messageLoggingConfig.setMaxLogFiles(maxFile);
		}

		String rolloverSize = agentConfig.getValue(ADAPTERMSGLOGROLLOVERSIZE);
		if(rolloverSize != null && !rolloverSize.isEmpty()) {
			messageLoggingConfig.setRolloverSize(rolloverSize);
		} else {

			rolloverSize = agentConfig.getValue(ADAPTERMSGLOGROLLOVERSIZE2);
			if(rolloverSize != null && !rolloverSize.isEmpty()) {
				messageLoggingConfig.setRolloverSize(rolloverSize);
			}
		}

		String filename = agentConfig.getValue(ADAPTERMSGLOGFILENAME);
		if(filename != null && !filename.isEmpty()) {
			messageLoggingConfig.setAdapterLog(filename);
		}

		boolean enabled = agentConfig.getValue(ADAPTERMSGLOGMENABLED, Boolean.TRUE);
		messageLoggingConfig.setEnabled(enabled);
		
		Integer freq = agentConfig.getValue(ADAPTERMSGLOGFREQUENCY);
		if(freq != null) {
			messageLoggingConfig.setFrequency(freq);
		}
		
		Integer delay = agentConfig.getValue(ADAPTERMSGLOGDELAY);
		if(delay != null) {
			messageLoggingConfig.setDelay(delay);
		}
		
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
