package com.newrelic.instrumentation.labs.sap.auditlogging;

import com.newrelic.api.agent.NewRelic;

/**
 * Test utility to verify SAP Audit and Message logging functionality
 * Run this to generate test messages and verify log file creation/content filtering
 * 
 * @author gsidhwani
 */
public class LoggingTestUtility {
    
    public static void main(String[] args) {
        System.out.println("=== SAP Logging Test Utility ===");
        System.out.println("Testing audit and message logging functionality...\n");
        
        // Test audit logging
        testAuditLogging();
        
        // Test message logging  
        testMessageLogging();
        
        // Test content filtering
        testContentFiltering();
        
        System.out.println("\n=== Test Complete ===");
        System.out.println("Check the following log files:");
        System.out.println("1. /usr/newrelic/logging/audit.log (or ${newrelic.home}/audit.log)");
        System.out.println("2. /usr/newrelic/logging/sap-messages.log (or ${newrelic.home}/sap-messages.log)");
        System.out.println("3. Verify sensitive data is filtered out from logs");
    }
    
    /**
     * Test audit logging functionality
     */
    public static void testAuditLogging() {
        System.out.println("--- Testing Audit Logging ---");
        
        try {
            // Initialize audit logging if not already done
            AuditLoggingLogger.init();
            
            // Test messages
            String[] testMessages = {
                "AUDIT: User login successful - UserID: testuser@company.com",
                "AUDIT: Configuration change detected - Module: SAP_AUDIT_LOG",
                "AUDIT: Security event - Failed authentication attempt from IP: 192.168.1.100", 
                "AUDIT: Data access - Table: CUSTOMERS, Operation: SELECT, Records: 1500",
                "AUDIT: System startup - SAP Audit Logging Controller initialized successfully"
            };
            
            for (String message : testMessages) {
                AuditLoggingLogger.logToAuditLog(message);
                System.out.println("✓ Audit message logged: " + message);
                Thread.sleep(100); // Small delay between messages
            }
            
        } catch (Exception e) {
            System.err.println("✗ Audit logging test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test message logging functionality
     */
    public static void testMessageLogging() {
        System.out.println("\n--- Testing Message Logging ---");
        
        try {
            // Initialize message logging if not already done
            MessageLoggingLogger.init();
            
            // Test messages
            String[] testMessages = {
                "MESSAGE: SAP XI/PI message processing started - MessageID: MSG_12345",
                "MESSAGE: Channel communication - Sender: SALES_SYSTEM, Receiver: ERP_BACKEND",
                "MESSAGE: Adapter message - Type: IDOC, Status: SUCCESS, ProcessingTime: 250ms",
                "MESSAGE: Integration flow - FlowName: Customer_Data_Sync, Status: COMPLETED",
                "MESSAGE: Error handling - MessageID: MSG_67890, Error: Connection timeout, RetryCount: 2"
            };
            
            for (String message : testMessages) {
                MessageLoggingLogger.logToMessageLog(message);
                System.out.println("✓ Message logged: " + message);
                Thread.sleep(100); // Small delay between messages
            }
            
        } catch (Exception e) {
            System.err.println("✗ Message logging test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test content filtering functionality
     */
    public static void testContentFiltering() {
        System.out.println("\n--- Testing Content Filtering ---");
        
        try {
            // Test audit logging with sensitive data (should be filtered)
            String[] auditSensitiveMessages = {
                "AUDIT: User authentication - Username: admin, Password: secret123, Token: abc123def456",
                "AUDIT: API call - Endpoint: /api/users, AuthToken: bearer_token_xyz789, Secret: my_secret_key",
                "AUDIT: Database connection - Server: db.company.com, Password: db_password_123"
            };
            
            System.out.println("Audit messages with sensitive data (should be filtered):");
            for (String message : auditSensitiveMessages) {
                AuditLoggingLogger.logToAuditLog(message);
                System.out.println("✓ Sent (with filtering): " + message);
                Thread.sleep(100);
            }
            
            // Test message logging with sensitive data (should be filtered)
            String[] messageSensitiveMessages = {
                "MESSAGE: User sync - UserData: {username: 'john', credentials: 'user_creds_456', auth: 'oauth_token'}",
                "MESSAGE: Authentication flow - AuthResult: SUCCESS, Credentials: encrypted_creds_789, Token: session_token_abc",
                "MESSAGE: Channel security - Protocol: HTTPS, Auth: basic_auth_credentials, Status: CONNECTED"
            };
            
            System.out.println("\nMessage logs with sensitive data (should be filtered):");
            for (String message : messageSensitiveMessages) {
                MessageLoggingLogger.logToMessageLog(message);
                System.out.println("✓ Sent (with filtering): " + message);
                Thread.sleep(100);
            }
            
        } catch (Exception e) {
            System.err.println("✗ Content filtering test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test configuration reading
     */
    public static void testConfiguration() {
        System.out.println("\n--- Testing Configuration ---");
        
        try {
            com.newrelic.api.agent.Config agentConfig = NewRelic.getAgent().getConfig();
            
            // Test audit config
            AuditLoggingConfig auditConfig = new AuditLoggingConfig(agentConfig);
            System.out.println("Audit Config:");
            System.out.println("  Log File: " + auditConfig.getAuditLogFile());
            System.out.println("  Max Files: " + auditConfig.getMaxAuditLogFiles());
            System.out.println("  Size Limit: " + auditConfig.getAuditRolloverSize());
            System.out.println("  Rollover Minutes: " + auditConfig.getAuditRolloverMinutes());
            System.out.println("  Enabled: " + auditConfig.isAuditEnabled());
            System.out.println("  Ignores: " + auditConfig.getAuditIgnores());
            
            // Test message config
            MessageLoggingConfig messageConfig = new MessageLoggingConfig(agentConfig);
            System.out.println("\nMessage Config:");
            System.out.println("  Log File: " + messageConfig.getMessageLogFile());
            System.out.println("  Max Files: " + messageConfig.getMaxMessageLogFiles());
            System.out.println("  Size Limit: " + messageConfig.getMessageRolloverSize());
            System.out.println("  Rollover Minutes: " + messageConfig.getMessageRolloverMinutes());
            System.out.println("  Enabled: " + messageConfig.isMessageEnabled());
            System.out.println("  Ignores: " + messageConfig.getMessageIgnores());
            
        } catch (Exception e) {
            System.err.println("✗ Configuration test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}