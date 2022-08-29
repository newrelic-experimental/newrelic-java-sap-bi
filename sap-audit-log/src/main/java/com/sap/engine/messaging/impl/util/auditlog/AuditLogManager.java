package com.sap.engine.messaging.impl.util.auditlog;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.engineimpl.EngineUtils;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogEntry;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;

@Weave
public abstract class AuditLogManager {
	
	public AuditLogEntry writeEntry(AuditLogStatus status, String textKey, Object[] params) {
		EngineUtils.recordAuditLog(status, textKey, params);
		return Weaver.callOriginal();
	}
	
	public void writeEntry(MessageKey msgKey, AuditLogStatus status, String textKey, Object[] params, boolean persist) {
		EngineUtils.recordAuditLog(msgKey, status, textKey, params);
		Weaver.callOriginal();
	}
}
