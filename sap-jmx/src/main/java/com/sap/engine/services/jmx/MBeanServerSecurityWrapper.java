package com.sap.engine.services.jmx;

import javax.management.MBeanServer;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.instrumentation.labs.sap.jmx.SAPJMXHarvester;

@Weave
public abstract class MBeanServerSecurityWrapper {

	public MBeanServerSecurityWrapper(JmxFrame frame, MBeanServer mbs) {
		SAPJMXHarvester.addMBeanServer(mbs);
	}
}
