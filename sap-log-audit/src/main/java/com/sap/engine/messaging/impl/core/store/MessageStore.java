package com.sap.engine.messaging.impl.core.store;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.auditlogging.Logger;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.event.FinalMessageStatusData;
import com.sap.engine.messaging.impl.core.queue.QueueMessage;

@Weave
public abstract class MessageStore {

	@Weave
	public static class PutMessageInStoreOperation {
		
		private QueueMessage queueMessage = Weaver.callOriginal();
		private MessageKey msgKey = Weaver.callOriginal();
	
		public void commit(boolean txSyncCallback) {
			if(msgKey != null && queueMessage != null) {
				Logger.log(msgKey, queueMessage);
			}
			Weaver.callOriginal();
		}
		
	}
	
	@Weave
	public static class UpdateStatusOperation {
		
		
		private MessageKey messageKey = Weaver.callOriginal();
		private Integer timesFailed = Weaver.callOriginal();
		private FinalMessageStatusData fmsd = Weaver.callOriginal();
		
		public void commit(boolean txSyncCallback) {
			if(fmsd != null) {
				Logger.log(messageKey, fmsd, timesFailed);
			}
			Weaver.callOriginal();
		}
	}
}
