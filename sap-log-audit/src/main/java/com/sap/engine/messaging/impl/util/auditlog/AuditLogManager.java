package com.sap.engine.messaging.impl.util.auditlog;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.auditlogging.Logger;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogEntry;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;

@Weave
public abstract class AuditLogManager {
	
	
	private AuditLogManager() {
		if(!Logger.initialized) {
			Logger.init();
		}
	}
	
	public AuditLogEntry writeEntry(AuditLogStatus status, String textKey, Object[] params) {
		//Logger.log(status, textKey, params);
		return Weaver.callOriginal();
	}
	
	public void writeEntry(MessageKey msgKey, AuditLogStatus status, String textKey, Object[] params, boolean persist) {
		Logger.log(msgKey, status, textKey, params);
		Weaver.callOriginal();
	}
}
