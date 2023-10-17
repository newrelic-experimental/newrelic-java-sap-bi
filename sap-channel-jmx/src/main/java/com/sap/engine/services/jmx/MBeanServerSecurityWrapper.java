package com.sap.engine.services.jmx;

import javax.management.MBeanServer;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.labs.sap.channel.JMXDumper;

@Weave
public class MBeanServerSecurityWrapper {
	
	public MBeanServerSecurityWrapper(JmxFrame frame, MBeanServer mbs) {
		JMXDumper.addMBeanServer(mbs);
	}
	
}