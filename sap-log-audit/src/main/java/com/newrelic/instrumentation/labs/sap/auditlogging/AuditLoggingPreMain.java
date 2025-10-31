package com.newrelic.instrumentation.labs.sap.auditlogging;

import java.lang.instrument.Instrumentation;

import com.newrelic.api.agent.NewRelic;

/**
 * PreMain class for SAP Audit Logging instrumentation
 * Initializes the audit logging infrastructure when the agent starts
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
        try {
            // Initialize the audit logging system
            AuditLoggingLogger.init();
            
            NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, 
                "SAP Audit Logging PreMain initialization completed successfully");
                
        } catch (Exception e) {
            NewRelic.getAgent().getLogger().log(java.util.logging.Level.SEVERE, e, 
                "Failed to initialize SAP Audit Logging in PreMain");
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