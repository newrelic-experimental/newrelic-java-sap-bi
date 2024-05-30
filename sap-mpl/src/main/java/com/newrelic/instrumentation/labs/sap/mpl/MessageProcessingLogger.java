package com.newrelic.instrumentation.labs.sap.mpl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.agent.HarvestListener;
import com.newrelic.agent.stats.StatsEngine;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.Insights;
import com.newrelic.api.agent.NewRelic;

public class MessageProcessingLogger implements HarvestListener {

	protected static final String MPL_LOG_NAME = "messageprocessing.log";
	public static boolean initialized = false;
	private static Logger GATEWAYLOGGER;
	protected static final String GATEWAYLOGFILENAME = "SAP.gatewaylog.mpllog_file_name";
	protected static final String GATEWAYLOGGINGROLLOVERINTERVAL = "SAP.gatewaylog.log_file_interval";
	protected static final String GATEWAYLOGGINGROLLOVERSIZE = "SAP.gatewaylog.log_size_limit";
	protected static final String GATEWAYLOGGINGROLLOVERSIZE2 = "SAP.gatewaylog.log_file_size";
	protected static final String GATEWAYLOGGINGMAXFILES = "SAP.gatewaylog.log_file_count";
	protected static final String GATEWAYLOGENABLED = "SAP.messageprocessinglog.enabled";
	private static final String LOGEVENTS = "SAP/MessageProcessingLogger/Events";
	
	private static MessageProcessingConfig currentMessageProcessingConfig = null;
	
	private static MessageProcessingLogger INSTANCE = null;
	
	private static NRLabsHandler handler;
	
	private static NRLabsTimerTask timerTask = null;
	private static Integer LoggingCount = 0;
	private static boolean enabled = true;
	
	private MessageProcessingLogger() {
		
	}
	
	protected static void checkConfig() {
		Config agentConfig = NewRelic.getAgent().getConfig();
		
		MessageProcessingConfig newConfig = getConfig(agentConfig);
		if(!newConfig.equals(currentMessageProcessingConfig)) {
			// reinitialize log
			currentMessageProcessingConfig = newConfig;
			init();
		}
	}
	
	protected MessageProcessingConfig getConfig() {
		if(currentMessageProcessingConfig != null) return currentMessageProcessingConfig;
		Config agentConfig = NewRelic.getAgent().getConfig();
		return getConfig(agentConfig);
	}
	
	public static MessageProcessingConfig getConfig(Config agentConfig) {
		MessageProcessingConfig messageProcessingConfig = new MessageProcessingConfig();
		Integer rolloverMinutes = agentConfig.getValue(GATEWAYLOGGINGROLLOVERINTERVAL);
		if(rolloverMinutes != null) {
			messageProcessingConfig.setRolloverMinutes(rolloverMinutes);
		}
		
		Integer maxFile = agentConfig.getValue(GATEWAYLOGGINGMAXFILES);
		if(maxFile != null) {
			messageProcessingConfig.setMaxLogFiles(maxFile);
		}

		String rolloverSize = agentConfig.getValue(GATEWAYLOGGINGROLLOVERSIZE);
		if(rolloverSize != null && !rolloverSize.isEmpty()) {
			messageProcessingConfig.setRolloverSize(rolloverSize);
		} else {

			rolloverSize = agentConfig.getValue(GATEWAYLOGGINGROLLOVERSIZE2);
			if(rolloverSize != null && !rolloverSize.isEmpty()) {
				messageProcessingConfig.setRolloverSize(rolloverSize);
			}
		}

		String filename = agentConfig.getValue(GATEWAYLOGFILENAME);
		if(filename != null && !filename.isEmpty()) {
			messageProcessingConfig.setMessageProcessingLog(filename);
		}

		boolean enabled = agentConfig.getValue(GATEWAYLOGENABLED, Boolean.TRUE);
		messageProcessingConfig.setMessageProcessing_enabled(enabled);
		

		return messageProcessingConfig;
	}
	
	public static void log(String message) {
		if(!enabled) return;
		
		if(!initialized) {
			init();
		}
		GATEWAYLOGGER.log(Level.INFO, message);
		LoggingCount++;
	}
	
	public static void init() {
		if(currentMessageProcessingConfig == null) {
			Config agentConfig = NewRelic.getAgent().getConfig();
			currentMessageProcessingConfig = getConfig(agentConfig);
		}
		
		enabled = currentMessageProcessingConfig.isMessageProcessing_enabled();
		
		if(INSTANCE == null) {
			INSTANCE = new MessageProcessingLogger();
		}
		
		if(timerTask == null) {
			timerTask = new NRLabsTimerTask();
			Timer timer = new Timer("MessageProcessingConfig");
			timer.scheduleAtFixedRate(timerTask, 2L * 60000L,  2L * 60000);
		}
		
		int rolloverMinutes = currentMessageProcessingConfig.getRolloverMinutes();
		if(rolloverMinutes < 1) {
			rolloverMinutes = 60;
		}
		
		String rolloverSize = currentMessageProcessingConfig.getRolloverSize();
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

		String messageProcessingFileName = currentMessageProcessingConfig.getMessageProcessingLog();
		File file = new File(messageProcessingFileName);
		File parent = file.getParentFile();
		
		if(!parent.exists()) {
			boolean result = parent.mkdirs();
			if(result) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Created directories needed for messageProcessing log files: {0})", messageProcessingFileName);
			} else {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to created directories needed for messageProcessing log files: {0})", messageProcessingFileName);			}
		}
		
		int maxFiles = currentMessageProcessingConfig.getMaxLogFiles();
		if(handler != null) {
			handler.flush();
			handler.close();
			if(GATEWAYLOGGER != null) {
				GATEWAYLOGGER.removeHandler(handler);
			}
			handler = null;
		}
		
		try {
			handler = new NRLabsHandler(messageProcessingFileName, rolloverMinutes, size, maxFiles);
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create NRHandler for messageProcessing log");
		}
		
		if(GATEWAYLOGGER == null) {
			GATEWAYLOGGER = Logger.getLogger("MessageProcessingLog");
		}
		
		GATEWAYLOGGER.setLevel(Level.INFO);
		GATEWAYLOGGER.setUseParentHandlers(false);
		
		if(GATEWAYLOGGER != null && handler != null) {
			GATEWAYLOGGER.addHandler(handler);
		}
		Map<String, Object> event = new HashMap<>();
		event.put("Initialized", new Date());
		event.put("GateLogFile",messageProcessingFileName);
		event.put("RolloverSize",rolloverSize);
		event.put("RolloverMinutes",rolloverMinutes);
		Insights insights = NewRelic.getAgent().getInsights();
		if(insights != null) {
			insights.recordCustomEvent("MessageProcessingLogInit", event);
		} else {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Constructed GATEWAYLOGGER using : {0}", event);
		}
		initialized = true;
	}

	@Override
	public void beforeHarvest(String appName, StatsEngine statsEngine) {
		synchronized(LoggingCount) {
			statsEngine.getStats(LOGEVENTS).recordDataPoint(LoggingCount);
			LoggingCount = 0;
		}
	}

	@Override
	public void afterHarvest(String appName) {
	}
}
