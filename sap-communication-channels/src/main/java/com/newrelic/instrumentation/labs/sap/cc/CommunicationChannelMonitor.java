package com.newrelic.instrumentation.labs.sap.cc;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.agent.deps.org.apache.logging.log4j.Level;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.LoggerContext;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.Configurator;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import com.newrelic.agent.deps.org.apache.logging.log4j.spi.ExtendedLogger;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.Logger;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.service.cpa.CPAException;
import com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannel;
import com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannelClusterData;
import com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannelList;
import com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannelProcessingData;
import com.sap.aii.mdt.itsam.mbeans.utils.XIAdapterChannelUtil;

public class CommunicationChannelMonitor implements Runnable, AgentConfigListener {

	public static boolean initialized = false;
	private static long lastCollection;
	private static ExtendedLogger DETAILSLOGGER;
	private static ExtendedLogger SUMMARYLOGGER;
	protected static final String CHANNELLOGFILENAME = "SAP.communicationlog.log_file_name";
	protected static final String SUMMARYCHANNELLOGFILENAME = "SAP.communicationlog.summarylog_file_name";
	protected static final String CHANNELLOGROLLOVERINTERVAL = "SAP.communicationlog.log_file_interval";
	protected static final String CHANNELLOGIGNORES = "SAP.communicationlog.ignores";
	protected static final String CHANNELLOGROLLOVERSIZE = "SAP.communicationlog.log_size_limit";
	protected static final String CHANNELLOGROLLOVERSIZE2 = "SAP.communicationlog.log_file_size";
	protected static final String CHANNELLOGMAXFILES = "SAP.communicationlog.log_file_count";
	protected static final String CHANNELSLOGENABLED = "SAP.communicationlog.enabled";
	public static final String log_file_name = "communicationchannels.log";
	public static final String summary_log_file_name = "channelsummary.log";
	private static LoggerContext logging_ctx = null;
	private static CommunicationChannelConfig currentChannelConfig = null;
	private static CommunicationChannelMonitor INSTANCE = null;
	private static boolean enabled = true;

	@SuppressWarnings("rawtypes")
	public static void init() {
		if(INSTANCE == null) {
			INSTANCE = new CommunicationChannelMonitor();
			ServiceFactory.getConfigService().addIAgentConfigListener(INSTANCE);
		}
		if(!initialized) {
			try {


				Calendar cal = Calendar.getInstance();
				cal.roll(Calendar.MINUTE, -2);
				lastCollection = cal.getTimeInMillis();

				if(currentChannelConfig == null) {
					Config agentConfig = NewRelic.getAgent().getConfig();
					currentChannelConfig = getConfig(agentConfig);
					NewRelic.getAgent().getInsights().recordCustomEvent("CommunicationChannelConfig", currentChannelConfig.getCurrentSettings());
				}

				enabled = currentChannelConfig.isEnabled();

				ConfigurationBuilder<BuiltConfiguration> cc_builder = ConfigurationBuilderFactory.newConfigurationBuilder();
				cc_builder.setStatusLevel(Level.INFO);

				int rolloverMinutes = currentChannelConfig.getRolloverMinutes();
				String cronString;

				if(rolloverMinutes == 0) {
					// default to rolling every hour
					cronString = "0 0 * * * ?";
				} else {
					// default to rolling every rolloverMinutes value minutes
					cronString = "0 */"+rolloverMinutes+" * * * ?";
				}

				String rolloverSize = currentChannelConfig.getRolloverSize();

				ComponentBuilder triggeringPolicy = cc_builder.newComponent("Policies")
						.addComponent(cc_builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", cronString))
						.addComponent(cc_builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", rolloverSize));

				AppenderComponentBuilder communicationFile = cc_builder.newAppender("ccrolling", "RollingFile");

				String communicationFileName = currentChannelConfig.getChannelLog();
				if(communicationFileName == null || communicationFileName.isEmpty()) {
					File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
					File logfile = new File(newRelicDir,log_file_name);
					communicationFileName = logfile.getName();					
				}

				int maxFiles = currentChannelConfig.getMaxLogFiles();

				communicationFile.addAttribute("fileName", communicationFileName);
				communicationFile.addAttribute("filePattern",communicationFileName + "-%i");
				communicationFile.addAttribute("max", maxFiles);
				LayoutComponentBuilder standard = cc_builder.newLayout("PatternLayout");
				standard.addAttribute("pattern", "%msg%n%throwable");
				communicationFile.add(standard);
				communicationFile.addComponent(triggeringPolicy);

				ComponentBuilder rolloverStrategy = cc_builder.newComponent("DefaultRolloverStrategy").addAttribute("max", maxFiles);

				communicationFile.addComponent(rolloverStrategy);

				cc_builder.add(communicationFile);

				cc_builder.add(cc_builder.newLogger("CommunctionChannelLog",Level.INFO)
						.add(cc_builder.newAppenderRef("ccrolling"))
						.addAttribute("additivity", false));
				
				
				
				ComponentBuilder triggeringPolicy2 = cc_builder.newComponent("Policies")
						.addComponent(cc_builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", cronString))
						.addComponent(cc_builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", rolloverSize));

				AppenderComponentBuilder summaryFile = cc_builder.newAppender("sumrolling", "RollingFile");

				String summaryFileName = currentChannelConfig.getSummaryChannelLog();

				if(summaryFileName == null || summaryFileName.isEmpty()) {
					File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
					File logfile = new File(newRelicDir,summary_log_file_name);
					summaryFileName = logfile.getName();
				}
			
				summaryFile.addAttribute("fileName", summaryFileName);
				summaryFile.addAttribute("filePattern",summaryFileName + "-%i");
				summaryFile.addAttribute("max", maxFiles);
				summaryFile.add(standard);
				summaryFile.addComponent(triggeringPolicy2);
				summaryFile.addComponent(rolloverStrategy);

				ComponentBuilder rolloverStrategy2 = cc_builder.newComponent("DefaultRolloverStrategy").addAttribute("max", maxFiles);
				summaryFile.addComponent(rolloverStrategy2);
				cc_builder.add(summaryFile);

				cc_builder.add(cc_builder.newLogger("SummaryChannelLog", Level.INFO)
						.add(cc_builder.newAppenderRef("sumrolling"))
						.addAttribute("additivity", false));
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "CommunicationChannels - Value of ConfigurationBuilder XML is {0}", cc_builder.toXmlConfiguration());

				BuiltConfiguration details_config = cc_builder.build();

				if(logging_ctx == null) {
					logging_ctx = Configurator.initialize(details_config);
				} else {
					logging_ctx.setConfiguration(details_config);
					logging_ctx.reconfigure();
				}

				DETAILSLOGGER = logging_ctx.getLogger("CommunctionChannelLog");

				
				SUMMARYLOGGER = logging_ctx.getLogger("SummaryChannelLog");
				

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

	public static CommunicationChannelConfig getConfig(Config agentConfig) {
		CommunicationChannelConfig channelConifg = new CommunicationChannelConfig();
		Integer rolloverMinutes = agentConfig.getValue(CHANNELLOGROLLOVERINTERVAL);

		if(rolloverMinutes != null) {
			channelConifg.setRolloverMinutes(rolloverMinutes);
		}

		Integer maxFile = agentConfig.getValue(CHANNELLOGMAXFILES);
		if(maxFile != null) {
			channelConifg.setMaxLogFiles(maxFile);
		}

		String rolloverSize = agentConfig.getValue(CHANNELLOGROLLOVERSIZE);
		if(rolloverSize != null && !rolloverSize.isEmpty()) {
			channelConifg.setRolloverSize(rolloverSize);
		} else {

			rolloverSize = agentConfig.getValue(CHANNELLOGROLLOVERSIZE2);
			if(rolloverSize != null && !rolloverSize.isEmpty()) {
				channelConifg.setRolloverSize(rolloverSize);
			}
		}

		String filename = agentConfig.getValue(CHANNELLOGFILENAME);
		if(filename != null && !filename.isEmpty()) {
			channelConifg.setChannelLog(filename);
		}

		boolean enabled = agentConfig.getValue(CHANNELSLOGENABLED, Boolean.TRUE);
		channelConifg.setEnabled(enabled);
		
		String summary = agentConfig.getValue(SUMMARYCHANNELLOGFILENAME);
		if(summary != null && !summary.isEmpty()) {
			channelConifg.setSummaryChannelLog(summary);
		}

		return channelConifg;

	}

	@Override
	public void run() {

		Logger logger = NewRelic.getAgent().getLogger();
		if(!enabled) {
			logger.log(java.util.logging.Level.FINE, "Communication Channel Monitoring is disabled, skipping monitoring");
			return;
		}

		long now = System.currentTimeMillis();
		logger.log(java.util.logging.Level.FINE, "Communication Channel Monitor start, now: {0}, lastColletion: {1}",now, lastCollection);

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

				NewRelic.recordMetric("Supportability/SAP/CommunicationChannels/ChannelsReported", listOfChannels.length);
				NewRelic.recordMetric("Supportability/SAP/CommunicationChannels/ChannelRecords", count);
			} else {
				NewRelic.recordMetric("Supportability/SAP/CommunicationChannels/NoChannelsFound", 1);
				NewRelic.recordMetric("Supportability/SAP/CommunicationChannels/ChannelRecords", 0);
			}

		} catch (CPAException e) {
			NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, e, "Failed to report communication channels due to CPAException");
		}


		//		LookupManager lookupMgr = LookupManager.getInstance();
		//
		//		try {
		//			LinkedList<Channel> channelList = lookupMgr.getAllCPAObjects(CPAObjectType.CHANNEL);
		//			for(Channel channel : channelList) {
		//				String channelId = channel.getObjectId();
		//				for(SAP_ITSAMXIAdapterChannelService_Impl channelService : channelServices) {
		//					SAP_ITSAMXIAdapterChannelList clist = channelService.RetrieveChannels(new String[] {channelId}, "DETAIL", Locale.getDefault().getLanguage());
		//					if(clist != null) {
		//						SAP_ITSAMXIAdapterChannel[] listOfChannels = clist.getChannelList();
		//						for(SAP_ITSAMXIAdapterChannel aChannel : listOfChannels) {
		//							SAP_ITSAMXIAdapterChannelClusterData[] clusterData = aChannel.getClusterDetails();null
		//							if(clusterData != null) {
		//								for(SAP_ITSAMXIAdapterChannelClusterData cData : clusterData) {
		//									SAP_ITSAMXIAdapterChannelProcessingData[] processingDetails = cData.getProcessingDetails();
		//									for(SAP_ITSAMXIAdapterChannelProcessingData pData : processingDetails) {
		//										Date timestamp = pData.getTimestamp();
		//										if(timestamp != null) {
		//											long ts = timestamp.getTime();
		//											if(ts >= lastCollection && ts < now) {
		//												report(aChannel, cData, pData);
		//												count++;
		//											}
		//										}
		//									}
		//								}
		//							}
		//						}
		//					}
		//				}
		//			}
		//		} catch (CPAException e) {
		//			logger.log(Level.FINE, e, "Error getting channel list");
		//		}

		lastCollection = now;
		logger.log(java.util.logging.Level.FINE, "Last collection set to {0}", lastCollection);

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
			DETAILSLOGGER.log(Level.INFO, result);
		}

	}

	@Override
	public void configChanged(String appName, AgentConfig agentConfig) {

		CommunicationChannelConfig channelConfig = getConfig(agentConfig);
		if(channelConfig != null) {
			if(currentChannelConfig == null) {
				currentChannelConfig = channelConfig;
				NewRelic.getAgent().getInsights().recordCustomEvent("CommunicationChannelConfig", currentChannelConfig.getCurrentSettings());
				initialized = false;
				init();
			} else {
				if(!channelConfig.equals(currentChannelConfig)) {
					currentChannelConfig = channelConfig;
					NewRelic.getAgent().getInsights().recordCustomEvent("CommunicationChannelConfig", currentChannelConfig.getCurrentSettings());
					initialized = false;
					init();
				}
			}
		}

	}

	private static class SummaryFileLogger implements Runnable {

		public void run() {
			NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Call to SummaryFileLogger.run");
			try {
				String[] channelIds = XIAdapterChannelUtil.getAllChannelIds();
				SAP_ITSAMXIAdapterChannelList channelList = XIAdapterChannelUtil.getChannelDetails("ID", true, null, channelIds, null);

				if(channelList != null) {
					SAP_ITSAMXIAdapterChannel[] listOfChannels = channelList.getChannelList();
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
						NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Writing summary to log: {0}", result);
					
						SUMMARYLOGGER.log(Level.INFO, result);
					}
					
				}
			} catch (CPAException e) {
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, e,"Error writting to Summary Log file");
			}

		}

	}
}
