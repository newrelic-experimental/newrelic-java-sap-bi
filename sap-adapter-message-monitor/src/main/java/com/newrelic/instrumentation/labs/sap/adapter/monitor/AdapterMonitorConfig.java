package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import com.newrelic.agent.config.ConfigFileHelper;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;

public class AdapterMonitorConfig {

	private String attributeLog = null;
	private int maxLogFiles = 3;
	private String rolloverSize = "100K";
	private int rolloverMinutes = 0;
	private boolean enabled = true;
	private Level level = Level.INFO;

	public AdapterMonitorConfig() {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File log_file = new File(newRelicDir, AdapterMonitorLogger.log_file_name);
		attributeLog = log_file.getAbsolutePath();
	}
	
	public String getAdapterMonitorLog() {
		return attributeLog;
	}

	public void setAdapterMonitorLog(String attributeLog) {
		this.attributeLog = attributeLog;
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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		
		if(!(obj instanceof AdapterMonitorConfig)) return false;
		
		AdapterMonitorConfig newConfig = (AdapterMonitorConfig)obj;
				
		return newConfig.enabled == enabled && newConfig.attributeLog.equals(attributeLog) && newConfig.maxLogFiles == maxLogFiles && newConfig.rolloverMinutes == rolloverMinutes && newConfig.rolloverSize.equals(rolloverSize) && newConfig.level.intValue()	== level.intValue();
	}
	
	public HashMap<String, Object> getCurrentSettings() {
		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("AttributesFileName", attributeLog);
		attributes.put("MaxMessageLogFiles", maxLogFiles);
		attributes.put("RollOverSize", rolloverSize);
		attributes.put("RollOverMinutes", rolloverMinutes);
		attributes.put("ConfigurationType", "MessageLog");
		attributes.put("Enabled", enabled);
		attributes.put("Level", level.toString());
		
		return attributes;
	}

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
