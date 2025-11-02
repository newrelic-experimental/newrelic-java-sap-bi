package com.newrelic.instrumentation.labs.sap.auditlogging;

import java.util.Date;
import java.util.Properties;

import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.MessageStatus;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;
import com.sap.engine.interfaces.messaging.api.logger.ProcessingState;

/**
 * Utility class for formatting SAP audit log and message log entries
 * Extracted from the legacy Logger.java for better maintainability
 * 
 * @author gsidhwani
 */
public class AuditLogUtils {

    private static Properties messageMappings = null;

    public static void setMessageMappings(Properties mappings) {
        messageMappings = mappings;
    }

    public static String formatAuditMessage(MessageKey msgKey, AuditLogStatus status, String textKey, Object... params) {
        return formatAuditMessage(msgKey, status, null, textKey, params);
    }

    public static String formatAuditMessage(MessageKey msgKey, AuditLogStatus status, String timestamp, String textKey, Object... params) {
        StringBuilder sb = new StringBuilder();
        
        // Add timestamp
        if(timestamp != null && !timestamp.isEmpty()) {
            sb.append("Timestamp: ").append(timestamp).append("; ");
        } else {
            sb.append("Timestamp: ").append(new Date().toString()).append("; ");
        }
        
        // Add message key info
        if(msgKey != null) {
            sb.append("MessageKey: ").append(msgKey.getMessageId())
              .append(", ").append(msgKey.getDirection()).append("; ");
        } else {
            sb.append("MessageKey: Unknown; ");
        }
        
        // Add status
        String auditStatus = getStatusString(status);
        sb.append("Status: ").append(auditStatus).append("; ");
        
        // Add formatted text key with parameters
        String convertedTextKey = convertTextKey(textKey);
        String formattedTextKey = formatTextKeyWithParams(convertedTextKey, params);
        sb.append("TextKey: ").append(formattedTextKey);
        
        return sb.toString();
    }

    public static String formatMessageLog(Message message, MessageStatus status, String errorCode, String connName) {
        StringBuilder sb = new StringBuilder();
        
        // Get basic message information
        if(message != null) {
            MessageKey msgKey = message.getMessageKey();
            if(msgKey != null) {
                String msgId = msgKey.getMessageId();
                String msgDir = msgKey.getDirection().toString();
                
                if(msgId != null && !msgId.isEmpty()) {
                    sb.append("Message Id: ").append(msgId).append(", ");
                }
                if(msgDir != null && !msgDir.isEmpty()) {
                    sb.append("Message Direction: ").append(msgDir).append(", ");
                }
            }
        }
        
        // Add status
        if(status != null) {
            sb.append("Message Status: ").append(status.toString()).append(", ");
        }
        
        // Add error code
        if(errorCode != null && !errorCode.isEmpty()) {
            sb.append("Error Code: ").append(errorCode).append(", ");
        }
        
        // Add connection name
        if(connName != null && !connName.isEmpty()) {
            sb.append("Connection Name: ").append(connName).append(", ");
        }
        
        // Add logging type
        sb.append("Logging Type: Message");
        
        return sb.toString();
    }

    public static String formatProcessingStateMessage(Message message, ProcessingState state) {
        StringBuilder sb = new StringBuilder();
        
        if(message != null) {
            MessageKey msgKey = message.getMessageKey();
            if(msgKey != null) {
                String msgId = msgKey.getMessageId();
                String msgDir = msgKey.getDirection().toString();
                
                if(msgId != null && !msgId.isEmpty()) {
                    sb.append("Message Id: ").append(msgId).append(", ");
                }
                if(msgDir != null && !msgDir.isEmpty()) {
                    sb.append("Message Direction: ").append(msgDir).append(", ");
                }
            }
        }
        
        sb.append("ProcessingState: ").append(getProcessingStateString(state));
        
        return sb.toString();
    }

    public static String formatQueueMessage(MessageKey msgKey, String messageStatus, String errorCode, 
                                          int retries, int timesFailed, int messageSize, String connectionName) {
        StringBuilder sb = new StringBuilder();
        
        if(msgKey != null) {
            String msgId = msgKey.getMessageId();
            String msgDir = msgKey.getDirection().toString();
            
            if(msgId != null && !msgId.isEmpty()) {
                sb.append("Message Id: ").append(msgId).append(", ");
            }
            if(msgDir != null && !msgDir.isEmpty()) {
                sb.append("Message Direction: ").append(msgDir).append(", ");
            }
        }
        
        if(messageStatus != null && !messageStatus.isEmpty()) {
            sb.append("Message Status: ").append(messageStatus).append(", ");
        }
        
        if(errorCode != null && !errorCode.isEmpty()) {
            sb.append("Error Code: ").append(errorCode).append(", ");
        }
        
        sb.append("Retries: ").append(retries).append(", ");
        sb.append("Times Failed: ").append(timesFailed).append(", ");
        sb.append("Message Size: ").append(messageSize);
        
        if(connectionName != null && !connectionName.isEmpty()) {
            sb.append(", Connection Name: ").append(connectionName);
        }
        
        sb.append(", Logging Type: QueueMessage");
        
        return sb.toString();
    }

    private static String getStatusString(AuditLogStatus status) {
        if(status == null) return "Unknown";
        
        if(status == AuditLogStatus.SUCCESS) {
            return "Success";
        } else if(status == AuditLogStatus.WARNING) {
            return "Warning";
        } else if(status == AuditLogStatus.ERROR) {
            return "Error";
        } else {
            return "Unknown";
        }
    }

    private static String getProcessingStateString(ProcessingState state) {
        if(state == null) return "UNKNOWN";
        
        if(state == ProcessingState.PROCESSED) {
            return "PROCESSED";
        } else if(state == ProcessingState.PROCESSING) {
            return "PROCESSING";
        } else if(state == ProcessingState.RECORDED) {
            return "RECORDED";
        } else if(state == ProcessingState.ERROR) {
            return "ERROR";
        } else {
            return "UNKNOWN";
        }
    }

    private static String convertTextKey(String textKey) {
        if(messageMappings != null && textKey != null) {
            String converted = messageMappings.getProperty(textKey);
            return converted != null ? converted : textKey;
        }
        return textKey;
    }

    private static String formatTextKeyWithParams(String textKey, Object... params) {
        if(params == null || params.length == 0 || textKey == null) {
            return textKey != null ? textKey : "";
        }
        
        String result = textKey;
        for(int i = 0; i < params.length && i < 6; i++) {
            if(params[i] != null) {
                result = result.replace("{" + i + "}", params[i].toString());
            }
        }
        
        return result;
    }

    public static boolean isAuditIgnored(String textKey) {
        if(!AuditLoggingLogger.initialized) {
            AuditLoggingLogger.init();
        }
        
        AuditLoggingConfig config = AuditLoggingLogger.getAuditConfig();
        return config != null && config.isIgnored(textKey);
    }

    public static boolean isMessageIgnored(String messageType) {
        if(!AuditLoggingLogger.initialized) {
            AuditLoggingLogger.init();
        }
        
        MessageLoggingConfig config = MessageLoggingLogger.getMessageConfig();
        return config != null && config.isIgnored(messageType);
    }

    public static void logAudit(MessageKey msgKey, AuditLogStatus status, String textKey, Object... params) {
        if(isAuditIgnored(textKey)) {
            return;
        }
        
        AuditLoggingConfig config = AuditLoggingLogger.getAuditConfig();
        if(config == null || !config.isAuditEnabled()) {
            return;
        }
        
        String formattedMessage = formatAuditMessage(msgKey, status, textKey, params);
        AuditLoggingLogger.logToAuditLog(formattedMessage);
    }

    public static void logAudit(MessageKey msgKey, AuditLogStatus status, String timestamp, String textKey, String... params) {
        if(isAuditIgnored(textKey)) {
            return;
        }
        
        AuditLoggingConfig config = AuditLoggingLogger.getAuditConfig();
        if(config == null || !config.isAuditEnabled()) {
            return;
        }
        
        String formattedMessage = formatAuditMessage(msgKey, status, timestamp, textKey, (Object[])params);
        AuditLoggingLogger.logToAuditLog(formattedMessage);
    }

    public static void logMessage(Message message, MessageStatus status, String errorCode, String connName) {
        MessageLoggingConfig config = MessageLoggingLogger.getMessageConfig();
        if(config == null || !config.isMessageEnabled()) {
            return;
        }
        
        String formattedMessage = formatMessageLog(message, status, errorCode, connName);
        MessageLoggingLogger.logToMessageLog(formattedMessage);
    }

    public static void logMessage(Message message, ProcessingState state) {
        MessageLoggingConfig config = MessageLoggingLogger.getMessageConfig();
        if(config == null || !config.isMessageEnabled()) {
            return;
        }
        
        String formattedMessage = formatProcessingStateMessage(message, state);
        MessageLoggingLogger.logToMessageLog(formattedMessage);
    }

    public static void logQueueMessage(MessageKey msgKey, String messageStatus, String errorCode, 
                                     int retries, int timesFailed, int messageSize, String connectionName) {
        MessageLoggingConfig config = MessageLoggingLogger.getMessageConfig();
        if(config == null || !config.isMessageEnabled()) {
            return;
        }
        
        String formattedMessage = formatQueueMessage(msgKey, messageStatus, errorCode, retries, timesFailed, messageSize, connectionName);
        MessageLoggingLogger.logToMessageLog(formattedMessage);
    }
}