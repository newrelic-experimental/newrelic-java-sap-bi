package com.newrelic.instrumentation.sap.gateway;

import java.io.File;
import java.util.HashMap;

import com.newrelic.agent.config.ConfigFileHelper;

public class GatewayMessageConfig {

	private String messageFile = null;
	private int maxLogFiles = 3;
	private String rolloverSize = "100K";
	private int rolloverMinutes = 0;
	private boolean enabled = true;
	private boolean simulate = false;
	
	public GatewayMessageConfig() {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File message_file = new File(newRelicDir, GatewayLogger.DEFAULT_MESSAGE_FILE_NAME);
		messageFile = message_file.getAbsolutePath();
	}

	
	public boolean isSimulate() {
		return simulate;
	}


	public void setSimulate(boolean simulate) {
		this.simulate = simulate;
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
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public HashMap<String, Object> getCurrentSettings() {
		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("MessageFileName", messageFile);
		attributes.put("MaxMessageLogFiles", maxLogFiles);
		attributes.put("RollOverSize", rolloverSize);
		attributes.put("RollOverMinutes", rolloverMinutes);
		attributes.put("ConfigurationType", "MessageLog");
		attributes.put("Enabled", enabled);
		attributes.put("Simulate", simulate);
		return attributes;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		
		if(!(obj instanceof GatewayMessageConfig)) {
			return false;
		}
		GatewayMessageConfig newConfig = (GatewayMessageConfig)obj;
		
		return newConfig.enabled == enabled && newConfig.maxLogFiles == maxLogFiles && newConfig.messageFile.equals(messageFile) && newConfig.rolloverSize.equals(rolloverSize);
	}
	
	
}
