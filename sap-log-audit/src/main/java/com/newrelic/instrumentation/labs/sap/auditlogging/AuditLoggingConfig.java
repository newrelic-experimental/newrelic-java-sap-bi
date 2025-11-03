package com.newrelic.instrumentation.labs.sap.auditlogging;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import com.newrelic.agent.config.ConfigFileHelper;

/**
 * Configuration class for SAP Audit Logging
 * Based on the modern pattern from ChannelMonitoringConfig
 * 
 * @author gsidhwani
 */
public class AuditLoggingConfig {

    private String auditLogFile = null;
    private int maxAuditLogFiles = 3;
    private String auditRolloverSize = "100M";
    private int auditRolloverMinutes = 0;
    private boolean auditEnabled = true;
    private HashSet<String> auditIgnores = new HashSet<>();

    public static final String DEFAULT_AUDIT_FILE_NAME = "audit.log";

    public AuditLoggingConfig() {
        File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
        File auditFile = new File(newRelicDir, DEFAULT_AUDIT_FILE_NAME);
        auditLogFile = auditFile.getAbsolutePath();
    }

    public AuditLoggingConfig(com.newrelic.api.agent.Config agentConfig) {
        this(); // Set defaults first
        
        // Read configuration values directly using existing SAP config structure
        Object rolloverMinutes = agentConfig.getValue("SAP.auditlog.log_file_interval");
        if(rolloverMinutes != null) {
            setAuditRolloverMinutes(Integer.parseInt(rolloverMinutes.toString()));
        }

        Object maxFiles = agentConfig.getValue("SAP.auditlog.log_file_count");
        if(maxFiles != null) {
            setMaxAuditLogFiles(Integer.parseInt(maxFiles.toString()));
        }

        Object rolloverSize = agentConfig.getValue("SAP.auditlog.log_size_limit");
        if(rolloverSize != null && !rolloverSize.toString().isEmpty()) {
            setAuditRolloverSize(rolloverSize.toString());
        }

        Object filename = agentConfig.getValue("SAP.auditlog.log_file_name");
        if(filename != null && !filename.toString().isEmpty()) {
            setAuditLogFile(filename.toString());
        }

        Object enabled = agentConfig.getValue("SAP.auditlog.enabled");
        if(enabled != null) {
            setAuditEnabled(Boolean.parseBoolean(enabled.toString()));
        }

        Object ignores = agentConfig.getValue("SAP.auditlog.ignores");
        if(ignores != null && !ignores.toString().isEmpty()) {
            setAuditIgnoresFromString(ignores.toString());
        }
    }

    public String getAuditLogFile() {
        return auditLogFile;
    }

    public void setAuditLogFile(String auditLogFile) {
        this.auditLogFile = auditLogFile;
    }

    public int getMaxAuditLogFiles() {
        return maxAuditLogFiles;
    }

    public void setMaxAuditLogFiles(int maxAuditLogFiles) {
        this.maxAuditLogFiles = maxAuditLogFiles;
    }

    public String getAuditRolloverSize() {
        return auditRolloverSize;
    }

    public void setAuditRolloverSize(String auditRolloverSize) {
        this.auditRolloverSize = auditRolloverSize;
    }

    public int getAuditRolloverMinutes() {
        return auditRolloverMinutes;
    }

    public void setAuditRolloverMinutes(int auditRolloverMinutes) {
        this.auditRolloverMinutes = auditRolloverMinutes;
    }

    public boolean isAuditEnabled() {
        return auditEnabled;
    }

    public void setAuditEnabled(boolean auditEnabled) {
        this.auditEnabled = auditEnabled;
    }

    public HashSet<String> getAuditIgnores() {
        return auditIgnores;
    }

    public void setAuditIgnores(HashSet<String> auditIgnores) {
        this.auditIgnores = auditIgnores;
    }

    public void addAuditIgnore(String ignore) {
        this.auditIgnores.add(ignore);
    }

    public void clearAuditIgnores() {
        this.auditIgnores.clear();
    }

    public void setAuditIgnoresFromString(String ignoresString) {
        auditIgnores.clear();
        if (ignoresString != null && !ignoresString.isEmpty()) {
            StringTokenizer st = new StringTokenizer(ignoresString, ",");
            while (st.hasMoreTokens()) {
                String token = st.nextToken().trim();
                if (!token.isEmpty()) {
                    auditIgnores.add(token);
                }
            }
        }
    }

    public boolean isIgnored(String textKey) {
        if (textKey == null || textKey.isEmpty()) {
            return false;
        }

        for (String ignoreText : auditIgnores) {
            if (textKey.contains(ignoreText)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        
        if (!(obj instanceof AuditLoggingConfig)) return false;
        
        AuditLoggingConfig newConfig = (AuditLoggingConfig) obj;
        
        return newConfig.auditLogFile.equals(auditLogFile) && 
               newConfig.maxAuditLogFiles == maxAuditLogFiles && 
               newConfig.auditRolloverMinutes == auditRolloverMinutes && 
               newConfig.auditRolloverSize.equals(auditRolloverSize) && 
               newConfig.auditEnabled == auditEnabled &&
               newConfig.auditIgnores.equals(auditIgnores);
    }

    public HashMap<String, Object> getCurrentSettings() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("AuditLogFileName", auditLogFile);
        attributes.put("MaxAuditLogFiles", maxAuditLogFiles);
        attributes.put("AuditRolloverSize", auditRolloverSize);
        attributes.put("AuditRolloverMinutes", auditRolloverMinutes);
        attributes.put("AuditEnabled", auditEnabled);
        attributes.put("AuditIgnoresCount", auditIgnores.size());
        attributes.put("ConfigurationType", "AuditLog");
        
        return attributes;
    }

    public boolean isIntervalChangeOnly(AuditLoggingConfig newConfig) {
        return newConfig.auditLogFile.equals(auditLogFile) && 
               newConfig.maxAuditLogFiles == maxAuditLogFiles && 
               newConfig.auditRolloverSize.equals(auditRolloverSize) && 
               newConfig.auditEnabled == auditEnabled &&
               newConfig.auditIgnores.equals(auditIgnores) &&
               newConfig.auditRolloverMinutes != auditRolloverMinutes;
    }
}