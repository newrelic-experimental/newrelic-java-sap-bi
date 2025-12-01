package com.newrelic.instrumentation.labs.sap.channel.monitoring;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;

public class ChannelMonitoringLogger implements AgentConfigListener  {

	public static boolean initialized = false;
	private static Logger CHANNELSLOGGER;
	protected static final String CHANNELLOGFILENAME = "SAP.channelmonitoring.log_file_name";
	protected static final String CHANNELLOGROLLOVERINTERVAL = "SAP.channelmonitoring.log_file_interval";
	protected static final String CHANNELLOGROLLOVERSIZE = "SAP.channelmonitoring.log_size_limit";
	protected static final String CHANNELLOGROLLOVERSIZE2 = "SAP.channelmonitoring.log_file_size";
	protected static final String CHANNELLOGMAXFILES = "SAP.channelmonitoring.log_file_count";
	protected static final String CHANNELSLOGENABLED = "SAP.channelmonitoring.enabled";
	protected static final String CHANNELSLOGCOLLECTIONPERIOD = "SAP.channelmonitoring.collection_period";
	public static final String log_file_name = "channels.log";
	private static ChannelMonitoringConfig currentChannelConfig = null;

	private static ChannelMonitoringLogger INSTANCE = null;
	private static final Object LOCK = new Object();
	private static NRLabsHandler channelsHandler;

	private ChannelMonitoringLogger() {

	}

	protected static ChannelMonitoringConfig getConfig() {
		if(currentChannelConfig != null) return currentChannelConfig;
		Config agentConfig = NewRelic.getAgent().getConfig();
		return getConfig(agentConfig);
	}

	protected static void logToChannelMonitor(String message) {
		if(!initialized) {
			init();
		}
		CHANNELSLOGGER.log(Level.INFO, message);
	}

	public static void init() {
		synchronized(LOCK) {
			if(INSTANCE == null) {
				INSTANCE = new ChannelMonitoringLogger();
				ServiceFactory.getConfigService().addIAgentConfigListener(INSTANCE);
			}
		}
		if(currentChannelConfig == null) {
			Config agentConfig = NewRelic.getAgent().getConfig();
			currentChannelConfig = getConfig(agentConfig);
		}

		int rolloverMinutes = currentChannelConfig.getRolloverMinutes();

		if(rolloverMinutes == 0) {
			rolloverMinutes = 60;
		}

		String rolloverSize = currentChannelConfig.getRolloverSize();
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

		String channelMonitorFileName = currentChannelConfig.getChannelLog();
		// Ensure that the parent directory exists and if not attempt to create it
		File file = new File(channelMonitorFileName);
		File parent = file.getParentFile();
		if(!parent.exists()) {
			boolean result = parent.mkdirs();
			if(result) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Created directories needed for channel monitoring log files: {0})", channelMonitorFileName);
			} else {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to created directories needed for channel monitoring log files: {0})", channelMonitorFileName);			}
		}

		int maxFiles = currentChannelConfig.getMaxLogFiles();

		// If we have already initialized the handler then perform cleanup
		if(channelsHandler != null) {
			channelsHandler.flush();
			channelsHandler.close();
			if(CHANNELSLOGGER != null) {
				CHANNELSLOGGER.removeHandler(channelsHandler);
			}
			channelsHandler = null;
		}

		try {
			channelsHandler = new NRLabsHandler(channelMonitorFileName, rolloverMinutes, size, maxFiles);
			channelsHandler.setFormatter(new NRLabsFormatter());
		} catch (SecurityException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create channel monitoring log file at {0}", channelMonitorFileName);
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create channel monitorin log file at {0}", channelMonitorFileName);
		}


		if (CHANNELSLOGGER == null) {
			try {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Building Log File, name: {0}, size: {1}, maxfiles: {2}", channelMonitorFileName, size, maxFiles);
				CHANNELSLOGGER = Logger.getLogger("ChannelMonitorLog");
			} catch (SecurityException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create channel monitoring log file at {0}", channelMonitorFileName);
			} 
		}

		if(CHANNELSLOGGER != null && channelsHandler != null) {
			CHANNELSLOGGER.addHandler(channelsHandler);
		}

		initialized = true;
	}

	public static void checkConfig() {
		Config agentConfig = NewRelic.getAgent().getConfig();
		ChannelMonitoringConfig channelConfig = getConfig(agentConfig);
		if(channelConfig != null) {
			ChannelMonitor.enabled = channelConfig.isEnabled();
			if(currentChannelConfig == null) {
				currentChannelConfig = channelConfig;
				NewRelic.getAgent().getInsights().recordCustomEvent("ChannelMonitoringConfig", currentChannelConfig.getCurrentSettings());
				initialized = false;
				init();
			} else {
				if(!channelConfig.equals(currentChannelConfig)) {
					currentChannelConfig = channelConfig;
					NewRelic.getAgent().getInsights().recordCustomEvent("ChannelMonitoringConfig", currentChannelConfig.getCurrentSettings());
					initialized = false;
					init();
				}
			}
		}
	}

	public static ChannelMonitoringConfig getConfig(Config agentConfig) {
		ChannelMonitoringConfig channelConifg = new ChannelMonitoringConfig();
		Integer rolloverMinutes = agentConfig.getValue(CHANNELLOGROLLOVERINTERVAL);

		if(rolloverMinutes != null) {
			channelConifg.setRolloverMinutes(rolloverMinutes);
		}

		Integer maxFile = agentConfig.getValue(CHANNELLOGMAXFILES);
		if(maxFile != null) {
			channelConifg.setMaxLogFiles(maxFile);
		}

		String rolloverSize = agentConfig.getValue(CHANNELLOGROLLOVERSIZE);
		if(rolloverSize != null && !rolloverSize.isEmpty()) {
			channelConifg.setRolloverSize(rolloverSize);
		} else {

			rolloverSize = agentConfig.getValue(CHANNELLOGROLLOVERSIZE2);
			if(rolloverSize != null && !rolloverSize.isEmpty()) {
				channelConifg.setRolloverSize(rolloverSize);
			}
		}

		String filename = agentConfig.getValue(CHANNELLOGFILENAME);
		if(filename != null && !filename.isEmpty()) {
			channelConifg.setChannelLog(filename);
		}

		boolean enabled = agentConfig.getValue(CHANNELSLOGENABLED, Boolean.TRUE);
		channelConifg.setEnabled(enabled);

		Integer collection = agentConfig.getValue(CHANNELSLOGCOLLECTIONPERIOD);
		if(collection != null) {
			channelConifg.setCollection_period(collection);
		}

		return channelConifg;

	}

	public void configChanged(String appName, AgentConfig agentConfig) {
		ChannelMonitoringConfig channelConfig = getConfig(agentConfig);
		if(channelConfig != null) {
			ChannelMonitor.enabled = channelConfig.isEnabled();
			if(currentChannelConfig == null) {
				currentChannelConfig = channelConfig;
				NewRelic.getAgent().getInsights().recordCustomEvent("ChannelMonitoringConfig", currentChannelConfig.getCurrentSettings());
				initialized = false;
				init();
			} else {
				if(!channelConfig.equals(currentChannelConfig)) {
					currentChannelConfig = channelConfig;

					NewRelic.getAgent().getInsights().recordCustomEvent("ChannelMonitoringConfig", currentChannelConfig.getCurrentSettings());
					if(currentChannelConfig.isIntervalChangeOnly(channelConfig)) {
						ChannelMonitor.reinitialScheduled(channelConfig.getCollection_period());
					} else {
						initialized = false;
						init();
					}
				}
			}
		}
	}

}
