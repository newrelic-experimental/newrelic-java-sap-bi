package com.nr.instrumentation.sap.adaptermonitoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.logging.Level;

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.service.administration.api.monitoring.ChannelDirection;
import com.sap.aii.af.service.administration.api.monitoring.ChannelState;
import com.sap.aii.af.service.administration.api.monitoring.MonitoringStatusListener;
import com.sap.aii.af.service.administration.api.monitoring.ProcessContext;
import com.sap.aii.af.service.administration.api.monitoring.ProcessState;
import com.sap.aii.af.service.cpa.Channel;

/**
 * Utility class that gets called by SAP and reports SAP statuses.  Reports Channel and Process status.
 * 
 * @author dhilpipre
 *
 */
public class NRMonitoringStatusListener implements MonitoringStatusListener {

	private static final String CHANNEL_IGNORES = "SAP.Channel.ignores";
	private static final String PROCESS_IGNORES = "SAP.Process.ignores";

	private static HashSet<String> channel_ignores = new HashSet<String>();
	private static HashSet<String> process_ignores = new HashSet<String>();

	private static ConfigListener listener_instance = null;

	public NRMonitoringStatusListener() {
		if(listener_instance == null) {
			listener_instance = new ConfigListener();
			ServiceFactory.getConfigService().addIAgentConfigListener(listener_instance);
			NewRelic.getAgent().getLogger().log(Level.FINE, "Registered monitoring status configuration listener");
			com.newrelic.api.agent.Config config = NewRelic.getAgent().getConfig();			

			if(config instanceof AgentConfig) {
				AgentConfig agentConfig = (AgentConfig)config;
				listener_instance.configChanged("", agentConfig);
			} else {
				String ignores = config.getValue(CHANNEL_IGNORES);
				channel_ignores.clear();
				if(ignores != null && !ignores.isEmpty()) {
					StringTokenizer st = new StringTokenizer(ignores, ",");
					while(st.hasMoreTokens()) {
						String token = st.nextToken();
						channel_ignores.add(token);
						NewRelic.getAgent().getLogger().log(Level.FINE, "Will ignore channel status for adapter type: {0}", token);
					}
				}
				ignores = config.getValue(PROCESS_IGNORES);
				NewRelic.getAgent().getLogger().log(Level.FINE, "config value of {0} is {1}", PROCESS_IGNORES,ignores);
				process_ignores.clear();
				if(ignores != null && !ignores.isEmpty()) {
					StringTokenizer st = new StringTokenizer(ignores, ",");
					while(st.hasMoreTokens()) {
						String token = st.nextToken();
						process_ignores.add(token);
						NewRelic.getAgent().getLogger().log(Level.FINE, "Will ignore process status for adapter type: {0}", token);
					}
				}

			}
		}
	}

	@Override
	public void reportChannelStatus(String adapterNamespace, String adapterName, ChannelDirection direction, ChannelState state, String message, Object[] messageParams) {
		NewRelic.incrementCounter("Custom/ChannelStatus/"+adapterNamespace+"/"+adapterName+"/"+state.toString());
		if(!channel_ignores.contains(adapterName)) {
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			putValue(attributes, "AdapterNamespace", adapterNamespace);
			putValue(attributes, "AdapterName", adapterName);
			putValue(attributes, "ChannelDirection", direction);
			putValue(attributes, "ChannelState", state);
			putValue(attributes, "Message", message);
			int count = 1;
			for(Object param : messageParams) {
				putValue(attributes, "MessageParam-"+count, param);
				count++;
			}
			NewRelic.getAgent().getInsights().recordCustomEvent("ChannelStatus", attributes);
		}
	}

	@Override
	public void reportProcessStatus(String adapterNamespace, String adapterName, ChannelDirection direction,ProcessState state, String message, Object[] messageParams, ProcessContext context) {
		NewRelic.incrementCounter("Custom/ProcessStatus/"+adapterNamespace+"/"+adapterName+"/"+state.toString());
		if (!process_ignores.contains(adapterName)) {
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			putValue(attributes, "AdapterNamespace", adapterNamespace);
			putValue(attributes, "AdapterName", adapterName);
			putValue(attributes, "ChannelDirection", direction);
			putValue(attributes, "ChannelState", state);
			putValue(attributes, "Message", message);
			int count = 1;
			for (Object param : messageParams) {
				putValue(attributes, "MessageParam-" + count, param);
				count++;
			}
			putValue(attributes, "Process-Interface", context.getInterface());
//			putValue(attributes, "Process-InterfaceNameSpace", context.getInterfaceNamespace());
			putValue(attributes, "Process-MessageId", context.getMessageId());
			putValue(attributes, "Process-ReceiverParty", context.getReceiverParty());
			putValue(attributes, "Process-ReceiverService", context.getReceiverService());
			putValue(attributes, "Process-SenderParty", context.getSenderParty());
			putValue(attributes, "Process-SenderService", context.getSenderService());
			Channel channel = context.getChannel();
			putValue(attributes, "Channel", channel.getChannelName());
			NewRelic.getAgent().getInsights().recordCustomEvent("ProcessStatus", attributes);
		}

	}

	@Override
	public void reportChannelStatus(String adapterNamespace, String adapterName, Channel[] channels, ChannelState state,String message, Object[] messageParams) {
		NewRelic.incrementCounter("Custom/ChannelStatus/"+adapterNamespace+"/"+adapterName+"/"+state.toString());
		if(!channel_ignores.contains(adapterName)) {
			for(Channel channel : channels) {
				HashMap<String, Object> attributes = new HashMap<String, Object>();
				AdapterUtils.addChannelFull(attributes, channel);
				AdapterUtils.addChannelState(attributes, state);
				String adapterType = channel.getAdapterType();
				if(!adapterName.equals(adapterType)) {
					putValue(attributes,"AdapterName",adapterName);
				}
				putValue(attributes,"Message",message);
				int count = 1;
				for(Object param : messageParams) {
					putValue(attributes, "MessageParam-"+count, param);
					count++;
				}

				NewRelic.getAgent().getInsights().recordCustomEvent("ChannelStatus", attributes);
			}

		}
	}


	private static void putValue(HashMap<String,Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}


	private static class ConfigListener implements AgentConfigListener {
		@Override
		public void configChanged(String appName, AgentConfig agentConfig) {	
			String ignores = agentConfig.getValue(CHANNEL_IGNORES);
			channel_ignores.clear();
			if(ignores != null && !ignores.isEmpty()) {
				StringTokenizer st = new StringTokenizer(ignores, ",");
				while(st.hasMoreTokens()) {
					String token = st.nextToken();
					channel_ignores.add(token);
					NewRelic.getAgent().getLogger().log(Level.FINE, "Will ignore channel status for adapter type: {0}", token);
				}
			}
			ignores = agentConfig.getValue(PROCESS_IGNORES);
			process_ignores.clear();
			if(ignores != null && !ignores.isEmpty()) {
				StringTokenizer st = new StringTokenizer(ignores, ",");
				while(st.hasMoreTokens()) {
					String token = st.nextToken();
					process_ignores.add(token);
					NewRelic.getAgent().getLogger().log(Level.FINE, "Will ignore process status for adapter type: {0}", token);
				}
			}
		}

	}

} 
