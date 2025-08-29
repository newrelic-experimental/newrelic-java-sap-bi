package com.newrelic.instrumentation.labs.sap.adapters;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.Insights;
import com.newrelic.api.agent.NewRelic;

public class AdapterAttributesLogger {

	protected static final String ADAPTER_LOG_NAME = "adapter.log";
	protected static final String ADAPTER_LOG_DIR = "adapterLogging";
	private static Logger ADAPTER_LOGGER;
	protected static final String ADAPTERLOGFILENAME = "SAP.adapterlog.log_file_name";
	protected static final String ADAPTERLOGGINGROLLOVERINTERVAL = "SAP.adapterlog.log_file_interval";
	protected static final String ADAPTERLOGGINGROLLOVERSIZE = "SAP.adapterlog.log_size_limit";
	protected static final String ADAPTERLOGGINGROLLOVERSIZE2 = "SAP.adapterlog.log_file_size";
	protected static final String ADAPTERLOGGINGMAXFILES = "SAP.adapterlog.log_file_count";
	protected static final String ADAPTERLOGENABLED = "SAP.adapterlog.enabled";

	private static AdapterAttributesConfig currentAdapterConfig = null;

	private static AdapterAttributesLogger INSTANCE = null;

	private static NRLabsFullHandler handler;

	private static boolean enabled = true;
	public static boolean initialized = false;

	protected static AdapterAttributesLogger getLogger() {
		if(INSTANCE == null) {
			INSTANCE = new AdapterAttributesLogger();
		}
		return INSTANCE;
	}

	private AdapterAttributesLogger() {

	}

	public static AdapterAttributesConfig getCurrentConfig() {

		Config agentConfig = NewRelic.getAgent().getConfig();
		AdapterAttributesConfig newConfig = getConfig(agentConfig);
		if(currentAdapterConfig == null || !newConfig.equals(currentAdapterConfig)) {
			currentAdapterConfig = newConfig;
			init();
		}
		return currentAdapterConfig;
	}
	
	

	protected static AdapterAttributesConfig getConfig(Config agentConfig) {
		AdapterAttributesConfig adapterConfig = new AdapterAttributesConfig();

		Integer rolloverMinutes = agentConfig.getValue(ADAPTERLOGGINGROLLOVERINTERVAL);
		if(rolloverMinutes != null) {
			adapterConfig.setRolloverMinutes(rolloverMinutes);
		}

		Integer maxFile = agentConfig.getValue(ADAPTERLOGGINGMAXFILES);
		if(maxFile != null) {
			adapterConfig.setMaxLogFiles(maxFile);
		}

		String rolloverSize = agentConfig.getValue(ADAPTERLOGGINGROLLOVERSIZE);
		if(rolloverSize != null && !rolloverSize.isEmpty()) {
			adapterConfig.setRolloverSize(rolloverSize);
		} else {

			rolloverSize = agentConfig.getValue(ADAPTERLOGGINGROLLOVERSIZE2);
			if(rolloverSize != null && !rolloverSize.isEmpty()) {
				adapterConfig.setRolloverSize(rolloverSize);
			}
		}

		String filename = agentConfig.getValue(ADAPTERLOGFILENAME);
		if(filename != null && !filename.isEmpty()) {
			adapterConfig.setAdapterLog(filename);
		} else {
			// Use default file name
			File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
			File loggingDir = new File(newRelicDir,ADAPTER_LOG_DIR);
			File logFile = new File(loggingDir, ADAPTER_LOG_NAME);
			adapterConfig.setAdapterLog(logFile.getAbsolutePath());
		}

		boolean enabled = agentConfig.getValue(ADAPTERLOGENABLED, Boolean.TRUE);
		adapterConfig.setAdapterLog_enabled(enabled);

		
		return adapterConfig;
	}

	public static synchronized void init() {
		if(initialized) return;
		

		if(currentAdapterConfig == null) {
			Config agentConfig = NewRelic.getAgent().getConfig();
			currentAdapterConfig = getConfig(agentConfig);
		}

		enabled = currentAdapterConfig.isAdapterLog_enabled();

		if(INSTANCE == null) {
			INSTANCE = new AdapterAttributesLogger();
		}

		int rolloverMinutes = currentAdapterConfig.getRolloverMinutes();
		if(rolloverMinutes < 1) {
			rolloverMinutes = 60;
		}

		String rolloverSize = currentAdapterConfig.getRolloverSize();
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

		String adapterFileName = currentAdapterConfig.getAdapterLog();
		File file = null;
		file = new File(adapterFileName);
		File parent = file.getParentFile();

		if(!parent.exists()) {
			boolean result = parent.mkdirs();
			if(result) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Created directories needed for adapter log files: {0})", adapterFileName);
			} else {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to created directories needed for adapter log files: {0})", adapterFileName);			}
		}

		int maxFile = currentAdapterConfig.getMaxLogFiles();
		if(handler != null) {
			handler.flush();
			handler.close();
			if(ADAPTER_LOGGER != null) {
				ADAPTER_LOGGER.removeHandler(handler);
			}
			handler = null;
		}

		try {
			handler = new NRLabsFullHandler(file.getAbsolutePath(), end, size, maxFile);
			handler.setFormatter(new NRLabsFormatter());
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create NRHandler for adapter log");
		}

		if(ADAPTER_LOGGER == null) {
			ADAPTER_LOGGER = Logger.getLogger("AdapterInfoLog");
		}

		ADAPTER_LOGGER.setLevel(Level.INFO);
		ADAPTER_LOGGER.setUseParentHandlers(false);

		if(ADAPTER_LOGGER != null && handler != null) {
			ADAPTER_LOGGER.addHandler(handler);
		}
		
		Insights insights = NewRelic.getAgent().getInsights();
		
		if (insights != null) {
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			attributes.put("AdapterLogFile", file.getAbsolutePath());
			attributes.put("Maximum Log Files", maxFile);
			attributes.put("Rollover Size", rolloverSize);
			attributes.put("Rollover Minutes", rolloverMinutes);
			attributes.put("Enabled", enabled);
			insights.recordCustomEvent("AdapterLogInit", attributes);
		} else {
			// Insights not enabled (yet)
			NewRelic.getAgent().getLogger().log(Level.FINE, "Adapter logging configuration: {0}", currentAdapterConfig);
		}
		NewRelic.getAgent().getLogger().log(Level.FINE, "AdapterAttributeLogger has been initialized");
		initialized = true;
	}

	public void log(String message) {
		if(!enabled) return;

		if(!initialized) {
			init();
		}

		ADAPTER_LOGGER.log(Level.INFO, message);
	}

}
