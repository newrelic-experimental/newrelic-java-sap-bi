package com.newrelic.instrumentation.sap.gateway;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.agent.deps.org.apache.logging.log4j.Level;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.LoggerContext;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.Configurator;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import com.newrelic.agent.deps.org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import com.newrelic.agent.deps.org.apache.logging.log4j.spi.ExtendedLogger;
import com.newrelic.api.agent.NewRelic;
import com.sap.igw.ejb.composite.DataSource;
import com.sap.igw.ejb.composite.MPLHeaderUI;
import com.sap.igw.ejb.composite.MPLSearchFilter;
import com.sap.it.op.jpahelper.MonitoringUI;

public class GatewayMonitor implements Runnable {
	
	private static List<MonitoringUI> monitoringUIs = new ArrayList<>();
	public static boolean initialized = false;
	private static GatewayMonitor INSTANCE = null;
	private long startTime = System.currentTimeMillis();
	private long endTime = 0L;
	private static ExtendedLogger LOGGER;
	protected static final String DEFAULT_MESSAGE_FILE_NAME = "gateway-messages.log";
	protected static final String MESSAGELOGENABLED = "SAP.gatewaylog.enabled";
	protected static final String MESSAGELOGFILENAME = "SAP.gatewaylog.log_file_name";
	protected static final String MESSAGELOGROLLOVERINTERVAL = "SAP.gatewaylog.log_file_interval";
	protected static final String MESSAGELOGIGNORES = "SAP.gatewaylog.ignores";
	protected static final String MESSAGELOGROLLOVERSIZE = "SAP.gatewaylog.log_size_limit";
	protected static final String MESSAGELOGMAXFILES = "SAP.gatewaylog.log_file_count";
	private static LoggerContext ctx = null;
	
	public static void addMonitoringUI(MonitoringUI ui) {
		if(!monitoringUIs.contains(ui)) {
			monitoringUIs.add(ui);
		}
	}
	
	private GatewayMonitor() {
		
	}

	public static void initialize() {
		if(LOGGER == null) {
			initializeLogger();
		}
		if(INSTANCE == null) {
			INSTANCE = new GatewayMonitor();
		}
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(INSTANCE, 15L, 15L, TimeUnit.SECONDS);
		initialized = true;
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "GatewayMonitor has been initialized");
	}
	
	@SuppressWarnings("rawtypes")
	private static void initializeLogger() {
		
		GatewayMessageConfig gConfig = LoggingConfig.gatewayConfig;
		if(gConfig == null) {
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
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINER, "Gatewaylogfilename {0}", gatewayLogFileName);
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINER, "New Relic Dir {0}", newRelicDir.getAbsolutePath());

		int gatewayLogMaxFiles = gConfig.getMaxLogFiles();
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINER, "Gatewaylogmaxfiles {0}", gatewayLogMaxFiles);

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

		builder.add(builder.newLogger("GatewayLog",Level.INFO)
				.add(builder.newAppenderRef("rolling"))
				.addAttribute("additivity", false));

		BuiltConfiguration config = builder.build();

		if(ctx == null) {
			ctx = Configurator.initialize(config);
		} else {
			ctx.setConfiguration(config);
			ctx.reconfigure();
		}

		LOGGER = ctx.getLogger("GatewayLog");

	}

	@Override
	public void run() {
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Call to GatewayMonitor.run()");
		endTime = System.currentTimeMillis();
		try {
			MPLSearchFilter filter = new MPLSearchFilter();
			filter.setStart(new Date(startTime));
			filter.setEnd(new Date(endTime));
			List<MPLHeaderUI> allHeaders = new ArrayList<>();
			
			for(MonitoringUI ui : monitoringUIs) {
				List<MPLHeaderUI> listOfHeaders = ui.retrieveAllHeaders(filter, DataSource.All, 10000);
				if(listOfHeaders != null && !listOfHeaders.isEmpty()) {
					allHeaders.addAll(listOfHeaders);
				}
			}
			
			NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Reporting {0} MPLHeaderUI events",allHeaders.size());
			for(MPLHeaderUI header : allHeaders) {
				
				String integrationFlow = header.getIntegrationFlow();
				if(integrationFlow == null || integrationFlow.isEmpty()) {
					integrationFlow = "UnknownFlow";
				}

				String status = header.getStatus();
				if(status == null || status.isEmpty()) {
					status = "UnknownStatus";
				}

				String appId = header.getApplicationId();
				if(appId == null || appId.isEmpty()) {
					appId = "UnknownAppId";
				}
				
				String corrId = header.getCorrelationId();
				if(corrId == null || corrId.isEmpty()) {
					corrId = "UnknownCorrelationId";
				}
				
				String msgGuid = header.getMessageGuid();
				if(msgGuid == null || msgGuid.isEmpty()) {
					msgGuid = "UnknownMessageGuid";
				}
				
				LOGGER.log(Level.INFO,"IntegrationFlow: {0}, Status: {1}, ApplicationId: {2}, CorrelationId: {3}, MessageGuid: {4}", integrationFlow, status, appId, corrId, msgGuid);
			}
		} catch (Exception e) {
			NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, e, "Failed to retrieve MPLHeaders due to error");
		}
		startTime = endTime;
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "End Call to GatewayMonitor.run()");
	}
	
}
