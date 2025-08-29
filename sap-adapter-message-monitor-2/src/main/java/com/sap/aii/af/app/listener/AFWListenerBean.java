package com.sap.aii.af.app.listener;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.AttributeProcessor;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.MessageMonitor;
import com.sap.engine.interfaces.messaging.api.Message;

@Weave
public abstract class AFWListenerBean {

	public AFWListenerBean() {
		if(!MessageMonitor.initialized) {
			MessageMonitor.initialize();
		}

	}
	
	public void onMessage(Message requestMessage) {
		AttributeProcessor.recordObject(requestMessage);
		Weaver.callOriginal();
	}
}
