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
 * Controller for SAP Message Logging only
 * Audit logging is handled by AuditLoggingLogger
 * Based on the modern pattern from ChannelMonitoringLogger
 * 
 * @author gsidhwani
 */
public class MessageLoggingLogger implements AgentConfigListener {

    public static boolean initialized = false;
    private static Logger MESSAGELOGGER;
    private static final Object LOCK = new Object();
    private static MessageLoggingLogger INSTANCE = null;
    
    // Configuration keys for message logging
    protected static final String MESSAGELOGENABLED = "SAP.auditlogging.message_enabled";
    protected static final String MESSAGELOGFILENAME = "SAP.auditlogging.message_log_file_name";
    protected static final String MESSAGELOGROLLOVERINTERVAL = "SAP.auditlogging.message_log_file_interval";
    protected static final String MESSAGELOGROLLOVERSIZE = "SAP.auditlogging.message_log_size_limit";
    protected static final String MESSAGELOGMAXFILES = "SAP.auditlogging.message_log_file_count";
    
    private static NRLabsHandler messageHandler;
    private static MessageLoggingConfig currentMessageConfig;

    public static void init() {
        synchronized(MessageLoggingLogger.class) {
            if(INSTANCE == null) {
                INSTANCE = new MessageLoggingLogger();
                ServiceFactory.getConfigService().addIAgentConfigListener(INSTANCE);
            }
        }
        if(currentMessageConfig == null) {
            Config agentConfig = NewRelic.getAgent().getConfig();
            currentMessageConfig = getConfig(agentConfig);
        }
        
        // Setup the message logger if enabled
        if(currentMessageConfig != null && currentMessageConfig.isMessageEnabled()) {
            INSTANCE.setupMessageLogger();
        }
        
        initialized = true;
        NewRelic.getAgent().getLogger().log(Level.INFO, "SAP Message Logging Controller initialized");
    }

    public static void logToMessageLog(String message) {
        if(!initialized) {
            init();
        }
        if(MESSAGELOGGER != null && currentMessageConfig != null && currentMessageConfig.isMessageEnabled()) {
            MESSAGELOGGER.log(Level.INFO, message);
            NewRelic.getAgent().getLogger().log(Level.FINE, "Message logged to sap-messages.log: {0}", message);
        } else {
            NewRelic.getAgent().getLogger().log(Level.FINE, "Message logging skipped - MESSAGELOGGER: {0}, config: {1}, enabled: {2}", 
                MESSAGELOGGER != null, currentMessageConfig != null, 
                currentMessageConfig != null ? currentMessageConfig.isMessageEnabled() : false);
        }
    }

    public static MessageLoggingConfig getMessageConfig() {
        return currentMessageConfig;
    }

    private static MessageLoggingConfig getConfig(Config agentConfig) {
        return new MessageLoggingConfig(agentConfig);
    }

    @Override
    public void configChanged(String appName, AgentConfig agentConfig) {
        MessageLoggingConfig newConfig = getConfig(agentConfig);
        
        if(currentMessageConfig == null || !currentMessageConfig.equals(newConfig)) {
            synchronized(LOCK) {
                currentMessageConfig = newConfig;
                if(currentMessageConfig.isMessageEnabled()) {
                    setupMessageLogger();
                    NewRelic.getAgent().getInsights().recordCustomEvent("MessageLoggingConfig", currentMessageConfig.getCurrentSettings());
                }
            }
        } else {
            if(currentMessageConfig.isMessageEnabled()) {
                NewRelic.getAgent().getInsights().recordCustomEvent("MessageLoggingConfig", currentMessageConfig.getCurrentSettings());
            }
        }
    }

    private void setupMessageLogger() {
        String messageLogFileName = currentMessageConfig.getMessageLogFile();
        int rolloverMinutes = currentMessageConfig.getMessageRolloverMinutes();
        String rolloverSize = currentMessageConfig.getMessageRolloverSize();
        long size = parseRolloverSize(rolloverSize);
        int maxFiles = currentMessageConfig.getMaxMessageLogFiles();
        
        NewRelic.getAgent().getLogger().log(Level.INFO, "Setting up MessageLoggingController - file: {0}, enabled: {1}", 
            messageLogFileName, currentMessageConfig.isMessageEnabled());

        // Create directories if needed
        File messageFile = new File(messageLogFileName);
        File messageDir = messageFile.getParentFile();
        if(messageDir != null && !messageDir.exists()) {
            if(messageDir.mkdirs()) {
                NewRelic.getAgent().getLogger().log(Level.FINE, "Created directories needed for message log files: {0}", messageLogFileName);
            } else {
                NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to create directories needed for message log files: {0}", messageLogFileName);
            }
        }

        // Clean up existing handler
        if(messageHandler != null) {
            messageHandler.flush();
            messageHandler.close();
            if(MESSAGELOGGER != null) {
                MESSAGELOGGER.removeHandler(messageHandler);
            }
            messageHandler = null;
        }

        // Create new handler
        try {
            messageHandler = new NRLabsHandler(messageLogFileName, rolloverMinutes, size, maxFiles);
            messageHandler.setFormatter(new NRLabsFormatter());
        } catch (SecurityException e) {
            NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create message log file at {0}", messageLogFileName);
        } catch (IOException e) {
            NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create message log file at {0}", messageLogFileName);
        }

        // Create logger
        if (MESSAGELOGGER == null) {
            try {
                NewRelic.getAgent().getLogger().log(Level.FINE, "Building Message Log File, name: {0}, size: {1}, maxfiles: {2}", messageLogFileName, size, maxFiles);
                MESSAGELOGGER = Logger.getLogger("MessageLog");
                MESSAGELOGGER.setUseParentHandlers(false);
                NewRelic.getAgent().getLogger().log(Level.INFO, "Created MessageLoggingController logger successfully");
            } catch (SecurityException e) {
                NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to create message logger");
            } 
        }

        // Attach handler to logger
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
}