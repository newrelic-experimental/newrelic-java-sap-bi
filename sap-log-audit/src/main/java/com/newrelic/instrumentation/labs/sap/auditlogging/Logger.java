package com.newrelic.instrumentation.labs.sap.auditlogging;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.MessageStatus;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogEntry;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;
import com.sap.engine.interfaces.messaging.api.event.FinalMessageStatusData;
import com.sap.engine.interfaces.messaging.api.logger.ProcessingState;
import com.sap.engine.messaging.impl.core.queue.QueueMessage;

/**
 * Modernized SAP Logger using the new logging infrastructure
 * This class maintains backward compatibility while using the new modular architecture
 * 
 * @author gsidhwani
 */
public class Logger implements Runnable, AgentConfigListener {

    public static boolean initialized = false;
    private static boolean simulate = false;
    private static Logger instance = null;
    
    // Legacy configuration constants for backward compatibility
    protected static final String SIMULATE = "SAP.testing.simulate";
    protected static final String AUDITLOGENABLED = "SAP.auditlog.enabled";
    protected static final String AUDITLOGFILENAME = "SAP.auditlog.log_file_name";
    protected static final String AUDITLOGROLLOVERINTERVAL = "SAP.auditlog.log_file_interval";
    protected static final String AUDITLOGIGNORES = "SAP.auditlog.ignores";
    protected static final String AUDITLOGROLLOVERSIZE = "SAP.auditlog.log_size_limit";
    protected static final String AUDITLOGMAXFILES = "SAP.auditlog.log_file_count";
    protected static final String MESSAGELOGENABLED = "SAP.messagelog.enabled";
    protected static final String MESSAGELOGFILENAME = "SAP.messagelog.log_file_name";
    protected static final String MESSAGELOGROLLOVERINTERVAL = "SAP.messagelog.log_file_interval";
    protected static final String MESSAGELOGIGNORES = "SAP.messagelog.ignores";
    protected static final String MESSAGELOGROLLOVERSIZE = "SAP.messagelog.log_size_limit";
    protected static final String MESSAGELOGMAXFILES = "SAP.messagelog.log_file_count";

    protected static final String DEFAULT_AUDIT_FILE_NAME = "audit.log";
    protected static final String DEFAULT_MESSAGE_FILE_NAME = "sap-messages.log";

    private static ScheduledFuture<?> execution_future;
    private static Properties messageMappings = null;

    public static ThreadLocal<Boolean> LOGGED = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    static {
        instance = new Logger();
        ServiceFactory.getConfigService().addIAgentConfigListener(instance);
        Config agentConfig = NewRelic.getAgent().getConfig();
        Object obj = agentConfig.getValue(SIMULATE);
        if(obj != null) {
            if(obj instanceof Boolean) {
                Boolean b = (Boolean)obj;
                if(b != simulate) {
                    simulate = (Boolean)obj;
                    NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set to {0}", simulate);
                    processExecutor(b);
                }
            } else if(obj instanceof String) {
                Boolean b = Boolean.getBoolean((String)obj);
                if(b != simulate) {
                    simulate = b;
                    NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set to {0}", simulate);
                    processExecutor(b);
                }
            } else {
                NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set but was not Boolean or String", obj.getClass().getName());
            }
        } else {
            NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate not set ");
        }
    }

    public static void init() {
        if(initialized) return;

        NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Initializing modern SAP Logger");
        
        // Initialize the new logging infrastructure
        AuditLoggingLogger.init();
        
        // Load message mappings
        loadMessageMappings();
        
        // Set message mappings in utils
        if(messageMappings != null) {
            AuditLogUtils.setMessageMappings(messageMappings);
        }
        
        initialized = true;
    }

    private static void loadMessageMappings() {
        InputStream is = null;
        boolean loaded = false;

        try {
            NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Attempting to load message mappings from embedded property file");
            is = instance.getClass().getResourceAsStream("/com/sap/engine/interfaces/messaging/api/i18n/rb_AuditLogResource_en.properties");
            if(is != null) {
                if(messageMappings == null) {
                    messageMappings = new Properties();
                }
                messageMappings.load(is);
                NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Loaded {0} message mappings", messageMappings.size());
                loaded = true;
            }
        } catch(Exception e) {
            NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, e, "Failed to load message mappings from embedded property file");
        }

        if(!loaded) {
            try {
                NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Attempting to load message mappings from string");
                if(messageMappings == null) {
                    messageMappings = new Properties();
                }
                StringReader reader = new StringReader(getDefaultProps());
                messageMappings.load(reader);
                NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Loaded {0} message mappings", messageMappings.size());
            } catch (Exception e) {
                NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, e, "Failed to load message mappings");
            }
        }
    }

    // Public API methods that delegate to the new infrastructure
    public static void log(Message message, MessageStatus status, String errorCode, String connName) {
        if(!initialized) {
            init();
        }
        AuditLogUtils.logMessage(message, status, errorCode, connName);
    }

    public static void log(Message message, ProcessingState state) {
        if(!initialized) {
            init();
        }
        AuditLogUtils.logMessage(message, state);
    }

    public static void log(MessageKey msgKey, QueueMessage msg) {
        if(!initialized) {
            init();
        }
        if(msgKey != null && msg != null) {
            AuditLogUtils.logQueueMessage(msgKey, 
                                         msg.getMessageStatus() != null ? msg.getMessageStatus().toString() : null,
                                         msg.getErrorCode(),
                                         msg.getRetries(),
                                         msg.getTimesFailed(),
                                         msg.getMessageSize(),
                                         msg.getConnectionName());
        }
    }

    public static void log(MessageKey msgKey, FinalMessageStatusData data, Integer timesFailed, boolean simulated) {
        if(!initialized) {
            init();
        }
        if(msgKey != null && data != null) {
            String logMsg = formatFinalMessageStatusData(msgKey, data, timesFailed, simulated);
                MessageLoggingLogger.logToMessageLog(logMsg + ", Logging Type: FinalMessageStatusData");
        }
    }

    public static void log(LinkedList<AuditLogEntry> list) {
        if(!initialized) {
            init();
        }
        Iterator<AuditLogEntry> iterator = list.iterator();
        while(iterator.hasNext()) {
            AuditLogEntry entry = iterator.next();
            MessageKey msgKey = entry.getMsgKey();
            AuditLogStatus status = entry.getStatus();
            String textKey = entry.getTextKey();
            String[] params = entry.getParams();
            String timeStamp = entry.getTimestampAsString();
            AuditLogUtils.logAudit(msgKey, status, timeStamp, textKey, params);
        }
    }

    public static void log(AuditLogEntry[] entries) {
        if(!initialized) {
            init();
        }
        for(AuditLogEntry entry : entries) {
            MessageKey msgKey = entry.getMsgKey();
            AuditLogStatus status = entry.getStatus();
            String textKey = entry.getTextKey();
            String[] params = entry.getParams();
            String timeStamp = entry.getTimestampAsString();
            AuditLogUtils.logAudit(msgKey, status, timeStamp, textKey, params);
        }
    }

    public static void log(MessageKey msgKey, AuditLogStatus status, String ts, String origTextKey, String... params) {
        if(!initialized) {
            init();
        }
        AuditLogUtils.logAudit(msgKey, status, ts, origTextKey, params);
    }

    public static void log(MessageKey msgKey, AuditLogStatus status, String origTextKey, Object... params) {
        if(!initialized) {
            init();
        }
        AuditLogUtils.logAudit(msgKey, status, origTextKey, params);
    }

    public static void log(AuditLogStatus status, String origTextKey, Object... params) {
        if(!initialized) {
            init();
        }
        AuditLogUtils.logAudit(null, status, origTextKey, params);
    }

    // Helper method for FinalMessageStatusData formatting
    private static String formatFinalMessageStatusData(MessageKey messageKey, FinalMessageStatusData data, Integer timesFailed, boolean simulated) {
        StringBuilder sb = new StringBuilder();
        
        if(messageKey != null) {
            sb.append("MessageKey-MessageId: ").append(messageKey.getMessageId()).append(", ");
            sb.append("MessageKey-Direction: ").append(messageKey.getDirection()).append(", ");
        }
        
        if(data.getMessageStatus() != null) {
            sb.append("Message Status: ").append(data.getMessageStatus().toString()).append(", ");
        }
        
        if(data.getDeliverySemantics() != null) {
            sb.append("DeliverySemantics: ").append(data.getDeliverySemantics().toString()).append(", ");
        }
        
        if(data.getSequenceId() != null) {
            sb.append("SequenceId: ").append(data.getSequenceId()).append(", ");
        }
        
        if(data.getConnectionName() != null) {
            sb.append("ConnectionName: ").append(data.getConnectionName()).append(", ");
        }
        
        if(timesFailed != null) {
            sb.append("TimesFailed: ").append(timesFailed).append(", ");
        }
        
        if(simulated) {
            sb.append("Simulated: true, ");
        }
        
        String result = sb.toString();
        if(result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }
        
        return result;
    }

    // Legacy compatibility methods
    public static boolean isIgnored(String name) {
        return AuditLogUtils.isAuditIgnored(name);
    }

    @Override
    public void run() {
        // Simulation functionality can be implemented here if needed
        // For now, we'll keep it simple and just log that simulation ran
        NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "SAP Logger simulation run executed");
    }

    @Override
    public void configChanged(String appName, AgentConfig agentConfig) {
        // Delegate to the new logging infrastructure
        if(AuditLoggingLogger.initialized) {
            // The AuditLoggingLogger will handle its own configuration changes
            // We just need to handle the simulation configuration
            Object obj = agentConfig.getValue(SIMULATE);
            if(obj != null) {
                if(obj instanceof Boolean) {
                    Boolean b = (Boolean)obj;
                    if(b != simulate) {
                        simulate = (Boolean)obj;
                        NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set to {0}", simulate);
                        processExecutor(b);
                    }
                } else if(obj instanceof String) {
                    Boolean b = Boolean.getBoolean((String)obj);
                    if(b != simulate) {
                        simulate = b;
                        NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set to {0}", simulate);
                        processExecutor(b);
                    }
                }
            }
        }
    }

    private static void processExecutor(boolean isStart) {
        if(isStart) {
            // Simulation not running so start
            if(execution_future == null) {
                execution_future = NewRelicExecutors.submit(instance, 15L, 15L, TimeUnit.SECONDS);
            }
        } else {
            if(execution_future != null) {
                execution_future.cancel(true);
                execution_future = null;
            }
        }
    }

    // Minimal default properties for message mappings
    private static String getDefaultProps() {
        return "# Default message mappings\n" +
               "XI_PROCESSING_STARTED=Processing started\n" +
               "XI_PROCESSING_COMPLETED=Processing completed\n" +
               "XI_PROCESSING_ERROR=Processing error occurred\n";
    }
}