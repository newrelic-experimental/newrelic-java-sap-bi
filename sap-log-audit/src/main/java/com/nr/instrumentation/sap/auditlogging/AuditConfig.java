package com.nr.instrumentation.sap.auditlogging;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import com.newrelic.agent.config.ConfigFileHelper;

public class AuditConfig {

	private String auditFile = null;
	private int rolloverInterval = 0;
	private String rolloverSize = "100K";
	private int maxFiles = 3;
	private HashSet<String> ignores = new HashSet<>();
	private boolean enabled = true;
	
	protected AuditConfig() {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File audit_file = new File(newRelicDir, Logger.DEFAULT_AUDIT_FILE_NAME);
		auditFile = audit_file.getName();
	}

	public String getAuditFile() {
		return auditFile;
	}

	public void setAuditFile(String auditFile) {
		this.auditFile = auditFile;
	}
	
	public void clearIgnores() {
		ignores.clear();
	}
	
	public void addIgnore(String toIgnore) {
		ignores.add(toIgnore);
	}
	
	public boolean removeIgnore(String ignore) {
		return ignores.remove(ignore);
	}

	public int getRolloverInterval() {
		return rolloverInterval;
	}

	public void setRolloverInterval(int rolloverInterval) {
		this.rolloverInterval = rolloverInterval;
	}

	public String getRolloverSize() {
		return rolloverSize;
	}

	public void setRolloverSize(String rolloverSize) {
		this.rolloverSize = rolloverSize;
	}

	public int getMaxFiles() {
		return maxFiles;
	}

	public void setMaxFiles(int maxFiles) {
		this.maxFiles = maxFiles;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean equals(Object object) {
		if(object == null) return false;
		
		if(object instanceof AuditConfig) {
			AuditConfig other = (AuditConfig)object;
			return auditFile.equals(other.auditFile) & ignores.equals(other.ignores) & maxFiles == other.maxFiles 
					& rolloverInterval == other.rolloverInterval & rolloverSize.equals(other.rolloverSize);
		}
		return super.equals(object);
	}
	
	public HashMap<String, Object> getCurrentSettings() {
		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("AuditLogFileName", auditFile);
		attributes.put("MaxAuditLogFiles", maxFiles);
		attributes.put("RollOverSize", rolloverSize);
		attributes.put("RollOverMinutes", rolloverInterval);
		attributes.put("Enabled", enabled);
		
		return attributes;
	}

	
}
