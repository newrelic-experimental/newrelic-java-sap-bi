package com.nr.instrumentation.sap.auditlogging;

import static com.nr.instrumentation.sap.auditlogging.Logger.AUDITLOGFILENAME;
import static com.nr.instrumentation.sap.auditlogging.Logger.AUDITLOGMAXFILES;
import static com.nr.instrumentation.sap.auditlogging.Logger.AUDITLOGROLLOVERINTERVAL;
import static com.nr.instrumentation.sap.auditlogging.Logger.AUDITLOGROLLOVERSIZE;
import static com.nr.instrumentation.sap.auditlogging.Logger.MESSAGELOGMAXFILES;
import static com.nr.instrumentation.sap.auditlogging.Logger.MESSAGELOGROLLOVERINTERVAL;
import static com.nr.instrumentation.sap.auditlogging.Logger.MESSAGELOGROLLOVERSIZE;
import static com.nr.instrumentation.sap.auditlogging.Logger.MESSAGELOGFILENAME;

import java.util.logging.Level;

import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;

public class LoggingConfig {

	protected static AuditConfig auditConfig = null;
	protected static MessageConfig  messageConfig = null;
	
	public static boolean reInitialize(Config agentConfig) {
		if(auditConfig == null || messageConfig == null) return true;
		
		AuditConfig newAuditConfig = getAuditConfig(agentConfig);
		MessageConfig newMessageConfig = getMessageConfig(agentConfig);
		
		if(newAuditConfig.equals(auditConfig) && newMessageConfig.equals(messageConfig)) return false;
		
		return true;
	}
	
	
	private static AuditConfig getAuditConfig(Config agentConfig) {
		AuditConfig newAuditConfig = new AuditConfig();
		
		
		Object obj = agentConfig.getValue(AUDITLOGROLLOVERINTERVAL);
		int rolloverMinutes = 0;
		if(obj != null) {
			rolloverMinutes = (int)obj; 
		}
		newAuditConfig.setRolloverInterval(rolloverMinutes);
		
		obj = agentConfig.getValue(AUDITLOGROLLOVERSIZE);
		String rolloverSize = "100k";
		if(obj != null) {
			rolloverSize = (String)obj; 
		}
		newAuditConfig.setRolloverSize(rolloverSize);
		
		obj = agentConfig.getValue(AUDITLOGMAXFILES);
		int auditLogMaxFiles = 0;
		if(obj != null) {
				auditLogMaxFiles = (int)obj; 
		} 
		newAuditConfig.setMaxFiles(auditLogMaxFiles);

		obj = agentConfig.getValue(AUDITLOGFILENAME);
		String auditLogFileName = null;
		if(obj != null) {
				auditLogFileName = (String)obj; 
		} 
		if(auditLogFileName != null && !auditLogFileName.isEmpty()) {
			newAuditConfig.setAuditFile(auditLogFileName);
		}
		
		return newAuditConfig;
	}
	
	private static MessageConfig getMessageConfig(Config agentConfig) {
		MessageConfig newMessageConfig = new MessageConfig();
		Object obj = agentConfig.getValue(MESSAGELOGFILENAME);
		String messageFileName = null;
		if(obj != null) {
			messageFileName = (String)obj;
		}
		if(messageFileName != null && !messageFileName.isEmpty()) {
			newMessageConfig.setMessageFile(messageFileName);
		}
		
		obj = agentConfig.getValue(MESSAGELOGMAXFILES);
		int messageLogMaxFiles = 3;
		if(obj != null) {
			messageLogMaxFiles = (Integer)obj;
		}
		newMessageConfig.setMaxLogFiles(messageLogMaxFiles);
		
		obj = agentConfig.getValue(MESSAGELOGROLLOVERSIZE);
		String rolloverSize = "100k";
		if(obj != null) {
			rolloverSize = (String)obj; 
		} 
		newMessageConfig.setRolloverSize(rolloverSize);
		
		obj = agentConfig.getValue(MESSAGELOGROLLOVERINTERVAL);
		int rolloverMinutes = 0;
		if(obj != null) {
			rolloverMinutes = (int)obj; 
		}
		newMessageConfig.setRolloverMinutes(rolloverMinutes);	
		
		return newMessageConfig;
	}
	
	public static void setup(Config agentConfig) {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Initializing Logging Config");
		if(messageConfig == null) {
			messageConfig = getMessageConfig(agentConfig);
			NewRelic.getAgent().getLogger().log(Level.FINE, "Message Logging Config has been initialzed");
			NewRelic.getAgent().getInsights().recordCustomEvent("MessageLoggingConfig", messageConfig.getCurrentSettings());
		}
		if(auditConfig == null) {
			auditConfig = getAuditConfig(agentConfig);
			NewRelic.getAgent().getLogger().log(Level.FINE, "Message Audit Logging Config has been initialzed");
			NewRelic.getAgent().getInsights().recordCustomEvent("AuditLoggingConfig", auditConfig.getCurrentSettings());
		}

		CollectionConfig.initialize();
		NewRelic.getAgent().getLogger().log(Level.FINE, "Logging Config has been initialized");
	}
}
