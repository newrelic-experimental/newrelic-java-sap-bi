package com.sap.engine.messaging.impl.core.queue.consumer;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;
import com.sap.engine.messaging.impl.core.queue.Queue;
import com.sap.engine.messaging.impl.core.queue.QueueEntry;
import com.sap.engine.messaging.impl.core.queue.QueueMessage;

@Weave
public class CallConsumer extends SyncConsumer {

	
	public CallConsumer(Queue queue) {
		super(queue);
	}

	@Trace(async = true)
	public void onMessage(QueueMessage queueMessage, QueueEntry queueEntry) throws MessagingException {
		if(queueMessage.token != null) {
			queueMessage.token.linkAndExpire();
			queueMessage.token = null;
		}
		Weaver.callOriginal();
	}
}
