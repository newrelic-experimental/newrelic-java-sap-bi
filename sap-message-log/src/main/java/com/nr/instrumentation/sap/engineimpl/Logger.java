package com.nr.instrumentation.sap.engineimpl;

import java.io.File;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.agent.deps.org.apache.logging.log4j.Level;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.LoggerContext;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.Configuration;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.Configurator;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import com.newrelic.agent.deps.org.apache.logging.log4j.spi.ExtendedLogger;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;

public class Logger implements Runnable, AgentConfigListener {
  public static boolean initialized = false;
  
  private static ExtendedLogger LOGGER;
  
  private static boolean simulate = true;
  
  private int count = 0;
  
  private static Logger instance = null;
  
  private static final String SIMULATE = "SAP.messagelog.simulate";
  
  private static ScheduledExecutorService executor;
  
  private static Properties messageMappings = null;
  
  
  public static void init() {
    NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Initializing Logger");
    initialized = true;
    Config agentConfig = NewRelic.getAgent().getConfig();
    Object obj = agentConfig.getValue("SAP.messagelog.simulate");
    if (obj != null) {
      if (obj instanceof Boolean) {
        simulate = ((Boolean)obj).booleanValue();
        NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set to {0}", Boolean.valueOf(simulate));
      } else if (obj instanceof String) {
        simulate = Boolean.getBoolean((String)obj);
        NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set to {0}", Boolean.valueOf(simulate));
      } else {
        NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set but was not Boolean or String", obj.getClass().getName());
      } 
    } else {
      NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate not set ");
    } 
    ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
    builder.setStatusLevel(Level.INFO);
    ComponentBuilder triggeringPolicy = builder.newComponent("Policies").addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", "* */30 * * * ?")).addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "50M"));
    AppenderComponentBuilder file = builder.newAppender("rolling", "RollingFile");
    File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
    file.addAttribute("fileName", newRelicDir.getAbsolutePath() + "/sap-messages.log");
    file.addAttribute("filePattern", newRelicDir.getAbsolutePath() + "/sap-messages.log.%i.gz");
    file.addAttribute("max", 2);
    LayoutComponentBuilder standard = builder.newLayout("PatternLayout");
    standard.addAttribute("pattern", "%msg%n%throwable");
    file.add(standard);
    file.addComponent(triggeringPolicy);
    ComponentBuilder rolloverStrategy = builder.newComponent("DefaultRolloverStrategy").addAttribute("max", 3);
    file.addComponent(rolloverStrategy);
    builder.add(file);
    builder.add((LoggerComponentBuilder)((LoggerComponentBuilder)builder.newLogger("SAPMessageLog", Level.INFO)
        .add(builder.newAppenderRef("rolling")))
        .addAttribute("additivity", false));
    BuiltConfiguration config = (BuiltConfiguration)builder.build();
    LoggerContext ctx = Configurator.initialize((Configuration)config);
    LOGGER = (ExtendedLogger)ctx.getLogger("SAPMessageLog");
    ctx.getRootLogger().log(Level.ALL, "Initialized logger");
    instance = new Logger();
    ServiceFactory.getConfigService().addIAgentConfigListener(instance);
    if (simulate) {
        NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Starting Message log simulation ");
      executor = Executors.newSingleThreadScheduledExecutor();
      executor.scheduleAtFixedRate(instance, 15L, 15L, TimeUnit.SECONDS);
    } 
  }
  
  public void run() {
    NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Call to simulate message logging ");
	Timestamp ts = new Timestamp(System.currentTimeMillis());
	LOGGER.log(Level.ERROR, "Simulation run");
	LOGGER.log(Level.WARN, "Timestamp: "+ts.toString());
	LOGGER.log(Level.INFO, "count is "+count);
    count++;
  }
  
  public void configChanged(String appName, AgentConfig agentConfig) {
    Object obj = agentConfig.getValue("SAP.messagelog.simulate");
    if (obj != null) {
      if (obj instanceof Boolean) {
        Boolean b = (Boolean)obj;
        if (b.booleanValue() != simulate) {
          simulate = ((Boolean)obj).booleanValue();
          NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set to {0}", Boolean.valueOf(simulate));
          processExecutor(b.booleanValue());
        } 
      } else if (obj instanceof String) {
        Boolean b = Boolean.valueOf(Boolean.getBoolean((String)obj));
        if (b.booleanValue() != simulate) {
          simulate = b.booleanValue();
          NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set to {0}", Boolean.valueOf(simulate));
          processExecutor(b.booleanValue());
        } 
      } else {
        NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set but was not Boolean or String", obj.getClass().getName());
      } 
    } else {
      NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate not set ");
    } 
  }
  
  private static void processExecutor(boolean isStart) {
    if (isStart) {
      if (executor == null)
        executor = Executors.newSingleThreadScheduledExecutor(); 
      executor.scheduleAtFixedRate(instance, 15L, 15L, TimeUnit.SECONDS);
    } else if (executor != null) {
      executor.shutdown();
      executor = null;
    } 
  }
}
