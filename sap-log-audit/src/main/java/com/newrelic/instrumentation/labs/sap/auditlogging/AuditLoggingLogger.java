package com.newrelic.instrumentation.labs.sap.auditlogging;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;

/**
 * Main controller for SAP Audit and Message Logging
 * Based on the modern pattern from ChannelMonitoringLogger
 * 
 * @author gsidhwani
 */
public class AuditLoggingLogger implements AgentConfigListener {

    public static boolean initialized = false;
    private static Logger AUDITLOGGER;
    private static Logger MESSAGELOGGER;
    
    private static AuditLoggingConfig currentAuditConfig = null;
    private static MessageLoggingConfig currentMessageConfig = null;
    
    private static AuditLoggingLogger INSTANCE = null;
    private static NRLabsHandler auditHandler;
    private static NRLabsHandler messageHandler;

    // Configuration constants for audit logging
    protected static final String AUDITLOGENABLED = "SAP.auditlogging.enabled";
    protected static final String AUDITLOGFILENAME = "SAP.auditlogging.audit_log_file_name";
    protected static final String AUDITLOGROLLOVERINTERVAL = "SAP.auditlogging.audit_log_file_interval";
    protected static final String AUDITLOGROLLOVERSIZE = "SAP.auditlogging.audit_log_size_limit";
    protected static final String AUDITLOGMAXFILES = "SAP.auditlogging.audit_log_file_count";
    protected static final String AUDITLOGIGNORES = "SAP.auditlogging.audit_ignores";

    // Configuration constants for message logging
    protected static final String MESSAGELOGENABLED = "SAP.auditlogging.message_log_enabled";
    protected static final String MESSAGELOGFILENAME = "SAP.auditlogging.message_log_file_name";
    protected static final String MESSAGELOGROLLOVERINTERVAL = "SAP.auditlogging.message_log_file_interval";
    protected static final String MESSAGELOGROLLOVERSIZE = "SAP.auditlogging.message_log_size_limit";
    protected static final String MESSAGELOGMAXFILES = "SAP.auditlogging.message_log_file_count";
    protected static final String MESSAGELOGIGNORES = "SAP.auditlogging.message_ignores";

    private AuditLoggingLogger() {
        // Private constructor for singleton pattern
    }

    protected static AuditLoggingConfig getAuditConfig() {
        if(currentAuditConfig != null) return currentAuditConfig;
        Config agentConfig = NewRelic.getAgent().getConfig();
        return getAuditConfig(agentConfig);
    }

    protected static MessageLoggingConfig getMessageConfig() {
        if(currentMessageConfig != null) return currentMessageConfig;
        Config agentConfig = NewRelic.getAgent().getConfig();
        return getMessageConfig(agentConfig);
    }

    protected static void logToAuditLog(String message) {
        if(!initialized) {
            init();
        }
        if(AUDITLOGGER != null) {
            AUDITLOGGER.log(Level.INFO, message);
        }
    }

    protected static void logToMessageLog(String message) {
        if(!initialized) {
            init();
        }
        if(MESSAGELOGGER != null) {
            MESSAGELOGGER.log(Level.INFO, message);
        }
    }

    public static void init() {
        synchronized(AuditLoggingLogger.class) {
            if(INSTANCE == null) {
                INSTANCE = new AuditLoggingLogger();
                ServiceFactory.getConfigService().addIAgentConfigListener(INSTANCE);
            }
        }
        
        if(currentAuditConfig == null) {
            Config agentConfig = NewRelic.getAgent().getConfig();
            
            // Log migration recommendations on first initialization
            ConfigurationMigration.logMigrationRecommendations(agentConfig);
            
            currentAuditConfig = getAuditConfig(agentConfig);
            currentMessageConfig = getMessageConfig(agentConfig);
        }

        initializeAuditLogger();
        initializeMessageLogger();
        
        initialized = true;
        NewRelic.getAgent().getLogger().log(Level.INFO, "SAP Audit Logging initialized with modern architecture");
    }

    private static void initializeAuditLogger() {
        int rolloverMinutes = currentAuditConfig.getAuditRolloverMinutes();
        if(rolloverMinutes == 0) {
            rolloverMinutes = 60;
        }

        String rolloverSize = currentAuditConfig.getAuditRolloverSize();
        long size = parseRolloverSize(rolloverSize);

        String auditLogFileName = currentAuditConfig.getAuditLogFile();
        
        // Ensure that the parent directory exists
        File file = new File(auditLogFileName);
        File parent = file.getParentFile();
        if(!parent.exists()) {
            boolean result = parent.mkdirs();
            if(result) {
                NewRelic.getAgent().getLogger().log(Level.FINE, "Created directories needed for audit log files: {0}", auditLogFileName);
            } else {
                NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to create directories needed for audit log files: {0}", auditLogFileName);
            }
        }

        int maxFiles = currentAuditConfig.getMaxAuditLogFiles();

        // If we have already initialized the handler then perform cleanup
        if(auditHandler != null) {
            auditHandler.flush();
            auditHandler.close();
            if(AUDITLOGGER != null) {
                AUDITLOGGER.removeHandler(auditHandler);
            }
            auditHandler = null;
        }

        try {
            auditHandler = new NRLabsHandler(auditLogFileName, rolloverMinutes, size, maxFiles);
            auditHandler.setFormatter(new NRLabsFormatter());
        } catch (SecurityException e) {
            NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create audit log file at {0}", auditLogFileName);
        } catch (IOException e) {
            NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create audit log file at {0}", auditLogFileName);
        }

        if (AUDITLOGGER == null) {
            try {
                NewRelic.getAgent().getLogger().log(Level.FINE, "Building Audit Log File, name: {0}, size: {1}, maxfiles: {2}", auditLogFileName, size, maxFiles);
                AUDITLOGGER = Logger.getLogger("AuditLog");
            } catch (SecurityException e) {
                NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create audit logger");
            }
        }

        if(AUDITLOGGER != null && auditHandler != null) {
            AUDITLOGGER.addHandler(auditHandler);
        }
    }

    private static void initializeMessageLogger() {
        int rolloverMinutes = currentMessageConfig.getMessageRolloverMinutes();
        if(rolloverMinutes == 0) {
            rolloverMinutes = 60;
        }

        String rolloverSize = currentMessageConfig.getMessageRolloverSize();
        long size = parseRolloverSize(rolloverSize);

        String messageLogFileName = currentMessageConfig.getMessageLogFile();
        
        // Ensure that the parent directory exists
        File file = new File(messageLogFileName);
        File parent = file.getParentFile();
        if(!parent.exists()) {
            boolean result = parent.mkdirs();
            if(result) {
                NewRelic.getAgent().getLogger().log(Level.FINE, "Created directories needed for message log files: {0}", messageLogFileName);
            } else {
                NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to create directories needed for message log files: {0}", messageLogFileName);
            }
        }

        int maxFiles = currentMessageConfig.getMaxMessageLogFiles();

        // If we have already initialized the handler then perform cleanup
        if(messageHandler != null) {
            messageHandler.flush();
            messageHandler.close();
            if(MESSAGELOGGER != null) {
                MESSAGELOGGER.removeHandler(messageHandler);
            }
            messageHandler = null;
        }

        try {
            messageHandler = new NRLabsHandler(messageLogFileName, rolloverMinutes, size, maxFiles);
            messageHandler.setFormatter(new NRLabsFormatter());
        } catch (SecurityException e) {
            NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create message log file at {0}", messageLogFileName);
        } catch (IOException e) {
            NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create message log file at {0}", messageLogFileName);
        }

        if (MESSAGELOGGER == null) {
            try {
                NewRelic.getAgent().getLogger().log(Level.FINE, "Building Message Log File, name: {0}, size: {1}, maxfiles: {2}", messageLogFileName, size, maxFiles);
                MESSAGELOGGER = Logger.getLogger("MessageLog");
            } catch (SecurityException e) {
                NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create message logger");
            }
        }

        if(MESSAGELOGGER != null && messageHandler != null) {
            MESSAGELOGGER.addHandler(messageHandler);
        }
    }

    private static long parseRolloverSize(String rolloverSize) {
        StringBuffer sb = new StringBuffer();
        int length = rolloverSize.length();
        for(int i = 0; i < length; i++) {
            char c = rolloverSize.charAt(i);
            if(Character.isDigit(c)) {
                sb.append(c);
            }
        }

        long size = Long.parseLong(sb.toString());
        char end = rolloverSize.charAt(length-1);
        switch (end) {
        case 'K':
            size *= 1024L;
            break;
        case 'M':
            size *= 1024L*1024L;
            break;
        case 'G':
            size *= 1024L*1024L*1024L;
            break;
        }

        // disallow less than 10K
        if(size < 10 * 1024L) {
            size = 10 * 1024L;
        }

        return size;
    }

    public static void checkConfig() {
        Config agentConfig = NewRelic.getAgent().getConfig();
        AuditLoggingConfig auditConfig = getAuditConfig(agentConfig);
        MessageLoggingConfig messageConfig = getMessageConfig(agentConfig);
        
        if(auditConfig != null && messageConfig != null) {
            if(currentAuditConfig == null || currentMessageConfig == null) {
                currentAuditConfig = auditConfig;
                currentMessageConfig = messageConfig;
                NewRelic.getAgent().getInsights().recordCustomEvent("AuditLoggingConfig", currentAuditConfig.getCurrentSettings());
                NewRelic.getAgent().getInsights().recordCustomEvent("MessageLoggingConfig", currentMessageConfig.getCurrentSettings());
                initialized = false;
                init();
            } else {
                boolean auditChanged = !auditConfig.equals(currentAuditConfig);
                boolean messageChanged = !messageConfig.equals(currentMessageConfig);
                
                if(auditChanged || messageChanged) {
                    currentAuditConfig = auditConfig;
                    currentMessageConfig = messageConfig;
                    
                    NewRelic.getAgent().getInsights().recordCustomEvent("AuditLoggingConfig", currentAuditConfig.getCurrentSettings());
                    NewRelic.getAgent().getInsights().recordCustomEvent("MessageLoggingConfig", currentMessageConfig.getCurrentSettings());
                    
                    initialized = false;
                    init();
                }
            }
        }
    }

    public static AuditLoggingConfig getAuditConfig(Config agentConfig) {
        // Use migration utility for backward compatibility
        return ConfigurationMigration.createAuditConfigWithBackwardCompatibility(agentConfig);
    }

    public static MessageLoggingConfig getMessageConfig(Config agentConfig) {
        // Use migration utility for backward compatibility
        return ConfigurationMigration.createMessageConfigWithBackwardCompatibility(agentConfig);
    }

    @Override
    public void configChanged(String appName, AgentConfig agentConfig) {
        AuditLoggingConfig auditConfig = getAuditConfig(agentConfig);
        MessageLoggingConfig messageConfig = getMessageConfig(agentConfig);
        
        if(auditConfig != null && messageConfig != null) {
            if(currentAuditConfig == null || currentMessageConfig == null) {
                currentAuditConfig = auditConfig;
                currentMessageConfig = messageConfig;
                NewRelic.getAgent().getInsights().recordCustomEvent("AuditLoggingConfig", currentAuditConfig.getCurrentSettings());
                NewRelic.getAgent().getInsights().recordCustomEvent("MessageLoggingConfig", currentMessageConfig.getCurrentSettings());
                initialized = false;
                init();
            } else {
                boolean auditChanged = !auditConfig.equals(currentAuditConfig);
                boolean messageChanged = !messageConfig.equals(currentMessageConfig);
                
                if(auditChanged || messageChanged) {
                    currentAuditConfig = auditConfig;
                    currentMessageConfig = messageConfig;

                    NewRelic.getAgent().getInsights().recordCustomEvent("AuditLoggingConfig", currentAuditConfig.getCurrentSettings());
                    NewRelic.getAgent().getInsights().recordCustomEvent("MessageLoggingConfig", currentMessageConfig.getCurrentSettings());
                    
                    initialized = false;
                    init();
                }
            }
        }
    }
}