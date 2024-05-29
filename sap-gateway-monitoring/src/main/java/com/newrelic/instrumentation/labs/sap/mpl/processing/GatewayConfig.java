package com.newrelic.instrumentation.labs.sap.mpl.processing;

import java.io.File;
import java.util.HashMap;

import com.newrelic.agent.config.ConfigFileHelper;

public class GatewayConfig {

	private String gatewayLog = null;
	private int maxLogFiles = 3;
	private String rolloverSize = "100K";
	private int rolloverMinutes = 0;
	private boolean gateway_enabled = true;

	public GatewayConfig() {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File message_file = new File(newRelicDir, GatewayLogger.MPL_LOG_NAME);
		gatewayLog = message_file.getAbsolutePath();	
	}

	public String getGatewayLog() {
		return gatewayLog;
	}

	public void setGatewayLog(String gatewayLog) {
		this.gatewayLog = gatewayLog;
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
		
		if(!(obj instanceof GatewayConfig)) return false;
		
		GatewayConfig newConfig = (GatewayConfig)obj;
 		return newConfig.gatewayLog.equals(gatewayLog) && newConfig.maxLogFiles == maxLogFiles && newConfig.rolloverMinutes == rolloverMinutes && newConfig.rolloverSize.equals(rolloverSize) && newConfig.gateway_enabled == gateway_enabled;
	}
	
	public boolean isGateway_enabled() {
		return gateway_enabled;
	}

	public void setGateway_enabled(boolean gateway_enabled) {
		this.gateway_enabled = gateway_enabled;
	}

	public HashMap<String, Object> getCurrentSettings() {
		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("GatewayFileName", gatewayLog);
		attributes.put("MaxMessageLogFiles", maxLogFiles);
		attributes.put("RollOverSize", rolloverSize);
		attributes.put("RollOverMinutes", rolloverMinutes);
		attributes.put("ConfigurationType", "MessageLog");
		attributes.put("Enabled", gateway_enabled);
		
		return attributes;
	}

}
