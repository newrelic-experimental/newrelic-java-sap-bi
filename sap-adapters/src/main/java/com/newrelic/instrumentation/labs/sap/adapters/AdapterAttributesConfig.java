package com.newrelic.instrumentation.labs.sap.adapters;

import java.io.File;

import com.newrelic.agent.config.ConfigFileHelper;

public class AdapterAttributesConfig {

	private String adapterLog = null;
	private int maxLogFiles = 3;
	private String rolloverSize = "100K";
	private int rolloverMinutes = 0;
	private boolean adapterLog_enabled = true;
	
	public AdapterAttributesConfig() {
		String logFileName = AdapterAttributesLogger.ADAPTER_LOG_NAME;
		File newrelicDir = ConfigFileHelper.getNewRelicDirectory();
		String logDir = AdapterAttributesLogger.ADAPTER_LOG_DIR;
		File logDirFile = new File(newrelicDir, logDir);
		adapterLog = new File(logDirFile,logFileName).getAbsolutePath();
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

	public boolean isAdapterLog_enabled() {
		return adapterLog_enabled;
	}

	public void setAdapterLog_enabled(boolean adapterLog_enabled) {
		this.adapterLog_enabled = adapterLog_enabled;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(!(obj instanceof AdapterAttributesConfig)) return false;
		
		AdapterAttributesConfig newConfig = (AdapterAttributesConfig)obj;
		
		return newConfig.adapterLog.equals(adapterLog) && newConfig.adapterLog_enabled == adapterLog_enabled && newConfig.maxLogFiles == maxLogFiles && newConfig.rolloverMinutes == rolloverMinutes && newConfig.rolloverSize.equals(rolloverSize);
	}

	@Override
	public String toString() {
		return "AdapterAttributesConfig [adapterLog=" + adapterLog + ", maxLogFiles=" + maxLogFiles + ", rolloverSize="
				+ rolloverSize + ", rolloverMinutes=" + rolloverMinutes + ", adapterLog_enabled=" + adapterLog_enabled
				+ "]";
	}	
	
}
