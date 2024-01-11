package com.newrelic.instrumentation.labs.sap.ximonitor;

import java.util.logging.Level;

import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannel;
import com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannelClusterData;

public class XIChannelReporter {
	
	protected static final String CHANNELLOGFILENAME = "SAP.xicommunicationlog.log_file_name";
	protected static final String CHANNELLOGROLLOVERINTERVAL = "SAP.xicommunicationlog.log_file_interval";
	protected static final String CHANNELLOGIGNORES = "SAP.xicommunicationlog.ignores";
	protected static final String CHANNELLOGROLLOVERSIZE = "SAP.xicommunicationlog.log_size_limit";
	protected static final String CHANNELLOGMAXFILES = "SAP.xicommunicationlog.log_file_count";
	protected static final String CHANNELSLOGENABLED = "SAP.xicommunicationlog.enabled";
	public static final String log_file_name = "xicommunicationchannels.log";
	protected static XICommunctionChannelConfig currentChannelConfig = null;
	private static boolean enabled = true;
	public static boolean initialized = false;
	
	public static void init() {
		if(currentChannelConfig == null) {
			Config agentConfig = NewRelic.getAgent().getConfig();
			currentChannelConfig = getConfig(agentConfig);
			NewRelic.getAgent().getInsights().recordCustomEvent("XiCommunicationChannelConfig", currentChannelConfig.getCurrentSettings());
		}
		
		enabled = currentChannelConfig.isEnabled();
		initialized = true;
		
	}
	
	public static void reportChannelStatus(SAP_ITSAMXIAdapterChannel channelStatus,String reportingType) {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Call to XICommunicationChannelConfig.reportChannelStatus({0},{1}), enabled = {2}, initialized = {3}",channelStatus,reportingType,enabled,initialized);
		if(!enabled) return;
		
		if(!initialized) {
			init();
		}
		
		if(channelStatus != null) {
			StringBuffer sb = new StringBuffer();

			sb.append("XIChannelStatus: [");
			sb.append("ReportingType: " + reportingType + ", ");
			String tmp = channelStatus.getStatus();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("Status: ");
				sb.append(tmp);
				sb.append(", ");
			}

			tmp = channelStatus.getChannelName();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("ChannelName: ");
				sb.append(tmp);
				sb.append(", ");
			}
			
			tmp = channelStatus.getShortLog();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("ShortLog: ");
				sb.append(tmp);
				sb.append(", ");
			}

			tmp = channelStatus.getControlData();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("ControlData: ");
				sb.append(tmp);
				sb.append(", ");
			}

			tmp = channelStatus.getProcessingErrors();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("ProcessingErrors: ");
				sb.append(tmp);
				sb.append(", ");
			}

			tmp = channelStatus.getParty();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("Party: ");
				sb.append(tmp);
				sb.append(", ");
			}

			tmp = channelStatus.getComponent();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("Component: ");
				sb.append(tmp);
				sb.append(", ");
			}

			tmp = channelStatus.getNamespace();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("Namespace: ");
				sb.append(tmp);
				sb.append(", ");
			}

			tmp = channelStatus.getDirection();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("Direction: ");
				sb.append(tmp);
				sb.append(", ");
			}

			Integer i = channelStatus.getNumberOfNodes();
			if(i != null) {
				sb.append("NumberOfNodes: ");
				sb.append(i);
				sb.append(", ");
			}

			tmp = channelStatus.getChannelID();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("ChannelId: ");
				sb.append(tmp);
				sb.append(", ");
			}

			tmp = channelStatus.getDirectionInfo();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("DirectionInfo: ");
				sb.append(tmp);
				sb.append(", ");
			}

			tmp = channelStatus.getAdapterType();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("AdapterType: ");
				sb.append(tmp);
				sb.append(", ");
			}

			tmp = channelStatus.getMethodParametersId();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("MethodParametersId: ");
				sb.append(tmp);
				sb.append(", ");
			}

			tmp = channelStatus.getElementName();
			if(tmp != null && !tmp.isEmpty()) {
				sb.append("Element: ");
				sb.append(tmp);
				sb.append(", ");
			}
			
			sb.append("]");
			
			SAP_ITSAMXIAdapterChannelClusterData[] clusterDatas = channelStatus.getClusterDetails();
			

			if(clusterDatas != null && clusterDatas.length > 0) {
				int clusterSize = clusterDatas.length;
				sb.append(", ClusterData: { ");
				int counter = 1;
				for(SAP_ITSAMXIAdapterChannelClusterData data : clusterDatas) {
					sb.append("Cluster " + counter + ": [");
					
					tmp = data.getAdministrationError();
					if(tmp != null && !tmp.isEmpty()) {
						sb.append("AdministrationError: ");
						sb.append(tmp);
						sb.append(", ");
					}
					
					tmp = data.getClusterNodeID();
					if(tmp != null && !tmp.isEmpty()) {
						sb.append("ClusterNodeID: ");
						sb.append(tmp);
						sb.append(", ");
					}
					
					tmp = data.getElementName();
					if(tmp != null && !tmp.isEmpty()) {
						sb.append("ElementName: ");
						sb.append(tmp);
						sb.append(", ");
					}
					
					tmp = data.getInactive();
					if(tmp != null && !tmp.isEmpty()) {
						sb.append("Inactive: ");
						sb.append(tmp);
						sb.append(", ");
					}
					
					tmp = data.getMethodParametersId();
					if(tmp != null && !tmp.isEmpty()) {
						sb.append("MethodParametersId: ");
						sb.append(tmp);
						sb.append(", ");
					}
					
					tmp = data.getProcessingError();
					if(tmp != null && !tmp.isEmpty()) {
						sb.append("ProcessingError: ");
						sb.append(tmp);
						sb.append(", ");
					}
					
					tmp = data.getShortLog();
					if(tmp != null && !tmp.isEmpty()) {
						sb.append("ShortLog: ");
						sb.append(tmp);
						sb.append(", ");
					}
					
					tmp = data.getStatus();
					if(tmp != null && !tmp.isEmpty()) {
						sb.append("Status: ");
						sb.append(tmp);
						sb.append(", ");
					}
					
					if(counter < clusterSize-1) {
						sb.append("}, ");
					}
					counter++;
				}
				
				sb.append(']');
			}

			String result = sb.toString().replace(",]", "]");
			
			if(result.endsWith(",")) {
				result = result.substring(0, result.length()-1);
			}
			
			XIChannelLogger.log(result);
			
		}
	}


	
	public static XICommunctionChannelConfig getConfig(Config agentConfig) {
		XICommunctionChannelConfig channelConfig = new XICommunctionChannelConfig();
		
		Integer rolloverMinutes = agentConfig.getValue(CHANNELLOGROLLOVERINTERVAL);

		if(rolloverMinutes != null) {
			channelConfig.setRolloverMinutes(rolloverMinutes);
		}
		
		Integer maxFile = agentConfig.getValue(CHANNELLOGMAXFILES);
		if(maxFile != null) {
			channelConfig.setMaxLogFiles(maxFile);
		}
		
		String rolloverSize = agentConfig.getValue(CHANNELLOGROLLOVERSIZE);
		if(rolloverSize != null && !rolloverSize.isEmpty()) {
			channelConfig.setRolloverSize(rolloverSize);
		}
		
		String filename = agentConfig.getValue(CHANNELLOGFILENAME);
		if(filename != null && !filename.isEmpty()) {
			channelConfig.setChannelLog(filename);
		}
		
		boolean enabled = agentConfig.getValue(CHANNELSLOGENABLED, Boolean.TRUE);
		channelConfig.setEnabled(enabled);
		
		return channelConfig;
	}
}
