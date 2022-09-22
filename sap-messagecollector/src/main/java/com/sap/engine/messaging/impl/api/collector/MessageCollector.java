package com.sap.engine.messaging.impl.api.collector;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.sap.message.Utils;
import com.sap.engine.messaging.impl.core.queue.QueueMessage;

@Weave
public abstract class MessageCollector {

	@Trace(dispatcher = true)
	public boolean onMessage(QueueMessage qmsg)  {
		HashMap<String, Object> attributes = new HashMap<>();
		Utils.addQueueMessage(attributes, qmsg);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		return Weaver.callOriginal();
	}
}
