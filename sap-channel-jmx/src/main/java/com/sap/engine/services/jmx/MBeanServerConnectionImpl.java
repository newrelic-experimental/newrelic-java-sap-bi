package com.sap.engine.services.jmx;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.channel.JMXDumper;

@Weave
public abstract class MBeanServerConnectionImpl {

	@WeaveAllConstructors
	public MBeanServerConnectionImpl() {
		if(!JMXDumper.initilized) {
			JMXDumper.init(this);
		}
		
	}
	
	public Integer getMBeanCount() {
		return Weaver.callOriginal();
	}
}
