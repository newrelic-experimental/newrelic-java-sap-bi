package com.newrelic.instrumentation.labs.sap.channel.monitoring;

import java.io.File;
import java.util.HashMap;

import com.newrelic.agent.config.ConfigFileHelper;

public class ChannelMonitoringConfig {

	private String channelLog = null;
	private int maxLogFiles = 3;
	private String rolloverSize = "100K";
	private int rolloverMinutes = 0;
	private boolean enabled = true;
	private String channelStateLog = null;
	private int collection_period = 2;

	public ChannelMonitoringConfig() {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File message_file = new File(newRelicDir, ChannelMonitoringLogger.log_file_name);
		channelLog = message_file.getAbsolutePath();	
		File state_file = new File(newRelicDir, ChannelMonitoringLogger.state_log_name);
		channelStateLog = state_file.getAbsolutePath();
	}

	public String getChannelLog() {
		return channelLog;
	}

	public void setChannelLog(String channelLog) {
		this.channelLog = channelLog;
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


	public int getCollection_period() {
		return collection_period;
	}

	public void setCollection_period(int collection_period) {
		this.collection_period = collection_period;
	}

	public String getChannelStateLog() {
		return channelStateLog;
	}

	public void setChannelStateLog(String chanelStateLog) {
		this.channelStateLog = chanelStateLog;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		
		if(!(obj instanceof ChannelMonitoringConfig)) return false;
		
		ChannelMonitoringConfig newConfig = (ChannelMonitoringConfig)obj;
		
 		return newConfig.channelLog.equals(channelLog) && newConfig.maxLogFiles == maxLogFiles && newConfig.rolloverMinutes == rolloverMinutes && newConfig.rolloverSize.equals(rolloverSize) && newConfig.enabled == enabled && newConfig.collection_period == collection_period;
	}
	
	public HashMap<String, Object> getCurrentSettings() {
		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("ChannelFileName", channelLog);
		attributes.put("MaxMessageLogFiles", maxLogFiles);
		attributes.put("RollOverSize", rolloverSize);
		attributes.put("RollOverMinutes", rolloverMinutes);
		attributes.put("ConfigurationType", "MessageLog");
		attributes.put("ChannelStateFileName", channelStateLog);
		attributes.put("Enabled", enabled);
		attributes.put("CollectionPeriod", collection_period);
		
		return attributes;
	}

	public boolean isIntervalChangeOnly(ChannelMonitoringConfig newConfig ) {
 		return newConfig.channelLog.equals(channelLog) && newConfig.maxLogFiles == maxLogFiles && newConfig.rolloverMinutes == rolloverMinutes && newConfig.rolloverSize.equals(rolloverSize) && newConfig.enabled == enabled && newConfig.collection_period != collection_period;
	}
}
