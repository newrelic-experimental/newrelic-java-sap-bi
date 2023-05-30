package com.sap.engine.messaging.impl.core;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.engine.messaging.impl.core.queue.QueueMessage;
import com.sap.engine.messaging.impl.core.status.StatusCollectorDataImpl;

@Weave
public abstract class MessageController {

	@Trace(dispatcher = true)
	public void putMessageInStore(QueueMessage queueMessage, boolean syncResponse, boolean updatePerformance) {
		if(queueMessage.token == null) {
			Token t = NewRelic.getAgent().getTransaction().getToken();
			if(t != null && t.isActive()) {
				queueMessage.token = t;
			} else if(t != null) {
				t.expire();
				t = null;
			}
		}
		Weaver.callOriginal();
	}
	
	@Trace(dispatcher = true)
	public void putMessageInStore(QueueMessage queueMessage, StatusCollectorDataImpl data) {
		if(queueMessage.token == null) {
			Token t = NewRelic.getAgent().getTransaction().getToken();
			if(t != null && t.isActive()) {
				queueMessage.token = t;
			} else if(t != null) {
				t.expire();
				t = null;
			}
		}
		Weaver.callOriginal();
	}
}
