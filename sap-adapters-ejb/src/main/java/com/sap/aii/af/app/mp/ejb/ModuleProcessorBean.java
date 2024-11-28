package com.sap.aii.af.app.mp.ejb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adapters.ejb.AdaptersUtils;
import com.newrelic.instrumentation.labs.sap.adapters.ejb.DataUtils;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.service.cpa.CPAException;
import com.sap.aii.af.service.cpa.CPAObjectNotFoundException;
import com.sap.aii.af.service.cpa.CPAObjectType;
import com.sap.aii.af.service.cpa.Channel;
import com.sap.aii.af.service.cpa.Direction;
import com.sap.aii.af.service.cpa.LookupManager;

@Weave
public abstract class ModuleProcessorBean {
	
	private LookupManager lookupManager = Weaver.callOriginal();

	@Trace(dispatcher=true)
	public ModuleData process(String channelId, ModuleData objectData) {
		DataUtils.addData(objectData);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		Channel channel = null;
		String direction = null;
		String adapterType = null;
		HashMap<String, Object> span_attributes = new HashMap<String, Object>();
		AdaptersUtils.addValue(span_attributes,"ChannelId", channelId);
		DataUtils.addAttributes(objectData, span_attributes);
		traced.addCustomAttributes(span_attributes);
		HashMap<String, Object> eventAttributes = new HashMap<String, Object>();
		try {
			channel =(Channel) lookupManager.getCPAObject(CPAObjectType.CHANNEL, channelId);
		
			if(channel != null) {
				AdaptersUtils.addValue(eventAttributes, "Channel-Name", channel.getChannelName());
				AdaptersUtils.addValue(eventAttributes, "AdapterType", channel.getAdapterType());
				AdaptersUtils.addValue(eventAttributes, "Channel-Direction", channel.getDirection());
				AdaptersUtils.addValue(eventAttributes, "Channel-Party", channel.getParty());
				AdaptersUtils.addValue(eventAttributes, "Channel-Service", channel.getService());

				traced.addCustomAttribute("ChannelName", channel.getChannelName());
				adapterType = channel.getAdapterType();
				Direction channelDirection = channel.getDirection();
				if(channelDirection != null) {
					if(channelDirection == Direction.INBOUND) {
						direction = "OUTBOUND";
					} else if(channelDirection == Direction.OUTBOUND) {
						direction = "INBOUND";
					} else {
						direction = "UNKNOWN";
					}
				}
			}
		} catch (CPAObjectNotFoundException e) {
		} catch (CPAException e) {
		}
		
		Object principalObject = objectData.getPrincipalData();
		if(principalObject != null) {
			Map<String,Object> attributes = AdaptersUtils.processObject(principalObject);
			if(attributes != null && !attributes.isEmpty()) {
				traced.addCustomAttributes(attributes);
				AdaptersUtils.addValue(eventAttributes, "MessageKey-ID", attributes.get("MessageKey-ID"));
				AdaptersUtils.addValue(eventAttributes, "MessageKey-Direction", attributes.get("MessageKey-Direction"));
				AdaptersUtils.addValue(eventAttributes, "Message-CorrelationId", attributes.get("Message-CorrelationId"));
				AdaptersUtils.addValue(eventAttributes, "Endpoint-Address", attributes.get("Endpoint-Address"));
				AdaptersUtils.addValue(eventAttributes, "Endpoint-Transport", attributes.get("Endpoint-Transport"));
			}
		}
		List<String> names = new ArrayList<String>();
		names.add("Custom");
		names.add("SAP");
		names.add("Adapters");
		names.add("ModuleProcessorBean");
		names.add("process");
		if(adapterType != null) {
			names.add(adapterType);
		} else {
			names.add("UnknownAdapter");
		}
		if(direction != null) {
			names.add(direction);
		} else {
			names.add("UnknownDirection");
		}
		String[] namesArray = new String[names.size()];
		names.toArray(namesArray);
		traced.setMetricName(namesArray);
		if(!eventAttributes.isEmpty()) {
			AdaptersUtils.addInstanceName(eventAttributes);
			NewRelic.getAgent().getInsights().recordCustomEvent("AdapterMessage", eventAttributes);
		}
		
		ModuleData returnValue = Weaver.callOriginal();
		
		eventAttributes.clear();
		principalObject = returnValue.getPrincipalData();
		if(principalObject != null) {
			Map<String,Object> attributes = AdaptersUtils.processObject(principalObject);
			if(attributes != null && !attributes.isEmpty()) {
				traced.addCustomAttributes(attributes);
				AdaptersUtils.addValue(eventAttributes, "MessageKey-ID", attributes.get("MessageKey-ID"));
				AdaptersUtils.addValue(eventAttributes, "MessageKey-Direction", attributes.get("MessageKey-Direction"));
				AdaptersUtils.addValue(eventAttributes, "Message-CorrelationId", attributes.get("Message-CorrelationId"));
				AdaptersUtils.addValue(eventAttributes, "Endpoint-Address", attributes.get("Endpoint-Address"));
				AdaptersUtils.addValue(eventAttributes, "Endpoint-Transport", attributes.get("Endpoint-Transport"));
			}
		}

		if(!eventAttributes.isEmpty()) {
			AdaptersUtils.addInstanceName(eventAttributes);
			NewRelic.getAgent().getInsights().recordCustomEvent("AdapterMessage", eventAttributes);
		}
		
		return returnValue;
	}
}
