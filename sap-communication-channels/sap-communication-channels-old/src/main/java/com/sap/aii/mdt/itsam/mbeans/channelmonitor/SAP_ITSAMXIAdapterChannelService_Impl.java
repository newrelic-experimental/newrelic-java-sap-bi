package com.sap.aii.mdt.itsam.mbeans.channelmonitor;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.cc.CommunicationChannelMonitor;
import com.sap.aii.mdt.itsam.mbeans.channelmonitor.compositedata.SAP_ITSAMXIAdapterChannelList;

@Weave
public abstract class SAP_ITSAMXIAdapterChannelService_Impl implements SAP_ITSAMXIAdapterChannelService {
	
	@WeaveAllConstructors
	public SAP_ITSAMXIAdapterChannelService_Impl() {
		CommunicationChannelMonitor.addChannel(this);
		if(!CommunicationChannelMonitor.initialized) {
			CommunicationChannelMonitor.init();
		}
	}
	
	
	@Trace
	public void StartService() {
		Weaver.callOriginal();
	}

	@Trace
	public void StopService() {
		Weaver.callOriginal();
	}

	@Trace
	public SAP_ITSAMXIAdapterChannelList StartChannels(String[] ChannelIDs, String Locale) {
		return Weaver.callOriginal();
	}

	@Trace
	public SAP_ITSAMXIAdapterChannelList StopChannels(String[] ChannelIDs, String Locale) {
		return Weaver.callOriginal();
	}
}
