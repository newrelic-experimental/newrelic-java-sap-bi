package com.sap.it.op.jpahelper;

import java.util.List;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.sap.gateway.GatewayMonitor;
import com.sap.igw.ejb.composite.DataSource;
import com.sap.igw.ejb.composite.MPLHeaderUI;
import com.sap.igw.ejb.composite.MPLSearchFilter;

@Weave
public abstract class MonitoringUI {
	
	public MonitoringUI() {
		if(!GatewayMonitor.initialized) {
			GatewayMonitor.initialize();
		}
		GatewayMonitor.addMonitoringUI(this);
	}

	@Trace
	public List<MPLHeaderUI> retrieveAllHeaders(MPLSearchFilter searchFilter, DataSource dataSource, int maxResults) {
		return Weaver.callOriginal();
	}
	
	@Trace
	public List<MPLHeaderUI> retrieveAllHeadersWhere(String messageGuid, String correlationId, String applicationId, DataSource dataSource, int maxResults) {
		return Weaver.callOriginal();
	}
}
