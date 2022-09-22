package com.sap.engine.services.jmx;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.sap.jmx.SAPJMXHarvester;

@Weave
public abstract class MBeanServerConnectionImpl {

	@WeaveAllConstructors
	public MBeanServerConnectionImpl() {
		if(!SAPJMXHarvester.initilized) {
			SAPJMXHarvester.init(this);
		}
	}
	
	public Integer getMBeanCount() {
		return Weaver.callOriginal();
	}
}
