package com.sap.aii.af.app.modules;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.adapters.AdaptersUtils;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.service.administration.api.cpa.CPAChannelStoppedException;
import com.sap.aii.af.service.administration.api.cpa.CPAFactory;
import com.sap.aii.af.service.administration.api.cpa.CPALookupManager;
import com.sap.aii.af.service.cpa.CPAException;
import com.sap.aii.af.service.cpa.CPAObjectType;
import com.sap.aii.af.service.cpa.Channel;
import com.sap.engine.interfaces.messaging.api.Connection;
import com.sap.engine.interfaces.messaging.api.Message;


@Weave
public abstract class CallAdapterWithMessageBean {

	@Trace(dispatcher=true)
	private Object process_receiver(ModuleContext moduleContext, Message message, Channel channel) {
		String adapterType = channel.getAdapterType();
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP",adapterType,"Adapters","Channel","Receiver",adapterType);
//		if(message instanceof TransportableMessage) {
//			try {
//				TransportableMessage tMessage = (TransportableMessage)message;
//				SAPMessageHeaders wrapper = new SAPMessageHeaders(tMessage.getTransportHeaders());
//				NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.Other, wrapper);
//			} catch (MessageFormatException e) {
//			}
//		}
		
		HashMap<String,Object> attributes = new HashMap<String, Object>();
		AdaptersUtils.addChannel(attributes, channel);
		traced.addCustomAttribute("Channel", channel.getChannelName());
		traced.addCustomAttribute("Party", channel.getParty());
		traced.addCustomAttribute("Service", channel.getService());
		traced.addCustomAttribute("Engine", channel.getEngineName());

		return Weaver.callOriginal();
	}
	
	@Trace(dispatcher=true)
	private Message process_sender(Connection connection, ModuleContext moduleContext, Message msMessage) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","SAP","Adapters","Channel","Sender",connection.getName());
		try {
			CPALookupManager lm = CPAFactory.getInstance().getLookupManager();
			String strDisableCaching = moduleContext.getChannelID();
			Channel channel = (Channel) lm.getCPAObject(CPAObjectType.CHANNEL, strDisableCaching);
			traced.addCustomAttribute("Channel", channel.getChannelName());
			traced.addCustomAttribute("Party", channel.getParty());
			traced.addCustomAttribute("Service", channel.getService());
			traced.addCustomAttribute("Engine", channel.getEngineName());
		} catch (CPAChannelStoppedException e) {
		} catch (CPAException e) {
		}
		
		return Weaver.callOriginal();
	}
}
