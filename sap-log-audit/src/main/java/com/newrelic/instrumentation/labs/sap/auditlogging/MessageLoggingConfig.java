package com.newrelic.instrumentation.labs.sap.auditlogging;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import com.newrelic.agent.config.ConfigFileHelper;

/**
 * Configuration class for SAP Message Logging
 * Based on the modern pattern from ChannelMonitoringConfig
 * 
 * @author gsidhwani
 */
public class MessageLoggingConfig {

    private String messageLogFile = null;
    private int maxMessageLogFiles = 3;
    private String messageRolloverSize = "100M";
    private int messageRolloverMinutes = 0;
    private boolean messageEnabled = true;
    private HashSet<String> messageIgnores = new HashSet<>();

    public static final String DEFAULT_MESSAGE_FILE_NAME = "sap-messages.log";

    public MessageLoggingConfig() {
        File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
        File messageFile = new File(newRelicDir, DEFAULT_MESSAGE_FILE_NAME);
        messageLogFile = messageFile.getAbsolutePath();
    }

    public MessageLoggingConfig(com.newrelic.api.agent.Config agentConfig) {
        this(); // Set defaults first
        
        // Read configuration values directly using existing SAP config structure
        Object rolloverMinutes = agentConfig.getValue("SAP.messagelog.log_file_interval");
        if(rolloverMinutes != null) {
            setMessageRolloverMinutes(Integer.parseInt(rolloverMinutes.toString()));
        }

        Object maxFiles = agentConfig.getValue("SAP.messagelog.log_file_count");
        if(maxFiles != null) {
            setMaxMessageLogFiles(Integer.parseInt(maxFiles.toString()));
        }

        Object rolloverSize = agentConfig.getValue("SAP.messagelog.log_size_limit");
        if(rolloverSize != null && !rolloverSize.toString().isEmpty()) {
            setMessageRolloverSize(rolloverSize.toString());
        }

        Object filename = agentConfig.getValue("SAP.messagelog.log_file_name");
        if(filename != null && !filename.toString().isEmpty()) {
            setMessageLogFile(filename.toString());
        }

        Object enabled = agentConfig.getValue("SAP.messagelog.enabled");
        if(enabled != null) {
            setMessageEnabled(Boolean.parseBoolean(enabled.toString()));
        }

        Object ignores = agentConfig.getValue("SAP.messagelog.ignores");
        if(ignores != null && !ignores.toString().isEmpty()) {
            setMessageIgnoresFromString(ignores.toString());
        }
    }

    public String getMessageLogFile() {
        return messageLogFile;
    }

    public void setMessageLogFile(String messageLogFile) {
        this.messageLogFile = messageLogFile;
    }

    public int getMaxMessageLogFiles() {
        return maxMessageLogFiles;
    }

    public void setMaxMessageLogFiles(int maxMessageLogFiles) {
        this.maxMessageLogFiles = maxMessageLogFiles;
    }

    public String getMessageRolloverSize() {
        return messageRolloverSize;
    }

    public void setMessageRolloverSize(String messageRolloverSize) {
        this.messageRolloverSize = messageRolloverSize;
    }

    public int getMessageRolloverMinutes() {
        return messageRolloverMinutes;
    }

    public void setMessageRolloverMinutes(int messageRolloverMinutes) {
        this.messageRolloverMinutes = messageRolloverMinutes;
    }

    public boolean isMessageEnabled() {
        return messageEnabled;
    }

    public void setMessageEnabled(boolean messageEnabled) {
        this.messageEnabled = messageEnabled;
    }

    public HashSet<String> getMessageIgnores() {
        return messageIgnores;
    }

    public void setMessageIgnores(HashSet<String> messageIgnores) {
        this.messageIgnores = messageIgnores;
    }

    public void addMessageIgnore(String ignore) {
        this.messageIgnores.add(ignore);
    }

    public void clearMessageIgnores() {
        this.messageIgnores.clear();
    }

    public void setMessageIgnoresFromString(String ignoresString) {
        messageIgnores.clear();
        if (ignoresString != null && !ignoresString.isEmpty()) {
            StringTokenizer st = new StringTokenizer(ignoresString, ",");
            while (st.hasMoreTokens()) {
                String token = st.nextToken().trim();
                if (!token.isEmpty()) {
                    messageIgnores.add(token);
                }
            }
        }
    }

    public boolean isIgnored(String messageType) {
        if (messageType == null || messageType.isEmpty()) {
            return false;
        }

        for (String ignoreText : messageIgnores) {
            if (messageType.contains(ignoreText)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        
        if (!(obj instanceof MessageLoggingConfig)) return false;
        
        MessageLoggingConfig newConfig = (MessageLoggingConfig) obj;
        
        return newConfig.messageLogFile.equals(messageLogFile) && 
               newConfig.maxMessageLogFiles == maxMessageLogFiles && 
               newConfig.messageRolloverMinutes == messageRolloverMinutes && 
               newConfig.messageRolloverSize.equals(messageRolloverSize) && 
               newConfig.messageEnabled == messageEnabled &&
               newConfig.messageIgnores.equals(messageIgnores);
    }

    public HashMap<String, Object> getCurrentSettings() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("MessageLogFileName", messageLogFile);
        attributes.put("MaxMessageLogFiles", maxMessageLogFiles);
        attributes.put("MessageRolloverSize", messageRolloverSize);
        attributes.put("MessageRolloverMinutes", messageRolloverMinutes);
        attributes.put("MessageEnabled", messageEnabled);
        attributes.put("MessageIgnoresCount", messageIgnores.size());
        attributes.put("ConfigurationType", "MessageLog");
        
        return attributes;
    }

    public boolean isIntervalChangeOnly(MessageLoggingConfig newConfig) {
        return newConfig.messageLogFile.equals(messageLogFile) && 
               newConfig.maxMessageLogFiles == maxMessageLogFiles && 
               newConfig.messageRolloverSize.equals(messageRolloverSize) && 
               newConfig.messageEnabled == messageEnabled &&
               newConfig.messageIgnores.equals(messageIgnores) &&
               newConfig.messageRolloverMinutes != messageRolloverMinutes;
    }
}