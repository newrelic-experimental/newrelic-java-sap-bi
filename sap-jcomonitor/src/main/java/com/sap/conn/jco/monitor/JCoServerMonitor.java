package com.sap.conn.jco.monitor;

import java.util.List;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.nr.instrumentation.sap.jcomonitor.StatsCollector;
import com.sap.conn.jco.monitor.JCoConnectionData;
import com.sap.conn.jco.server.JCoServerState;

@Weave(type=MatchType.Interface)
public abstract class JCoServerMonitor {
	
	@WeaveAllConstructors
	public JCoServerMonitor() {
		if(!StatsCollector.initialized) {
			StatsCollector.init();
		}
		StatsCollector.addServerMonitor(this);
	}

	public abstract int getServerThreadCount();

	public abstract int getCurrentServerThreadCount();

	public abstract int getMaximumUsedServerThreadCount();

	public abstract int getUsedServerThreadCount();

	public abstract int getCurrentConnectionCount();

	public abstract int getStatelessConnectionCount();

	public abstract List<? extends JCoConnectionData> getConnectionsData();

	public abstract JCoServerState getState();
}
