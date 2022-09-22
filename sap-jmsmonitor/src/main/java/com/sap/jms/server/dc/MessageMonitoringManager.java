package com.sap.jms.server.dc;

import com.newrelic.api.agent.weaver.Weave;
import com.nr.instrumentation.sap.jmsmonitor.DataCollector;

@Weave
public abstract class MessageMonitoringManager implements MessageMonitoringFacade {
	
	public MessageMonitoringManager(ServiceEnvironment environment) {
		if(!DataCollector.initialized) {
			DataCollector.init();
		}
		DataCollector.addMessageMonitoringFacade(this,environment);
                                                                                
	}
}
