package com.newrelic.instrumentation.labs.sap.adaptermonitoring;

import java.util.HashMap;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.service.administration.api.monitoring.MonitoringManager;
import com.sap.aii.af.service.administration.api.monitoring.MonitoringManagerFactory;
import com.sap.aii.af.service.administration.impl.MonitoringManagerImpl;


/**
 * Utility class that registers NRMonitoringStatusListener as an SAP Monitoring Listener
 * This allows us to report SAP statuses as custom events
 * 
 * @author dhilpipre
 *
 */
public class AdapterCollector {
	
	public static boolean initialized = false;
	
	public static void init() {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		MonitoringManager monitoringMgr = MonitoringManagerFactory.getInstance().getMonitoringManager();
		if(monitoringMgr != null) {
			if(monitoringMgr instanceof MonitoringManagerImpl) {
				MonitoringManagerImpl monitoringMgrImpl = (MonitoringManagerImpl)monitoringMgr;
				monitoringMgrImpl.registerListener(new NRMonitoringStatusListener());
				attributes.put("initialized",true);
			} else {
				attributes.put("initialized",false);
			}
		} else {
			attributes.put("initialized",false);
		}
		AdapterUtils.addInstanceName(attributes);
		NewRelic.getAgent().getInsights().recordCustomEvent("MonitoringStatusListener", attributes);
		initialized = true;
	}

}
 