package com.sap.engine.interfaces.messaging.api.listener;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;

@Weave(type=MatchType.Interface)
public abstract class ExceptionListener {

	
	public void onException(MessagingException var1) {
		NewRelic.noticeError(var1);
		Weaver.callOriginal();
	}
}
