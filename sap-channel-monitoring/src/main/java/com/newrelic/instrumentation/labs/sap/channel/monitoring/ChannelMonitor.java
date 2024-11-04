package com.newrelic.instrumentation.labs.sap.channel.monitoring;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.service.administration.impl.AdminManagerImpl;
import com.sap.aii.af.service.administration.monitoring.ActivationState;
import com.sap.aii.af.service.administration.monitoring.ChannelActivationStatus;
import com.sap.aii.af.service.administration.monitoring.ClusterChannelStatusOverview;
import com.sap.aii.af.service.cpa.Channel;
import com.sap.aii.mdt.itsam.mbeans.utils.XIAdapterChannelUtil;

public class ChannelMonitor implements Runnable {

	public static boolean initialized = false;
	private static ChannelMonitoringConfig currentChannelConfig = null;
	protected static boolean enabled = true;
	private static ScheduledFuture<?> scheduledFuture = null;
	private static long collectionPeriod = 2L;
	private static ScheduledExecutorService executor;

	public static void init() {
		if(!initialized) {
			try {

				if(currentChannelConfig == null) {
					currentChannelConfig = ChannelMonitoringLogger.getConfig();
					NewRelic.getAgent().getInsights().recordCustomEvent("ChannelMonitoringConfig", currentChannelConfig.getCurrentSettings());
				}

				enabled = currentChannelConfig.isEnabled();

				executor = Executors.newScheduledThreadPool(2);
				collectionPeriod = currentChannelConfig.getCollection_period();
				NewRelic.getAgent().getLogger().log(Level.INFO, "Will collect channel status every {0} minutes", collectionPeriod);
				scheduledFuture = executor.scheduleAtFixedRate(new ChannelMonitor(), 1L, collectionPeriod, TimeUnit.MINUTES);
				initialized = true;
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, ", log file is {0}",currentChannelConfig.getChannelLog());

			} catch (Exception e) {
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, e, "Failed to open channel monitoring log");
			}


		}
	}
	
	protected static boolean reinitialScheduled(long n) {
		if(n == collectionPeriod) {
			// same period so noop
			return true;
		}
		// if monitor is currently running then cancel it
		if(scheduledFuture != null) {
			boolean b = scheduledFuture.cancel(true);
			// if false then unable to cancel so skip re-initialing 
			if(!b) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to stop Channel Montoring collector, skipping re-initialing with new collection period: {0}", n);
				return b;
			}
		}
		
		NewRelic.getAgent().getLogger().log(Level.INFO, "Will collect channel status every {0} minutes", collectionPeriod);
		scheduledFuture = executor.scheduleAtFixedRate(new ChannelMonitor(), n, n, TimeUnit.MINUTES);
	
		return true;
		
	}

	@Override
	public void run() {

		NewRelic.recordMetric("SAP/ChannelMonitoring/Collection",1);
		if(!enabled) {
			NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINEST, "Channel Monitoring is disabled, skipping monitoring");
			return;
		}

		if(!initialized) {
			init();
		}

		try {
			Date collectionTime = new Date();
			AdminManagerImpl adminManager = AdminManagerImpl.getInstance();

			Channel[] allChannels = XIAdapterChannelUtil.getAllChannels();

			HashSet<String> channelNames = new HashSet<>();
			HashMap<String, String> idToChannel = new HashMap<>();

			for(Channel channel : allChannels) {
				channelNames.add(channel.getChannelName());
				idToChannel.put(channel.getObjectId(), channel.getChannelName());
			}

			HashSet<String> stoppedChannels = new HashSet<>();
			HashSet<String> unknownChannels = new HashSet<>();
			HashSet<String> runningChannels = new HashSet<>();


			HashMap<String, ChannelActivationStatus> activationMap = adminManager.getChannelActivationStatusHashMap(allChannels);
			for(String channel : activationMap.keySet()) {
				String name = idToChannel.get(channel);
				ChannelActivationStatus actStatus = activationMap.get(channel);
				if(actStatus != null) {
					ActivationState actState = actStatus.getActivationState();
					if(actState == ActivationState.STOPPED) {
						stoppedChannels.add(name);
					} else if(actState == ActivationState.UNKNOWN) {
						unknownChannels.add(name);
					} else {
						runningChannels.add(name);
					}
				}
			}
			ClusterChannelStatusOverview overview = adminManager.getChannelStatusOverview();

			int numberOfNodes = overview.getNodeCount();

			for(int i=0;i<numberOfNodes;i++) {
				String nodeName = overview.getNodeName(i);
				HashSet<String> inactive = overview.getInactiveChannels(i);
				HashSet<String> inactive_Named = new HashSet<>();
				for(String id : inactive) {
					String name = idToChannel.get(id);
					inactive_Named.add(name);
				}
				
				
				HashSet<String> withErrors = overview.getChannelsWithProcessingErrors(i);
				HashSet<String> withErrors_Named = new HashSet<>();
				for(String id : withErrors) {
					String name = idToChannel.get(id);
					withErrors_Named.add(name);
				}
				

				HashSet<String> erronenous = overview.getErroneousChannels(i);
				HashSet<String> erronenous_Named = new HashSet<>();
				for(String id : erronenous) {
					String name = idToChannel.get(id);
					erronenous_Named.add(name);
				}


				HashMap<String, Object> attributes = new HashMap<>();
				attributes.put("NodeName", nodeName);
				attributes.put("Number Of Inactive Channels", inactive.size());
				attributes.put("Number Of Channels With Errors", withErrors.size());
				attributes.put("Number Of Erronous Channels", erronenous.size());
				NewRelic.getAgent().getInsights().recordCustomEvent("ClusterChannelOverview", attributes);
				
				reportClusterNode(nodeName, inactive_Named, erronenous_Named, withErrors_Named, stoppedChannels, runningChannels,collectionTime);

			}





		} catch(Exception e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Error executing Channel Monitoring");
		}

	}

	private static void reportClusterNode(String nodeName, HashSet<String> inactives, HashSet<String> errornous, HashSet<String> withErrors, 
			HashSet<String> stopped, Set<String> running, Date collectionTime) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Collection Time: ");
		sb.append(collectionTime);
		sb.append(',');

		sb.append("Node Name: " + nodeName + ",");

		sb.append("Inactive Channels: [");

		if(inactives != null && !inactives.isEmpty()) {
			int i = 0;
			int size = inactives.size();

			for(String id : inactives) {
				sb.append(id);
				i++;
				if (i < size) {
					sb.append(',');
				} 
			}
		} else {
			sb.append(" None Reported ");
		}
		sb.append("],");

		sb.append("Errornous Channels: [");

		if(errornous != null && !errornous.isEmpty()) {
			int i = 0;
			int size = errornous.size();

			for(String id : errornous) {
				sb.append(id);
				i++;
				if(i < size) {
					sb.append(',');
				}
			}
		} else {
			sb.append(" None Reported ");
		}
		sb.append("],");

		sb.append("With Errors Channels: [");

		if(withErrors != null && !withErrors.isEmpty()) {
			int i = 0;
			int size = withErrors.size();

			for(String id : withErrors) {
				sb.append(id);
				i++;
				if(i < size) {
					sb.append(',');
				}
			}
		} else {
			sb.append(" None Reported ");
		}
		sb.append("],");

		sb.append("Stopped Channels: [");

		if(stopped != null && !stopped.isEmpty()) {
			int i = 0;
			int size = stopped.size();

			for(String id : stopped) {
				sb.append(id);
				i++;
				if(i < size) {
					sb.append(',');
				}
			}
		} else {
			sb.append(" None Reported ");
		}
		sb.append("],");

		sb.append("Active Channels: [");

		if(running != null && !running.isEmpty()) {
			int i = 0;
			int size = running.size();

			for(String id : running) {
				sb.append(id);
				i++;
				if(i < size) {
					sb.append(',');
				}
			}
		} else {
			sb.append(" None Reported ");
		}
		sb.append("]");
		
		ChannelMonitoringLogger.logToChannelMonitor(sb.toString());
	}

}
