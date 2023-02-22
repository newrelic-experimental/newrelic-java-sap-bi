package com.sap.engine.messaging.impl.core.queue.consumer;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.engine.messaging.impl.core.queue.Queue;
import com.sap.engine.messaging.impl.core.queue.QueueEntry;
import com.sap.engine.messaging.impl.core.queue.QueueMessage;

@Weave
public abstract class SendConsumer  extends AsyncConsumer {

	public SendConsumer(Queue queue) {
		super(queue);
	}
	
	@Trace(async = true)
	public void onMessage(QueueMessage queueMessage, QueueEntry queueEntry) {
		if(queueMessage.token != null) {
			queueMessage.token.linkAndExpire();
			queueMessage.token = null;
		}
		Weaver.callOriginal();
	}
}
