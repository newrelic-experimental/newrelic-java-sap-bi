package com.nr.instrumentation.sap.auditlogging;

import java.io.File;
import java.util.HashMap;

import com.newrelic.agent.config.ConfigFileHelper;

public class MessageConfig {

	private String messageFile = null;
	private int maxLogFiles = 3;
	private String rolloverSize = "10K";
	private int rolloverMinutes = 0;
	
	public MessageConfig() {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File message_file = new File(newRelicDir, Logger.DEFAULT_MESSAGE_FILE_NAME);
		messageFile = message_file.getName();
	}

	public String getMessageFile() {
		return messageFile;
	}

	public void setMessageFile(String messageFile) {
		this.messageFile = messageFile;
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
	
	public HashMap<String, Object> getCurrentSettings() {
		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("MessageFileName", messageFile);
		attributes.put("MaxMessageLogFiles", maxLogFiles);
		attributes.put("RollOverSize", rolloverSize);
		attributes.put("RollOverMinutes", rolloverMinutes);
		attributes.put("ConfigurationType", "MessageLog");
		
		return attributes;
	}
	
}
