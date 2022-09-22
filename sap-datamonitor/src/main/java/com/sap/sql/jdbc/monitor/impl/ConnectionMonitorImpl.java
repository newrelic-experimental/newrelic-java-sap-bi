package com.sap.sql.jdbc.monitor.impl;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.datamonitor.DataSource_Harvester;
import com.sap.sql.jdbc.monitor.ConnectionMonitor;

@Weave
public abstract class ConnectionMonitorImpl implements ConnectionMonitor {
	
	public static ConnectionMonitor getInstance() {
		return Weaver.callOriginal();
	}

	@WeaveAllConstructors
	private ConnectionMonitorImpl() {
		if(!DataSource_Harvester.initialized) {
			DataSource_Harvester.init();
		}
	}

}
