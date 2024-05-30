package com.newrelic.instrumentation.labs.sap.tracemessage.processing;

import java.io.File;
import java.util.HashMap;

import com.newrelic.agent.config.ConfigFileHelper;

public class TraceLogConfig {

	private String traceMessageLog = null;
	private int maxLogFiles = 3;
	private String rolloverSize = "100K";
	private int rolloverMinutes = 0;
	private boolean traceLog_enabled = true;

	public TraceLogConfig() {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File message_file = new File(newRelicDir, TraceMessageLogger.TRACEMESSAGE_LOG_NAME);
		traceMessageLog = message_file.getAbsolutePath();	
	}

	public String getTraceMessageLog() {
		return traceMessageLog;
	}

	public void setTraceMessageLog(String traceMsgLog) {
		this.traceMessageLog = traceMsgLog;
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

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		
		if(!(obj instanceof TraceLogConfig)) return false;
		
		TraceLogConfig newConfig = (TraceLogConfig)obj;
 		return newConfig.traceMessageLog.equals(traceMessageLog) && newConfig.maxLogFiles == maxLogFiles && newConfig.rolloverMinutes == rolloverMinutes && newConfig.rolloverSize.equals(rolloverSize) && newConfig.traceLog_enabled == traceLog_enabled;
	}
	
	public boolean isTracelog_enabled() {
		return traceLog_enabled;
	}

	public void setTracelog_enabled(boolean tracelog_enabled) {
		this.traceLog_enabled = tracelog_enabled;
	}

	public HashMap<String, Object> getCurrentSettings() {
		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("TracelogFileName", traceMessageLog);
		attributes.put("MaxMessageLogFiles", maxLogFiles);
		attributes.put("RollOverSize", rolloverSize);
		attributes.put("RollOverMinutes", rolloverMinutes);
		attributes.put("ConfigurationType", "MessageLog");
		attributes.put("Enabled", traceLog_enabled);
		
		return attributes;
	}

}
