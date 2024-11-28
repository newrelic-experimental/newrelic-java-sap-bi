package com.sap.engine.messaging.impl.core.queue.consumer;

import java.util.HashMap;
import java.util.Map;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.TransportType;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.engineimpl.EngineUtils;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;
import com.sap.engine.messaging.impl.core.queue.QueueEntry;
import com.sap.engine.messaging.impl.core.queue.QueueMessage;

@Weave(type = MatchType.BaseClass)
public abstract class QueueConsumer {

	protected String queueName = Weaver.callOriginal();
	
	@Trace(dispatcher = true)
	public void onMessage(QueueMessage queueMessage, QueueEntry var2) throws MessagingException {
		if(queueMessage.nr_headers != null) {
			NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.Other, queueMessage.nr_headers);
		}
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","QueueConsumer",getClass().getSimpleName(),"onMessage");
		Map<String, Object> attributes = new HashMap<>();
		EngineUtils.addMessageKey(attributes, queueMessage.getMessageKey());
		attributes.put("QueueName", queueName);
		traced.addCustomAttributes(attributes);
		Weaver.callOriginal();
	}
}
