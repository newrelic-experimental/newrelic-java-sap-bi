package com.newrelic.instrumentation.labs.sap.cc;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.service.cpa.CPAException;
import com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannel;
import com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannelClusterData;
import com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannelList;
import com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannelProcessingData;
import com.sap.aii.mdt.itsam.mbeans.utils.XIAdapterChannelUtil;

public class CommunicationChannelMonitor implements Runnable {

	public static boolean initialized = false;
	private static long lastCollection;
	private static CommunicationChannelConfig currentChannelConfig = null;
	protected static boolean detailed_enabled = true;
	protected static boolean summary_enabled = true;

	public static void init() {
		if(!initialized) {
			try {


				Calendar cal = Calendar.getInstance();
				cal.roll(Calendar.MINUTE, -2);
				lastCollection = cal.getTimeInMillis();

				if(currentChannelConfig == null) {
					currentChannelConfig = CommunicationChannelLogger.getConfig();
					NewRelic.getAgent().getInsights().recordCustomEvent("CommunicationChannelConfig", currentChannelConfig.getCurrentSettings());
				}

				detailed_enabled = currentChannelConfig.isDetailedEnabled();
				summary_enabled = currentChannelConfig.isSummaryEnabled();


				ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
				executor.scheduleAtFixedRate(new CommunicationChannelMonitor(), 2L, 2L, TimeUnit.MINUTES);
				executor.scheduleAtFixedRate(new SummaryFileLogger(), 5L, 5L, TimeUnit.MINUTES);
				initialized = true;
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, ", log file is {0}",currentChannelConfig.getChannelLog());

			} catch (Exception e) {
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, e, "Failed to open communication channel log");
			}


		}
	}

	@Override
	public void run() {

		NewRelic.recordMetric("SAP/CommunicationChannels/Details/Collection",1);
		if(!detailed_enabled) {
			NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINEST, "Communication Channel Monitoring is disabled, skipping monitoring");
			return;
		}

		long now = System.currentTimeMillis();

		if(!initialized) {
			init();
		}

		int count = 0;

		try {
			String[] channelIds = XIAdapterChannelUtil.getAllChannelIds();
			SAP_ITSAMXIAdapterChannelList channelList = XIAdapterChannelUtil.getChannelDetails("ID", true, null, channelIds, null);

			if(channelList != null) {
				SAP_ITSAMXIAdapterChannel[] listOfChannels = channelList.getChannelList();
				for(SAP_ITSAMXIAdapterChannel aChannel : listOfChannels) {
					SAP_ITSAMXIAdapterChannelClusterData[] clusterData = aChannel.getClusterDetails();
					if(clusterData != null) {
						for(SAP_ITSAMXIAdapterChannelClusterData cData : clusterData) {
							SAP_ITSAMXIAdapterChannelProcessingData[] processingDetails = cData.getProcessingDetails();
							for(SAP_ITSAMXIAdapterChannelProcessingData pData : processingDetails) {
								Date timestamp = pData.getTimestamp();
								if(timestamp != null) {
									long ts = timestamp.getTime();
									if(ts >= lastCollection && ts < now) {
										report(aChannel, cData, pData);
										count++;
									}
								}
							}
						}
					}
				}

				NewRelic.recordMetric("SAP/CommunicationChannels/ChannelsReported", listOfChannels.length);
				NewRelic.recordMetric("SAP/CommunicationChannels/ChannelRecords", count);
			} else {
				NewRelic.recordMetric("SAP/CommunicationChannels/NoChannelsFound", 1);
				NewRelic.recordMetric("SAP/CommunicationChannels/ChannelRecords", 0);
			}

		} catch (CPAException e) {
			NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, e, "Failed to report communication channels due to CPAException");
		}

		lastCollection = now;

	}

	private static void report(SAP_ITSAMXIAdapterChannel channel, SAP_ITSAMXIAdapterChannelClusterData cluster, SAP_ITSAMXIAdapterChannelProcessingData detail) {
		// Report channel details
		StringBuffer sb = new StringBuffer();
		sb.append("Channel: [");
		String name = channel.getChannelName();
		if(name != null && !name.isEmpty()) {
			sb.append("Name: ");
			sb.append(name);
			sb.append(',');
		}

		String channelId = channel.getChannelID();
		if(channelId != null && !channelId.isEmpty()) {
			sb.append("Channel Id: ");
			sb.append(channelId);
			sb.append(',');
		}

		String status = channel.getStatus();
		if(status != null && !status.isEmpty()) {
			sb.append("Channel Status: ");
			sb.append(status);
			sb.append(',');
		}

		String shortLog = channel.getShortLog();
		if(shortLog != null && !shortLog.isEmpty()) {
			sb.append("Short Log: ");
			sb.append(shortLog);
			sb.append(',');
		}

		String controlData = channel.getControlData();
		if(controlData != null && !controlData.isEmpty()) {
			sb.append("Control Data: ");
			sb.append(controlData);
			sb.append(',');
		}

		String processingErrors = channel.getProcessingErrors();
		if(processingErrors != null && !processingErrors.isEmpty()) {
			sb.append("Processing Errors: ");
			sb.append(processingErrors);
			sb.append(',');
		}

		String party = channel.getParty();
		if(party != null && !party.isEmpty()) {
			sb.append("Party: ");
			sb.append(party);
			sb.append(',');
		}

		String component = channel.getComponent();
		if(component != null && !component.isEmpty()) {
			sb.append("Component: ");
			sb.append(component);
			sb.append(',');
		}

		String adapter = channel.getAdapterType();
		if(adapter != null && !adapter.isEmpty()) {
			sb.append("Adapter Type: ");
			sb.append(adapter);
			sb.append(',');
		}

		String direction = channel.getDirection();
		if(direction != null && !direction.isEmpty()) {
			sb.append("Direction: ");
			sb.append(direction);
			sb.append(',');
		}

		int nodes = channel.getNumberOfNodes();
		if(nodes > -1) {
			sb.append("Number of Nodes: ");
			sb.append(nodes);
			sb.append(',');
		}

		// Report Cluster
		sb.append("], Cluster Data : [");

		String clusterName = cluster.getElementName();
		if(clusterName != null) {
			sb.append("Name: ");
			sb.append(clusterName);
			sb.append(',');
		}

		String nodeId = cluster.getClusterNodeID();
		if(nodeId != null) {
			sb.append("NodeId: ");
			sb.append(nodeId);
			sb.append(',');
		}

		String clusterStatus = cluster.getStatus();
		if(clusterStatus != null) {
			sb.append("Cluster Status: ");
			sb.append(clusterStatus);
			sb.append(',');
		}

		String cShortLog = cluster.getShortLog();
		if(cShortLog != null) {
			sb.append("Cluster Short Log: ");
			sb.append(cShortLog);
			sb.append(',');
		}
		sb.append("], ");

		sb.append("Processing Detail: [");

		String detailName = detail.getElementName();
		if(detailName != null && !detailName.isEmpty()) {
			sb.append("Name: ");
			sb.append(detailName);
			sb.append(',');
		}

		String type = detail.getType();
		if(type != null && !type.isEmpty()) {
			sb.append("Type: ");
			sb.append(type);
			sb.append(',');
		}

		String explaination = detail.getExplanation();
		if(explaination != null && !explaination.isEmpty()) {
			sb.append("Explaination: ");
			sb.append(explaination);
			sb.append(',');
		}

		Date ts = detail.getTimestamp();
		if(ts != null) {
			sb.append("Timestamp: ");
			sb.append(ts.toString());
			sb.append(',');
		}

		String msgId = detail.getMessageID();
		if(msgId != null && !msgId.isEmpty()) {
			sb.append("Message ID: ");
			sb.append(msgId);
		}
		sb.append('}');
		String result = sb.toString();
		if(!result.isEmpty()) {
			CommunicationChannelLogger.logToDetatils(result);
		}

	}

	private static class SummaryFileLogger implements Runnable {

		public void run() {
			CommunicationChannelLogger.checkConfig();
			if(!summary_enabled) return;
			NewRelic.recordMetric("SAP/CommunicationChannels/Summary/Collection",1);
			
			try {
				String[] channelIds = XIAdapterChannelUtil.getAllChannelIds();
				SAP_ITSAMXIAdapterChannelList channelList = XIAdapterChannelUtil.getChannelDetails("ID", true, null, channelIds, null);

				if(channelList != null) {
					SAP_ITSAMXIAdapterChannel[] listOfChannels = channelList.getChannelList();
					int count = 0;
					for(SAP_ITSAMXIAdapterChannel channel : listOfChannels) {
						
						StringBuffer sb = new StringBuffer();
						sb.append("Channel: [");
						String name = channel.getChannelName();
						if(name != null && !name.isEmpty()) {
							sb.append("Name: ");
							sb.append(name);
							sb.append(',');
						}

						String channelId = channel.getChannelID();
						if(channelId != null && !channelId.isEmpty()) {
							sb.append("Channel Id: ");
							sb.append(channelId);
							sb.append(',');
						}

						String status = channel.getStatus();
						if(status != null && !status.isEmpty()) {
							sb.append("Channel Status: ");
							sb.append(status);
							sb.append(',');
						}

						String adapter = channel.getAdapterType();
						if(adapter != null && !adapter.isEmpty()) {
							sb.append("Adapter Type: ");
							sb.append(adapter);
							sb.append(',');
						}

						String direction = channel.getDirection();
						if(direction != null && !direction.isEmpty()) {
							sb.append("Direction: ");
							sb.append(direction);
							sb.append(',');
						}

						int nodes = channel.getNumberOfNodes();
						if(nodes > -1) {
							sb.append("Number of Nodes: ");
							sb.append(nodes);
							sb.append(',');
						}

						SAP_ITSAMXIAdapterChannelClusterData[] clusterData = channel.getClusterDetails();
						if(clusterData != null) {
							if (clusterData.length > 0) {
								sb.append("ClusterData: [{");
								int i = 1;
								for (SAP_ITSAMXIAdapterChannelClusterData cData : clusterData) {
									sb.append("{ Cluster )" + i + ": ");
									String nodeid = cData.getClusterNodeID();
									if(nodeid != null && !nodeid.isEmpty()) {
										sb.append("Node ID: " + nodeid + ",");
									}
									String cStatus = cData.getStatus();
									if(cStatus != null && !cStatus.isEmpty()) {
										sb.append("Status: " + cStatus);
									}
									i++;
									if(i < clusterData.length - 1) {
										sb.append("}, ");
									} else {
										sb.append("}");
									}
								}
								sb.append("]");
							} else {
								sb.append("ClusterData: [Empty]");
							}
						} else {
							sb.append("ClusterData: No Cluster Data");
						}
						String result = sb.toString();
					
						CommunicationChannelLogger.logToSummary(result);
						count++;
					}
					NewRelic.recordMetric("SAP/CommunicationChannels/SummaryChannelsReported", listOfChannels.length);
					NewRelic.recordMetric("SAP/CommunicationChannels/SummaryChannelRecords", count);
					
				} else {
					NewRelic.recordMetric("SAP/CommunicationChannels/SummaryChannelsReported", 0);
					NewRelic.recordMetric("SAP/CommunicationChannels/SummaryChannelRecords", 0);
				}
			} catch (CPAException e) {
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, e,"Error writting to Summary Log file");
			}

		}

	}
}
