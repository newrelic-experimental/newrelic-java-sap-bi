package com.newrelic.instrumentation.labs.sap.auditlogging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Simple formatter for SAP audit logging
 * Adapted from the sap-channel-monitoring implementation
 * 
 * @author gsidhwani
 */
public class NRLabsFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        return record.getMessage() + "\n";
    }
}