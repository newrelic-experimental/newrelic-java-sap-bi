package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.api.agent.NewRelic;

public class AttributeMonitorLogger {
	
	public static boolean initialized = false;
	private static Logger LOGGER = null;;
	private static NRLabsLoggerHandler handler = null;
	protected static boolean enabled = true;
	private static final Set<String> attributes = new LinkedHashSet<String>();
	
	public static void init() {
		
		if(initialized) return;
		
		long rolloverSize = 100L * 1024L * 1024L;
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File logDir = new File(newRelicDir,"adapterLogs");
		if(!logDir.exists()) {
			logDir.mkdirs();
		}
		String logfileName = "AttributeMonitor.log";
		int maxFiles = 3;
		File logFile = new File(logDir, logfileName);
		
		if(logFile.exists()) {
			logFile.delete();
		}
		
		if(handler == null) {
			try {
				handler = new NRLabsLoggerHandler(logFile.getPath(), 0, rolloverSize, maxFiles);
				handler.setFormatter(new NRLabsLogFormatter());
			} catch (SecurityException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create log for {0}",logFile.getPath());
			} catch (IOException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create log for {0}",logFile.getPath());
			}
		}
		
		if(LOGGER == null) {
			LOGGER = Logger.getLogger("AttributeMonitorLog");
			LOGGER.addHandler(handler);
		}
		
		initialized = true;
	}
	
	public static synchronized void addAttribute(String attribute, String source) {
		if(!enabled) return;
		
		if(!initialized) {
			init();
		}
		String key = source + "-" + attribute;
		if(!attributes.contains(key)) {
			attributes.add(key);
			logMessage(key);
		}
	}

	public static void logMessage(String message) {
		if(message != null && !message.isEmpty()) {
			LOGGER.log(Level.INFO, message);
		}
	}
		
}
