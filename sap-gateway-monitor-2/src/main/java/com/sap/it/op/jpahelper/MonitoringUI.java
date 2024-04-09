package com.sap.it.op.jpahelper;

import java.util.List;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.sap.gateway_2.MPLMonitor;
import com.sap.igw.ejb.composite.DataSource;
import com.sap.igw.ejb.composite.ExtendedMPLHeader;
import com.sap.igw.ejb.composite.MPLHeaderUI;
import com.sap.igw.ejb.composite.MPLSearchFilter;

@Weave
public abstract class MonitoringUI {
	
	public MonitoringUI() {
		MPLMonitor.setMonitoringUI(this);
		if(!MPLMonitor.initialized) {
			MPLMonitor.initialize();
		}
	}

	public abstract String retrieveMplPart(String resourceId, DataSource dataSource);
	
	public abstract List<MPLHeaderUI> retrieveAllHeaders(DataSource dataSource, int maxResults);
	
	public abstract List<MPLHeaderUI> retrieveAllHeaders(MPLSearchFilter searchFilter, DataSource dataSource, int maxResults);
	
	public abstract List<MPLHeaderUI> retrieveAllHeadersWhere(String messageGuid, String correlationId, String applicationId, DataSource dataSource, int maxResults);
	
	public abstract List<MPLHeaderUI> retrieveAllHeadersWhere(String messageGuid, DataSource dataSource, int maxResults);
	
	public abstract ExtendedMPLHeader retrieveMPLHeader(String messageGuid, DataSource dataSource);
}
