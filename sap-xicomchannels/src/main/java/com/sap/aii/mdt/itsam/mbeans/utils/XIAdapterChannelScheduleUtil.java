package com.sap.aii.mdt.itsam.mbeans.utils;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.labs.sap.ximonitor.XIChannelReporter;

@Weave
public class XIAdapterChannelScheduleUtil {

	public XIAdapterChannelScheduleUtil() {
		if(!XIChannelReporter.initialized) {
			XIChannelReporter.init();
		}
	}
}
