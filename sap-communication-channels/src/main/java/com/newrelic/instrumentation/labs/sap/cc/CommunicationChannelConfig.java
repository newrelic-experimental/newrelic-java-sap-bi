package com.newrelic.instrumentation.labs.sap.cc;

import java.io.File;
import java.util.HashMap;

import com.newrelic.agent.config.ConfigFileHelper;

public class CommunicationChannelConfig {

	private String channelLog = null;
	private int maxLogFiles = 3;
	private String rolloverSize = "100K";
	private int rolloverMinutes = 0;
	private boolean detailed_enabled = true;
	private boolean summary_enabled = true;
	private String summaryChannelLog = null;

	public CommunicationChannelConfig() {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File message_file = new File(newRelicDir, CommunicationChannelMonitor.log_file_name);
		File summary_file = new File(newRelicDir,CommunicationChannelMonitor.summary_log_file_name);
		channelLog = message_file.getAbsolutePath();	
		summaryChannelLog = summary_file.getAbsolutePath();
	}

	public String getChannelLog() {
		return channelLog;
	}

	public void setChannelLog(String channelLog) {
		this.channelLog = channelLog;
	}

	public String getSummaryChannelLog() {
		return summaryChannelLog;
	}

	public void setSummaryChannelLog(String summaryChannelLog) {
		this.summaryChannelLog = summaryChannelLog;
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

	public boolean isDetailedEnabled() {
		return detailed_enabled;
	}	

	public boolean isSummaryEnabled() {
		return summary_enabled;
	}	

	public void setDetailedEnabled(boolean enabled) {
		this.detailed_enabled = enabled;
	}

	public void setSummaryEnabled(boolean enabled) {
		this.summary_enabled = enabled;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		
		if(!(obj instanceof CommunicationChannelConfig)) return false;
		
		CommunicationChannelConfig newConfig = (CommunicationChannelConfig)obj;
		
 		return newConfig.channelLog.equals(channelLog) && newConfig.maxLogFiles == maxLogFiles && newConfig.rolloverMinutes == rolloverMinutes && newConfig.rolloverSize.equals(rolloverSize) && newConfig.detailed_enabled == detailed_enabled && newConfig.summaryChannelLog.equals(summaryChannelLog);
	}
	
	public HashMap<String, Object> getCurrentSettings() {
		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("ChannelFileName", channelLog);
		attributes.put("MaxMessageLogFiles", maxLogFiles);
		attributes.put("RollOverSize", rolloverSize);
		attributes.put("RollOverMinutes", rolloverMinutes);
		attributes.put("ConfigurationType", "MessageLog");
		attributes.put("Enabled", detailed_enabled);
		attributes.put("SummaryFileName", summaryChannelLog);
		
		return attributes;
	}

}
