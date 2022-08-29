package com.sap.aii.adapter.as2.ra.integration;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.as2.AS2Utils;
import com.sap.aii.af.service.cpa.Channel;
import com.sap.engine.interfaces.messaging.api.MessageKey;

@Weave
public abstract class ChannelDispatcher {
	
	@NewField
	private Token token = null;
	
	MessageKey amk = Weaver.callOriginal();
	Channel channel = Weaver.callOriginal();
	
	@WeaveAllConstructors
	public ChannelDispatcher() {
		if(token == null) {
			Token t = NewRelic.getAgent().getTransaction().getToken();
			if(t != null && t.isActive()) {
				token = t;
			} else if(t != null) {
				t.expire();
				t = null;
			}
		}
	}

	@Trace(async=true)
	public void receive() {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","SAP","AS2","ChannelDispatcher","receive");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		AS2Utils.addMessageKey(attributes, amk);
		AS2Utils.addChannel(attributes, channel);
		NewRelic.getAgent().getTracedMethod().addCustomAttributes(attributes);
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		Weaver.callOriginal();
	}
}
