package com.sap.engine.services.jmx;

import java.io.IOException;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.newrelic.instrumentation.labs.sap.jmxdumper.SAPJMXDumper;

@Weave
public abstract class MBeanServerConnectionImpl {
  @WeaveAllConstructors
  public MBeanServerConnectionImpl() {
    if (!SAPJMXDumper.initilized)
      SAPJMXDumper.init(this); 
  }
  
  public abstract Integer getMBeanCount() throws IOException;
  
}
