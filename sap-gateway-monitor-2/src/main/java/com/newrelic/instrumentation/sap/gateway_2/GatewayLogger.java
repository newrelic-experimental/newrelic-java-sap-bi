package com.newrelic.instrumentation.sap.gateway_2;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.labs.log4j.Level;
import com.newrelic.labs.log4j.core.LoggerContext;
import com.newrelic.labs.log4j.core.config.Configurator;
import com.newrelic.labs.log4j.core.config.builder.api.AppenderComponentBuilder;
import com.newrelic.labs.log4j.core.config.builder.api.ComponentBuilder;
import com.newrelic.labs.log4j.core.config.builder.api.ConfigurationBuilder;
import com.newrelic.labs.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import com.newrelic.labs.log4j.core.config.builder.api.LayoutComponentBuilder;
import com.newrelic.labs.log4j.core.config.builder.impl.BuiltConfiguration;
import com.newrelic.labs.log4j.spi.ExtendedLogger;

public class GatewayLogger {

	private static ExtendedLogger LOGGER;
	protected static final String DEFAULT_MESSAGE_FILE_NAME = "gateway-messages-2.log";
	protected static final String MESSAGELOGENABLED = "SAP.gatewaylog.enabled";
	protected static final String MESSAGELOGFILENAME = "SAP.gatewaylog.log_file_name";
	protected static final String MESSAGELOGROLLOVERINTERVAL = "SAP.gatewaylog.log_file_interval";
	protected static final String MESSAGELOGIGNORES = "SAP.gatewaylog.ignores";
	protected static final String MESSAGELOGROLLOVERSIZE = "SAP.gatewaylog.log_size_limit";
	protected static final String MESSAGELOGMAXFILES = "SAP.gatewaylog.log_file_count";
	protected static final String MESSAGELOGSIMULATE = "SAP.gatewaylog.simulate";
	private static LoggerContext ctx = null;
	public static boolean initialized = false;
	private static ScheduledFuture<?> simulateFuture = null;
	private static boolean simulated = false;
	private static Simulate simulate = null;
	private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	protected static void checkSimulate(GatewayMessageConfig gConfig) {
		if(simulate == null) {
			simulate = new Simulate();
		}
		boolean newSimulated = gConfig.isSimulate();
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Call to checkSimulate, simulated = {0}, newsimulated = {1}",simulated,newSimulated);
		if(simulated != newSimulated) {
			NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "New value of simulate is different");
			if(simulated) {
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Simulate is running, will attempt to stop");
				// simulated is true and newSimulate is false, so stop the simulation
				if(simulateFuture != null) {
					boolean stopped = simulateFuture.cancel(true);
					NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Result of cancelling simulate future was {0}", stopped);
				} else {
					NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Simulate was marked as running but no ScheduleFuture available to use for cancel");
				}
				simulated = false;
				simulate.simulate = false;
				simulateFuture = null;
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Simulate stopped, have future {0}, simulated : {1}, simulate.simulate : {2}",simulateFuture,simulated,simulate.simulate);
			} else {
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Simulate is not running, will attempt to start");
				// simulated is false and newSimulate is true, so start the simulation
				simulateFuture = executor.scheduleAtFixedRate(simulate, 15L, 15L, TimeUnit.SECONDS);
				simulated = true;
				simulate.simulate = true;
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Simulate started, have future {0}, simulated : {1}, simulate.simulate : {2}",simulateFuture,simulated,simulate.simulate);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void initialize() {
		GatewayMessageConfig gConfig = LoggingConfig.gatewayConfig;
		if(gConfig == null || !LoggingConfig.initialized) {
			LoggingConfig.setup(NewRelic.getAgent().getConfig());
			gConfig = LoggingConfig.gatewayConfig;
		}
		
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		builder.setStatusLevel(Level.INFO);

		int rolloverMinutes = gConfig.getRolloverMinutes();
		String cronString;

		if(rolloverMinutes > 0) {
			cronString = "0 0/"+rolloverMinutes+" * * * ?";
		} else {
			cronString = "0 0 * * * ?";
		}

		String rolloverSize = gConfig.getRolloverSize();

		ComponentBuilder triggeringPolicy = builder.newComponent("Policies")
				.addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", cronString))
				.addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", rolloverSize));


		AppenderComponentBuilder gatewayfile = builder.newAppender("rolling", "RollingFile");
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();

		String gatewayLogFileName = gConfig.getMessageFile();
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINER, "Gatewaylogfilename2 {0}", gatewayLogFileName);
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINER, "New Relic Dir {0}", newRelicDir.getAbsolutePath());
		String defaultName = newRelicDir.getAbsolutePath() + File.separator + DEFAULT_MESSAGE_FILE_NAME;
		if(!defaultName.equals(gatewayLogFileName)) {
			File f = new File(gatewayLogFileName);
			String fName = f.getName();
			int index = fName.indexOf(".log");
			if(index > -1) {
				gatewayLogFileName = gatewayLogFileName.replace(".log", "-2.log");
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINER, "Changed Gatewaylogfilename2 to {0}", gatewayLogFileName);
			} else {
				gatewayLogFileName += "-2";
			}
		}

		int gatewayLogMaxFiles = gConfig.getMaxLogFiles();
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINER, "Gateway2logmaxfiles {0}", gatewayLogMaxFiles);

		gatewayfile.addAttribute("fileName", gatewayLogFileName);
		gatewayfile.addAttribute("filePattern", gatewayLogFileName +  ".%i");
		gatewayfile.addAttribute("max", gatewayLogMaxFiles);
		LayoutComponentBuilder standard = builder.newLayout("PatternLayout");
		standard.addAttribute("pattern", "%msg%n%throwable");
		gatewayfile.add(standard);
		gatewayfile.addComponent(triggeringPolicy);

		ComponentBuilder rolloverStrategy = builder.newComponent("DefaultRolloverStrategy").addAttribute("max", gatewayLogMaxFiles);

		gatewayfile.addComponent(rolloverStrategy);

		builder.add(gatewayfile);

		builder.add(builder.newLogger("GatewayLog2",Level.INFO)
				.add(builder.newAppenderRef("rolling"))
				.addAttribute("additivity", false));

		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINER, "Builder Configuration Finished: {0}", builder);
		BuiltConfiguration config = builder.build();
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINER, "Configuration created: {0}", config);

		if(ctx == null) {
			ctx = Configurator.initialize(config);
		} else {
			ctx.setConfiguration(config);
			ctx.reconfigure();
		}

		LOGGER = ctx.getLogger("GatewayLog2");
		initialized = true;
		
		if(gConfig != null) {
			checkSimulate(gConfig);
		}
	}
	
	public static void logMessage(String message) {
		if(LOGGER == null) {
			if(!initialized) {
				initialize();
				if(LOGGER == null) return ;
			} else {
				return;
			}
		}
		LOGGER.log(Level.INFO, message);
	}
	
}
