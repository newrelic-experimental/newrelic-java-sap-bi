package com.sap.aii.af.app.modules;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.TransportType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adapters.ejb.AdaptersUtils;
import com.newrelic.instrumentation.labs.sap.adapters.ejb.DataUtils;
import com.newrelic.instrumentation.labs.sap.adapters.ejb.SAPMessageHeaders;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.service.cpa.Channel;
import com.sap.engine.interfaces.messaging.api.Connection;
import com.sap.engine.interfaces.messaging.api.Message;


@Weave
public abstract class CallAdapterWithMessageBean {

	@Trace(dispatcher=true)
	private Object process_receiver(ModuleContext moduleContext, Message message, Channel channel) {
		DataUtils.addContext(moduleContext);
		SAPMessageHeaders headers = new SAPMessageHeaders(message);
		NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.Other, headers);

		String adapterType = channel.getAdapterType();
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","Adapters","Channel","process_receiver",adapterType);
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		DataUtils.addAttributes(moduleContext, attributes);
		AdaptersUtils.addChannel(attributes, channel);
		AdaptersUtils.addMessage(attributes, message);
		traced.addCustomAttributes(attributes);

		return Weaver.callOriginal();
	}

	@Trace(dispatcher=true)
	private Message process_sender(Connection connection, ModuleContext moduleContext, Message msMessage) {
		DataUtils.addContext(moduleContext);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","Adapters","Channel","process_sender");
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		DataUtils.addAttributes(moduleContext, attributes);
		AdaptersUtils.addMessage(attributes, msMessage);
		traced.addCustomAttributes(attributes);

		return Weaver.callOriginal();
	}
}
