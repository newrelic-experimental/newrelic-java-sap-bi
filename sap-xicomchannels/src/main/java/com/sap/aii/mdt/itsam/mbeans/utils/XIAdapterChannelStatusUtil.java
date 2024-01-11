package com.sap.aii.mdt.itsam.mbeans.utils;

import java.util.Locale;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.ximonitor.XIChannelReporter;
import com.sap.aii.af.service.administration.api.monitoring.ChannelState;
import com.sap.aii.af.service.administration.monitoring.ActivationState;
import com.sap.aii.af.service.administration.monitoring.AutomationState;
import com.sap.aii.af.service.administration.monitoring.ChannelActivationStatus;
import com.sap.aii.af.service.administration.monitoring.ChannelError;
import com.sap.aii.af.service.administration.monitoring.ClusterChannelRuntimeStatus;
import com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannel;

@Weave
public abstract class XIAdapterChannelStatusUtil {
	
	

	@Trace
	public static void setChannelToAdminError(SAP_ITSAMXIAdapterChannel modelAdapterChannel) {
		Weaver.callOriginal();
		XIChannelReporter.reportChannelStatus(modelAdapterChannel,"setChannelToAdminError");
	}
	
	@Trace
	public static void setChannelToUnregistered(SAP_ITSAMXIAdapterChannel modelAdapterChannel) {
		Weaver.callOriginal();
		XIChannelReporter.reportChannelStatus(modelAdapterChannel,"setChannelToUnregistered");
	}
	
	@Trace
	public static void setClusterChannelsInformation(SAP_ITSAMXIAdapterChannel modelAdapterChannel, ActivationState activationState, ChannelError[] adminActionClusterErrors, ClusterChannelRuntimeStatus channelRuntimeStatus, String locResult) {
		Weaver.callOriginal();
		XIChannelReporter.reportChannelStatus(modelAdapterChannel,"setClusterChannelsInformation");
	}
	
	@Trace
	public static void setRegisteredMonitorChannelStateAndBriefLog(SAP_ITSAMXIAdapterChannel modelAdapterChannel, ChannelState channelState, int clusterNodeCount, String message, ChannelActivationStatus activationStatus,
			ClusterChannelRuntimeStatus channelStatus, Locale locale) {
		Weaver.callOriginal();
		XIChannelReporter.reportChannelStatus(modelAdapterChannel,"setRegisteredMonitorChannelStateAndBriefLog");		
	}
	
	@Trace
	public static void updateAutomationState(SAP_ITSAMXIAdapterChannel modelAdapterChannel, AutomationState automationStatus) {
		Weaver.callOriginal();
		XIChannelReporter.reportChannelStatus(modelAdapterChannel,"updateAutomationState");		
	}
	
}
