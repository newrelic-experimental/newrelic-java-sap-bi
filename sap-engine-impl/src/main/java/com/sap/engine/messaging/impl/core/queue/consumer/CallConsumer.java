package com.sap.engine.messaging.impl.core.queue.consumer;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransportType;
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

	@Trace(dispatcher = true)
	public void onMessage(QueueMessage queueMessage, QueueEntry queueEntry) throws MessagingException {
		if(queueMessage.nr_headers != null) {
			NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.Other, queueMessage.nr_headers);
		}
		Weaver.callOriginal();
	}
}
