package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.io.File;
import java.util.HashMap;

import com.newrelic.agent.config.ConfigFileHelper;

public class MessageLoggingConfig {

	private String adapterLog = null;
	private int maxLogFiles = 3;
	private String rolloverSize = "100K";
	private int rolloverMinutes = 0;
	private boolean enabled = true;
	private long frequency = 3L;
	private long delay = 1L;

	public MessageLoggingConfig() {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File log_file = new File(newRelicDir, AdapterMessageLogger.log_file_name);
		adapterLog = log_file.getAbsolutePath();
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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean frequencyChanged(Object obj) {
		if(obj == null) return false;
		
		if(!(obj instanceof MessageLoggingConfig)) return false;
		
		MessageLoggingConfig newConfig = (MessageLoggingConfig)obj;
		
		return newConfig.frequency != frequency;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		
		if(!(obj instanceof MessageLoggingConfig)) return false;
		
		MessageLoggingConfig newConfig = (MessageLoggingConfig)obj;
				
		return newConfig.enabled == enabled && newConfig.adapterLog.equals(adapterLog) && newConfig.maxLogFiles == maxLogFiles && newConfig.rolloverMinutes == rolloverMinutes && newConfig.rolloverSize.equals(rolloverSize); 
	}
	
	public HashMap<String, Object> getCurrentSettings() {
		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("AdapterFileName", adapterLog);
		attributes.put("MaxMessageLogFiles", maxLogFiles);
		attributes.put("RollOverSize", rolloverSize);
		attributes.put("RollOverMinutes", rolloverMinutes);
		attributes.put("ConfigurationType", "MessageLog");
		attributes.put("Enabled", enabled);
		
		return attributes;
	}

	public long getFrequency() {
		return frequency;
	}

	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

}
