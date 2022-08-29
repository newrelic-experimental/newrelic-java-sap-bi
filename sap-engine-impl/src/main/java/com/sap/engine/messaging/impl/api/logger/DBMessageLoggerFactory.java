package com.sap.engine.messaging.impl.api.logger;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.engineimpl.EngineUtils;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageStatus;

@Weave
public abstract class DBMessageLoggerFactory {

	@Weave
	static class DBMessageLogger {
		
		@Trace
		public void log(Message message, MessageStatus status, String errorCode, boolean triggerEvent) {
			EngineUtils.recordMessageLog(message, status, errorCode);
			Weaver.callOriginal();
		}
		
		@Trace
		public void logStatus(Message message, MessageStatus status, String errorCode, boolean triggerEvent) {
			EngineUtils.recordMessageLog(message, status, errorCode);
			Weaver.callOriginal();
		}
	}
}
