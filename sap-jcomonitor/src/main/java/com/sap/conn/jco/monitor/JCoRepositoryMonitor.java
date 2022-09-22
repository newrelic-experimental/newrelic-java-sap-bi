package com.sap.conn.jco.monitor;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.nr.instrumentation.sap.jcomonitor.StatsCollector;

@Weave(type=MatchType.Interface)
public abstract class JCoRepositoryMonitor {
	
	@WeaveAllConstructors
	public JCoRepositoryMonitor() {
		if(!StatsCollector.initialized) {
			StatsCollector.init();
		}
		StatsCollector.addRepositoryMonitor(this);
	}

	public abstract long getLastAccessTimestamp();

	public abstract long getLastRemoteQueryTimestamp();

	public abstract int getFunctionMetaDataCount();

	public abstract int getTypeMetaDataCount();

	public abstract int getClassMetaDataCount();

}
