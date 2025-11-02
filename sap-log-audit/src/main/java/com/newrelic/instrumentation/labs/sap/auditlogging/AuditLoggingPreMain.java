package com.newrelic.instrumentation.labs.sap.auditlogging;

import java.lang.instrument.Instrumentation;

import com.newrelic.api.agent.NewRelic;

/**
 * PreMain class for SAP Audit Logging instrumentation
 * Initializes both AuditLoggingController and MessageLoggingController when the agent starts
 * Provides separate initialization tracking for better New Relic observability
 * 
 * @author gsidhwani
 */
public class AuditLoggingPreMain {

    /**
     * Called by the JVM when the agent is loaded
     * @param agentArgs agent arguments (unused)
     * @param inst Instrumentation instance (unused for logging)
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, 
            "SAP Audit Logging PreMain: Starting unified SAP logging infrastructure initialization...");
        
        // Initialize Audit Logging Controller
        initializeAuditLogging();
        
        // Initialize Message Logging Controller  
        initializeMessageLogging();
        
        NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, 
            "SAP Audit Logging PreMain: Complete initialization finished - Both AuditLoggingController and MessageLoggingController are ready for SAP observability");
    }

    /**
     * Initialize the AuditLoggingController for SAP audit trail and compliance logging
     */
    private static void initializeAuditLogging() {
        try {
            NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, 
                "SAP Audit Logging PreMain: [AUDIT] Starting AuditLoggingController initialization...");
            
            AuditLoggingLogger.init();
            
            NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, 
                "SAP Audit Logging PreMain: [AUDIT] ✓ AuditLoggingController initialized successfully - Ready for SAP audit trail, compliance, and security monitoring");
                
        } catch (Exception e) {
            NewRelic.getAgent().getLogger().log(java.util.logging.Level.SEVERE, e, 
                "SAP Audit Logging PreMain: [AUDIT] ✗ CRITICAL - Failed to initialize AuditLoggingController. Audit logging will NOT be available for SAP compliance and security monitoring.");
        }
    }

    /**
     * Initialize the MessageLoggingController for SAP channel monitoring and message processing
     */
    private static void initializeMessageLogging() {
        try {
            NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, 
                "SAP Audit Logging PreMain: [MESSAGE] Starting MessageLoggingController initialization...");
            
            MessageLoggingLogger.init();
            
            NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, 
                "SAP Audit Logging PreMain: [MESSAGE] ✓ MessageLoggingController initialized successfully - Ready for SAP channel monitoring, message processing, and integration logs");
                
        } catch (Exception e) {
            NewRelic.getAgent().getLogger().log(java.util.logging.Level.SEVERE, e, 
                "SAP Audit Logging PreMain: [MESSAGE] ✗ CRITICAL - Failed to initialize MessageLoggingController. Message logging will NOT be available for SAP channels and integration monitoring.");
        }
    }

    /**
     * Called by the JVM when the agent is loaded via agentmain (dynamic attach)
     * @param agentArgs agent arguments (unused)
     * @param inst Instrumentation instance (unused for logging)
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }
}