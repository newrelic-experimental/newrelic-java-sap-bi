package com.sap.aii.af.app.listener;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.engine.interfaces.messaging.api.Message;

@Weave
public abstract class AFWListenerBean {

	public void onMessage(Message requestMessage) {
		Weaver.callOriginal();
	}
}
