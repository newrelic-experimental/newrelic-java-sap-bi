package com.newrelic.instrumentation.labs.sap.mpl;

import java.io.File;
import java.util.HashMap;

import com.newrelic.agent.config.ConfigFileHelper;

public class MessageProcessingConfig {

	private String messageProcessingLog = null;
	private int maxLogFiles = 3;
	private String rolloverSize = "100K";
	private int rolloverMinutes = 0;
	private boolean messageProcessing_enabled = true;

	public MessageProcessingConfig() {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File message_file = new File(newRelicDir, MessageProcessingLogger.MPL_LOG_NAME);
		messageProcessingLog = message_file.getAbsolutePath();	
	}

	public String getMessageProcessingLog() {
		return messageProcessingLog;
	}

	public void setMessageProcessingLog(String messageProcessingLog) {
		this.messageProcessingLog = messageProcessingLog;
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
		
		if(!(obj instanceof MessageProcessingConfig)) return false;
		
		MessageProcessingConfig newConfig = (MessageProcessingConfig)obj;
		
 		return newConfig.messageProcessingLog.equals(messageProcessingLog) && newConfig.maxLogFiles == maxLogFiles && newConfig.rolloverMinutes == rolloverMinutes && newConfig.rolloverSize.equals(rolloverSize) && newConfig.messageProcessing_enabled == messageProcessing_enabled;
	}
	
	public boolean isMessageProcessing_enabled() {
		return messageProcessing_enabled;
	}

	public void setMessageProcessing_enabled(boolean messageProcessing_enabled) {
		this.messageProcessing_enabled = messageProcessing_enabled;
	}

	public HashMap<String, Object> getCurrentSettings() {
		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("MessageProcessingFileName", messageProcessingLog);
		attributes.put("MaxMessageLogFiles", maxLogFiles);
		attributes.put("RollOverSize", rolloverSize);
		attributes.put("RollOverMinutes", rolloverMinutes);
		attributes.put("ConfigurationType", "MessageLog");
		attributes.put("Enabled", messageProcessing_enabled);
		
		return attributes;
	}

}
