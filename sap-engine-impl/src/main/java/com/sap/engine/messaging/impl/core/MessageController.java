package com.sap.engine.messaging.impl.core;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.engineimpl.NRSAPHeaders;
import com.sap.engine.messaging.impl.core.queue.QueueMessage;
import com.sap.engine.messaging.impl.core.status.StatusCollectorDataImpl;

@Weave
public abstract class MessageController {

	@Trace(dispatcher = true)
	public void putMessageInStore(QueueMessage queueMessage, boolean syncResponse, boolean updatePerformance) {
		if(queueMessage.nr_headers == null) {
			queueMessage.nr_headers = new NRSAPHeaders();
			NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(queueMessage.nr_headers);
		}
		Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	public void putMessageInStore(QueueMessage queueMessage, StatusCollectorDataImpl data) {
		if(queueMessage.nr_headers == null) {
			queueMessage.nr_headers = new NRSAPHeaders();
			NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(queueMessage.nr_headers);
		}
		Weaver.callOriginal();
	}
}
