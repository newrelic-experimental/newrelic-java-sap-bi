package com.newrelic.instrumentation.labs.sap.cc;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;

public class CommunicationChannelLogger  {

	public static boolean initialized = false;
	private static Logger DETAILSLOGGER;
	private static Logger SUMMARYLOGGER;
	protected static final String CHANNELLOGFILENAME = "SAP.communicationlog.log_file_name";
	protected static final String SUMMARYCHANNELLOGFILENAME = "SAP.communicationlog.summarylog_file_name";
	protected static final String CHANNELLOGROLLOVERINTERVAL = "SAP.communicationlog.log_file_interval";
	protected static final String CHANNELLOGIGNORES = "SAP.communicationlog.ignores";
	protected static final String CHANNELLOGROLLOVERSIZE = "SAP.communicationlog.log_size_limit";
	protected static final String CHANNELLOGROLLOVERSIZE2 = "SAP.communicationlog.log_file_size";
	protected static final String CHANNELLOGMAXFILES = "SAP.communicationlog.log_file_count";
	protected static final String CHANNELSLOGENABLED = "SAP.communicationlog.enabled";
	protected static final String CHANNELSSUMMARYLOGENABLED = "SAP.communicationlog.summarylog_enabled";
	public static final String log_file_name = "communicationchannels.log";
	public static final String summary_log_file_name = "channelsummary.log";
	private static CommunicationChannelConfig currentChannelConfig = null;
	
	private static CommunicationChannelLogger INSTANCE = null;
	private static NRLabsHandler detailsHandler;
	private static NRLabsHandler summaryHandler;
	
	private CommunicationChannelLogger() {
		
	}

	protected static CommunicationChannelConfig getConfig() {
		if(currentChannelConfig != null) return currentChannelConfig;
		Config agentConfig = NewRelic.getAgent().getConfig();
		return getConfig(agentConfig);
	}

	protected static void logToDetatils(String message) {
		if(!initialized) {
			init();
		}
		DETAILSLOGGER.log(Level.INFO, message);
	}

	protected static void logToSummary(String message) {
		if(!initialized) {
			init();
		}
		SUMMARYLOGGER.log(Level.INFO, message);
	}

	public static void init() {
		if(INSTANCE == null) {
			INSTANCE = new CommunicationChannelLogger();
			//ServiceFactory.getConfigService().addIAgentConfigListener(INSTANCE);
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

		String communicationFileName = currentChannelConfig.getChannelLog();
		// Ensure that the parent directory exists and if not attempt to create it
		File file = new File(communicationFileName);
		File parent = file.getParentFile();
		if(!parent.exists()) {
			boolean result = parent.mkdirs();
			if(result) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Created directories needed for communication log files: {0})", communicationFileName);
			} else {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to created directories needed for communication log files: {0})", communicationFileName);			}
		}

		int maxFiles = currentChannelConfig.getMaxLogFiles();
		String summaryFileName = currentChannelConfig.getSummaryChannelLog();
		file = new File(summaryFileName);
		parent = file.getParentFile();
		if(!parent.exists()) {
			boolean result = parent.mkdirs();
			if(result) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Created directories needed for communication summary log files: {0})", summaryFileName);
			} else {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to created directories needed for communication summary log files: {0})", summaryFileName);			}
		}
		
		// If we have already initialized the handler then perform cleanup
		if(detailsHandler != null) {
			detailsHandler.flush();
			detailsHandler.close();
			if(DETAILSLOGGER != null) {
				DETAILSLOGGER.removeHandler(detailsHandler);
			}
			detailsHandler = null;
		}

		try {
			detailsHandler = new NRLabsHandler(communicationFileName, rolloverMinutes, size, maxFiles);
			detailsHandler.setFormatter(new NRLabsFormatter());
		} catch (SecurityException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create details communication log file at {0}", communicationFileName);
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create details communication log file at {0}", communicationFileName);
		}


		if (DETAILSLOGGER == null) {
			try {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Building Details Log File, name: {0}, size: {1}, maxfiles: {2}", communicationFileName, size, maxFiles);
				DETAILSLOGGER = Logger.getLogger("CommunctionChannelLog");
			} catch (SecurityException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create details communication log file at {0}", communicationFileName);
			} 
		}

		if(DETAILSLOGGER != null && detailsHandler != null) {
			DETAILSLOGGER.addHandler(detailsHandler);
		}
		
		if(summaryHandler != null) {
			summaryHandler.flush();
			summaryHandler.close();
			if(SUMMARYLOGGER != null) {
				SUMMARYLOGGER.removeHandler(summaryHandler);
			}
			summaryHandler = null;
		}
		
		try {
			summaryHandler = new NRLabsHandler(summaryFileName, rolloverMinutes, size, maxFiles);

			summaryHandler.setFormatter(new NRLabsFormatter());
		} catch (SecurityException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create summary communication log file at {0}", summaryFileName);
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create summary communication log file at {0}", summaryFileName);
		}

		if (SUMMARYLOGGER == null) {
			try {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Building summary Log File, name: {0}, size: {1}, maxfiles: {2}", summaryFileName, size, maxFiles);

				SUMMARYLOGGER = Logger.getLogger("SummaryChannelLog");
			} catch (SecurityException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create summary communication log file at {0}", summaryFileName);
			} 
		}

		if (SUMMARYLOGGER != null && summaryHandler != null) {
			SUMMARYLOGGER.addHandler(summaryHandler);
		}
		initialized = true;
	}

	public static void checkConfig() {
		Config agentConfig = NewRelic.getAgent().getConfig();
		CommunicationChannelConfig channelConfig = getConfig(agentConfig);
		if(channelConfig != null) {
			CommunicationChannelMonitor.detailed_enabled = channelConfig.isDetailedEnabled();
			CommunicationChannelMonitor.summary_enabled = channelConfig.isSummaryEnabled();
			if(currentChannelConfig == null) {
				currentChannelConfig = channelConfig;
				NewRelic.getAgent().getInsights().recordCustomEvent("CommunicationChannelConfig", currentChannelConfig.getCurrentSettings());
				initialized = false;
				init();
			} else {
				if(!channelConfig.equals(currentChannelConfig)) {
					currentChannelConfig = channelConfig;
					NewRelic.getAgent().getInsights().recordCustomEvent("CommunicationChannelConfig", currentChannelConfig.getCurrentSettings());
					initialized = false;
					init();
				}
			}
		}
	}
	
	public static CommunicationChannelConfig getConfig(Config agentConfig) {
		CommunicationChannelConfig channelConifg = new CommunicationChannelConfig();
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
		channelConifg.setDetailedEnabled(enabled);

		String summary = agentConfig.getValue(SUMMARYCHANNELLOGFILENAME);
		if(summary != null && !summary.isEmpty()) {
			channelConifg.setSummaryChannelLog(summary);
		}

		return channelConifg;

	}

	public void configChanged(String appName, Config agentConfig) {
		CommunicationChannelConfig channelConfig = getConfig(agentConfig);
		if(channelConfig != null) {
			CommunicationChannelMonitor.detailed_enabled = channelConfig.isDetailedEnabled();
			CommunicationChannelMonitor.summary_enabled = channelConfig.isSummaryEnabled();
			if(currentChannelConfig == null) {
				currentChannelConfig = channelConfig;
				NewRelic.getAgent().getInsights().recordCustomEvent("CommunicationChannelConfig", currentChannelConfig.getCurrentSettings());
				initialized = false;
				init();
			} else {
				if(!channelConfig.equals(currentChannelConfig)) {
					currentChannelConfig = channelConfig;
					NewRelic.getAgent().getInsights().recordCustomEvent("CommunicationChannelConfig", currentChannelConfig.getCurrentSettings());
					initialized = false;
					init();
				}
			}
		}
	}

}
