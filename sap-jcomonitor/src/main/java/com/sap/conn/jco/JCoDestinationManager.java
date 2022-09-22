package com.sap.conn.jco;

import java.util.List;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.nr.instrumentation.sap.jcomonitor.StatsCollector;
import com.sap.conn.jco.monitor.JCoDestinationMonitor;

@Weave(type=MatchType.BaseClass)
public abstract class JCoDestinationManager {
	
	@WeaveAllConstructors
	protected JCoDestinationManager() {
		if(!StatsCollector.initialized) {
			StatsCollector.init();
		}
		StatsCollector.addDestinationManager(this);
	}

	public abstract JCoDestination getDestinationInstance(String var1, String var2) throws JCoException;

	public abstract List<String> getDestinationIDs();

	public abstract List<String> getCustomDestinationIDs(String var1);

	public abstract JCoDestinationMonitor getDestinationMonitor(String var1) throws JCoRuntimeException;

	public abstract JCoDestinationMonitor getRepositoryDestinationMonitor(String var1) throws JCoRuntimeException;

}
