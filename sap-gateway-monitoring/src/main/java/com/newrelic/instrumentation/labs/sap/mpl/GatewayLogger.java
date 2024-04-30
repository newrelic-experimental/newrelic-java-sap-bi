package com.newrelic.instrumentation.labs.sap.mpl;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.agent.HarvestListener;
import com.newrelic.agent.stats.StatsEngine;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;

public class GatewayLogger implements HarvestListener {

	protected static final String MPL_LOG_NAME = "gateway.log";
	public static boolean initialized = false;
	private static Logger GATEWAYLOGGER;
	protected static final String GATEWAYLOGFILENAME = "SAP.gatewaylog.log_file_name";
	protected static final String GATEWAYLOGGINGROLLOVERINTERVAL = "SAP.gatewaylog.log_file_interval";
	protected static final String GATEWAYLOGGINGROLLOVERSIZE = "SAP.gatewaylog.log_size_limit";
	protected static final String GATEWAYLOGGINGROLLOVERSIZE2 = "SAP.gatewaylog.log_file_size";
	protected static final String GATEWAYLOGGINGMAXFILES = "SAP.gatewaylog.log_file_count";
	protected static final String CHANNELSLOGENABLED = "SAP.gatewaylog.enabled";
	private static final String LOGEVENTS = "SAP/GatewayLogger/Events";
	
	private static GatewayConfig currentGatewayConfig = null;
	
	private static GatewayLogger INSTANCE = null;
	
	private static NRLabsHandler handler;
	
	private static NRLabsTimerTask timerTask = null;
	private static Integer LoggingCount = 0;
	
	private GatewayLogger() {
		
	}
	
	protected static void checkConfig() {
		Config agentConfig = NewRelic.getAgent().getConfig();
		
		GatewayConfig newConfig = getConfig(agentConfig);
		if(newConfig != currentGatewayConfig) {
			// reinitialize log
			currentGatewayConfig = newConfig;
			init();
		}
	}
	
	protected GatewayConfig getConfig() {
		if(currentGatewayConfig != null) return currentGatewayConfig;
		Config agentConfig = NewRelic.getAgent().getConfig();
		return getConfig(agentConfig);
	}
	
	public static GatewayConfig getConfig(Config agentConfig) {
		GatewayConfig gatewayConfig = new GatewayConfig();
		Integer rolloverMinutes = agentConfig.getValue(GATEWAYLOGGINGROLLOVERINTERVAL);
		if(rolloverMinutes != null) {
			gatewayConfig.setRolloverMinutes(rolloverMinutes);
		}
		
		Integer maxFile = agentConfig.getValue(GATEWAYLOGGINGMAXFILES);
		if(maxFile != null) {
			gatewayConfig.setMaxLogFiles(maxFile);
		}

		String rolloverSize = agentConfig.getValue(GATEWAYLOGGINGROLLOVERSIZE);
		if(rolloverSize != null && !rolloverSize.isEmpty()) {
			gatewayConfig.setRolloverSize(rolloverSize);
		} else {

			rolloverSize = agentConfig.getValue(GATEWAYLOGGINGROLLOVERSIZE2);
			if(rolloverSize != null && !rolloverSize.isEmpty()) {
				gatewayConfig.setRolloverSize(rolloverSize);
			}
		}

		String filename = agentConfig.getValue(GATEWAYLOGFILENAME);
		if(filename != null && !filename.isEmpty()) {
			gatewayConfig.setGatewayLog(filename);
		}

		boolean enabled = agentConfig.getValue(CHANNELSLOGENABLED, Boolean.TRUE);
		gatewayConfig.setGateway_enabled(enabled);
		

		return gatewayConfig;
	}
	
	public static void log(String message) {
		if(!initialized) {
			init();
		}
		GATEWAYLOGGER.log(Level.INFO, message);
		LoggingCount++;
	}
	
	public static void init() {
		if(INSTANCE == null) {
			INSTANCE = new GatewayLogger();
		}
		
		if(timerTask == null) {
			timerTask = new NRLabsTimerTask();
			Timer timer = new Timer("GatewayConfig");
			timer.scheduleAtFixedRate(timerTask, 2L * 60000L,  2L * 60000);
		}
		
		if(currentGatewayConfig == null) {
			Config agentConfig = NewRelic.getAgent().getConfig();
			currentGatewayConfig = getConfig(agentConfig);
		}
		
		int rolloverMinutes = currentGatewayConfig.getRolloverMinutes();
		if(rolloverMinutes < 1) {
			rolloverMinutes = 60;
		}
		
		String rolloverSize = currentGatewayConfig.getRolloverSize();
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

		String gatewayFileName = currentGatewayConfig.getGatewayLog();;
		File file = new File(gatewayFileName);
		File parent = file.getParentFile();
		
		if(!parent.exists()) {
			boolean result = parent.mkdirs();
			if(result) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Created directories needed for gateway log files: {0})", gatewayFileName);
			} else {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to created directories needed for gateway log files: {0})", gatewayFileName);			}
		}
		
		int maxFiles = currentGatewayConfig.getMaxLogFiles();
		if(handler != null) {
			handler.flush();
			handler.close();
			if(GATEWAYLOGGER != null) {
				GATEWAYLOGGER.removeHandler(handler);
			}
			handler = null;
		}
		
		try {
			handler = new NRLabsHandler(gatewayFileName, rolloverMinutes, size, maxFiles);
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create NRHandler for gateway log");
		}
		
		if(GATEWAYLOGGER == null) {
			GATEWAYLOGGER = Logger.getLogger("GatewayLog");
		}
		
		if(GATEWAYLOGGER != null && handler != null) {
			GATEWAYLOGGER.addHandler(handler);
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
