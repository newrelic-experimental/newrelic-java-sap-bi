package com.newrelic.instrumentation.labs.sap.engine;

import java.io.File;
import java.util.logging.Level;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.api.agent.NewRelic;

public class EngineLoggingConfig {
	
	private String adapterLog = null;
	private int maxLogFiles = 3;
	private String rolloverSize = "250K";
	private int rolloverMinutes = 120;
	
	public EngineLoggingConfig() {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File logDirectory = new File(newRelicDir, "attributeLogging");
		boolean logDirExists = true;
		if(!logDirectory.exists()) {
			logDirectory.mkdir();
		} else if(!logDirectory.isDirectory()) {
			boolean deleted = logDirectory.delete();
			if(!deleted) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "{0} is not a directory and unable to delete", logDirectory);
				logDirExists = false;
			} else {
				logDirectory.mkdir();
			}
		}
		if(logDirExists) {
			File logFile = new File(logDirectory, EngineLogger.LOGFILENAME);
			adapterLog = logFile.getAbsolutePath();
		} else {
			File logFile = new File(newRelicDir, EngineLogger.LOGFILENAME);
			adapterLog = logFile.getAbsolutePath();
		}
	}

	public String getAdapterLog() {
		return adapterLog;
	}

	public void setAdapterLog(String adapterLog) {
		this.adapterLog = adapterLog;
	}

	public int getMaxLogFiles() {
		return maxLogFiles;
	}

	public void setMaxLogFiles(int maxLogFiles) {
		this.maxLogFiles = maxLogFiles;
	}

	public String getRolloverSize() {
		return rolloverSize;
	}

	public void setRolloverSize(String rolloverSize) {
		this.rolloverSize = rolloverSize;
	}

	public int getRolloverMinutes() {
		return rolloverMinutes;
	}

	public void setRolloverMinutes(int rolloverMinutes) {
		this.rolloverMinutes = rolloverMinutes;
	}
	
	
}
