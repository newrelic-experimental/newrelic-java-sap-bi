package com.newrelic.instrumentation.labs.sap.payload;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.api.agent.NewRelic;


public class PayloadLogger {

	public static boolean initialized = false;
	private static Logger LOGGER = null;;
	private static NRLabsHandler handler = null;
	protected static boolean enabled = true;

	public static void init() {
		
		if(initialized) return;
		
		long rolloverSize = 100L * 1024L * 1024L;
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File logDir = new File(newRelicDir,"payloadLogs");
		if(!logDir.exists()) {
			logDir.mkdirs();
		}
		String logfileName = "Payload.log";
		int maxFiles = 3;
		File logFile = new File(logDir, logfileName);
		
		if(handler == null) {
			try {
				handler = new NRLabsHandler(logFile.getPath(), 0, rolloverSize, maxFiles);
				handler.setFormatter(new NRLabsFormatter());
			} catch (SecurityException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create log for {0}",logFile.getPath());
			} catch (IOException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create log for {0}",logFile.getPath());
			}
		}
		
		if(LOGGER == null) {
			LOGGER = Logger.getLogger("PayloadLog");
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
}
