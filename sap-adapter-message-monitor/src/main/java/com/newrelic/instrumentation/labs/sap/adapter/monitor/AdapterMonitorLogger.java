package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.api.agent.NewRelic;

public class AdapterMonitorLogger {
	
	public static boolean initialized = false;
	private static Logger LOGGER = null;;
	private static NRLabsLoggerHandler handler = null;
	protected static boolean enabled = true;
	
	public static void init() {
		
		if(initialized) return;
		
		long rolloverSize = 100L * 1024L * 1024L;
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File logDir = new File(newRelicDir,"adapterLogs");
		if(!logDir.exists()) {
			logDir.mkdirs();
		}
		String logfileName = "AdapterMonitor.log";
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
			LOGGER = Logger.getLogger("AdapterMonitorLog");
			LOGGER.addHandler(handler);
		}
		
		initialized = true;
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
