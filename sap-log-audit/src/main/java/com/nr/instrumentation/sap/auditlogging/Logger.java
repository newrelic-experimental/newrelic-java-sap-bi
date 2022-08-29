package com.nr.instrumentation.sap.auditlogging;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
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
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;
import com.sap.engine.interfaces.messaging.api.Action;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageDirection;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.MessageStatus;
import com.sap.engine.interfaces.messaging.api.MessageType;
import com.sap.engine.interfaces.messaging.api.Party;
import com.sap.engine.interfaces.messaging.api.Service;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogEntry;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;
import com.sap.engine.interfaces.messaging.api.event.FinalMessageStatusData;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;
import com.sap.engine.messaging.impl.api.MessageFactoryImpl;
import com.sap.engine.messaging.impl.core.event.FinalMessageStatusDataImpl;
import com.sap.engine.messaging.impl.core.queue.QueueMessage;
import com.sap.engine.messaging.runtime.MessagingSystem;

public class Logger implements Runnable, AgentConfigListener {

	public static boolean initialized = false;
	private static ExtendedLogger LOGGER;
	private static ExtendedLogger LOGGER2;
	private static boolean simulate = false;
	private int count = 0;
	private static Logger instance = null;
	private static final String SIMULATE = "SAP.auditlog.simulate";
	private static final String AUDITLOGFILENAME = "SAP.auditlog.log_file_name";
	private static final String AUDITLOGROLLOVERINTERVAL = "SAP.auditlog.log_file_interval";
	private static final String AUDITLOGIGNORES = "SAP.auditlog.ignores";
	private static final String AUDITLOGROLLOVERSIZE = "SAP.auditlog.log_size_limit";
	private static final String AUDITLOGMAXFILES = "SAP.auditlog.log_file_count";
	private static HashSet<String> auditLogIgnores = new HashSet<String>();
	private static final String MESSAGEOGFILENAME = "SAP.messagelog.log_file_name";
	private static final String MESSAGELOGROLLOVERINTERVAL = "SAP.messagelog.log_file_interval";
	private static final String MESSAGELOGIGNORES = "SAP.messagelog.ignores";
	private static final String MESSAGELOGROLLOVERSIZE = "SAP.messagelog.log_size_limit";
	private static final String MESSAGELOGMAXFILES = "SAP.messagelog.log_file_count";

	private static ScheduledExecutorService executor;
	private static Properties messageMappings = null;
	private static final Random random = new Random();

	@SuppressWarnings("rawtypes")
	public static void init() {
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Initializing Logger");
		initialized = true;
		Config agentConfig = NewRelic.getAgent().getConfig();
		Object obj = agentConfig.getValue(SIMULATE);
		if(obj != null) {
			if(obj instanceof Boolean) {
				simulate = (Boolean)obj;
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set to {0}",simulate);
			} else if(obj instanceof String) {
				simulate = Boolean.getBoolean((String)obj);
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set to {0}",simulate);
			} else {
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set but was not Boolean or String",obj.getClass().getName());
			}
		} else {
			NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate not set ");
		}

		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		builder.setStatusLevel(Level.INFO);

		// Set property Defaults
		obj = agentConfig.getValue(AUDITLOGROLLOVERINTERVAL);
		int rolloverMinutes = 0;
		if(obj != null) {
			rolloverMinutes = (int)obj; 
		}
		
		String cronString;
		
		if(rolloverMinutes > 0) {
			cronString = "0 0/"+rolloverMinutes+" * * * ?";
		} else {
			cronString = "0 0 * * * ?";
		}
		obj = agentConfig.getValue(AUDITLOGROLLOVERSIZE);
		String rolloverSize = "10k";
		if(obj != null) {
			rolloverSize = (String)obj; 
		}

		ComponentBuilder triggeringPolicy = builder.newComponent("Policies")
				.addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", cronString))
				.addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", rolloverSize));


		AppenderComponentBuilder auditfile = builder.newAppender("rolling", "RollingFile");
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();

		// Set property Defaults
		obj = agentConfig.getValue(AUDITLOGFILENAME);
		String auditLogFileName = "audit.log";
		if(obj != null) {
				auditLogFileName = (String)obj; 
		} 
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINER, "Auditlogfilename {0}", auditLogFileName);
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINER, "New Relic Dir {0}", newRelicDir.getAbsolutePath());
		obj = agentConfig.getValue(AUDITLOGMAXFILES);
		int auditLogMaxFiles = 0;
		if(obj != null) {
				auditLogMaxFiles = (int)obj; 
		} 
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINER, "Auditlogmaxfiles {0}", auditLogMaxFiles);

		auditfile.addAttribute("fileName", auditLogFileName);
		auditfile.addAttribute("filePattern", auditLogFileName +  ".%i");
		auditfile.addAttribute("max", auditLogMaxFiles);
		LayoutComponentBuilder standard = builder.newLayout("PatternLayout");
		standard.addAttribute("pattern", "%msg%n%throwable");
		auditfile.add(standard);
		auditfile.addComponent(triggeringPolicy);

		ComponentBuilder rolloverStrategy = builder.newComponent("DefaultRolloverStrategy").addAttribute("max", auditLogMaxFiles);

		auditfile.addComponent(rolloverStrategy);

		builder.add(auditfile);

		builder.add(builder.newLogger("AuditLog",Level.INFO)
				.add(builder.newAppenderRef("rolling"))
				.addAttribute("additivity", false));

		
		obj = agentConfig.getValue(MESSAGEOGFILENAME);
		AppenderComponentBuilder msgfile = builder.newAppender("rolling2", "RollingFile");
		String messageFileName = newRelicDir.getAbsolutePath() + File.separator + "sap-messages.log";
		if(obj != null) {
			messageFileName = (String)obj;
		}
		
		obj = agentConfig.getValue(MESSAGELOGMAXFILES);
		int messageLogMaxFiles = 3;
		if(obj != null) {
			messageLogMaxFiles = (Integer)obj;
		}
		
		obj = agentConfig.getValue(MESSAGELOGROLLOVERSIZE);
		rolloverSize = "10k";
		if(obj != null) {
			rolloverSize = (String)obj; 
		} 

		obj = agentConfig.getValue(MESSAGELOGROLLOVERINTERVAL);
		rolloverMinutes = 0;
		if(obj != null) {
			rolloverMinutes = (int)obj; 
		}
		
		
		if(rolloverMinutes > 0) {
			cronString = "0 0/"+rolloverMinutes+" * * * ?";
		} else {
			cronString = "0 0 * * * ?";
		}

		ComponentBuilder triggeringPolicy2 = builder.newComponent("Policies")
				.addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", cronString))
				.addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", rolloverSize));

		msgfile.addAttribute("fileName", messageFileName);
		msgfile.addAttribute("filePattern", messageFileName + ".%i");
		msgfile.addAttribute("max",messageLogMaxFiles);
		msgfile.add(standard);
		msgfile.addComponent(triggeringPolicy2);
		
		ComponentBuilder rolloverStrategy2 = builder.newComponent("DefaultRolloverStrategy").addAttribute("max", messageLogMaxFiles);

		msgfile.addComponent(rolloverStrategy2);
		
		builder.add(msgfile);
		
		builder.add(builder.newLogger("MessagesLog",Level.INFO)
				.add(builder.newAppenderRef("rolling2"))
				.addAttribute("additivity", false));

		BuiltConfiguration config = builder.build();

		LoggerContext ctx = Configurator.initialize(config);

		LOGGER = ctx.getLogger("AuditLog");
		
		LOGGER2 = ctx.getLogger("MessagesLog");
		
		instance= new Logger();
		ServiceFactory.getConfigService().addIAgentConfigListener(instance);
		if(simulate) {
			executor = Executors.newSingleThreadScheduledExecutor();
			executor.scheduleAtFixedRate(instance, 15L, 15L, TimeUnit.SECONDS);
		}

		String ignores = agentConfig.getValue(AUDITLOGIGNORES);
		NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINER, "Auditlogignores {0}", ignores);
		auditLogIgnores.clear();
		if(ignores != null && !ignores.isEmpty()) {
			StringTokenizer st = new StringTokenizer(ignores, ",");
			while(st.hasMoreTokens()) {
				String token = st.nextToken();
				auditLogIgnores.add(token);
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Will ignore log entries for textKey: {0}", token);
			}
		}

		InputStream is = null;
		boolean loaded = false;

		try {
			NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Attempting to load message mappings from embedded property file");
			is = instance.getClass().getResourceAsStream("/com/sap/engine/interfaces/messaging/api/i18n/rb_AuditLogResource_en.properties");
			if(is != null) {
				if(messageMappings == null) {
					messageMappings = new Properties();
				}
				messageMappings.load(is);
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Loaded {0} message mappings", messageMappings.size());
				loaded = true;
			}
		} catch(Exception e) {
			NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, e, "Failed to load message mappings from embedded property file");
		}

		if(!loaded) {
			try {
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Attempting to load message mappings from string");
				if(messageMappings == null) {
					messageMappings = new Properties();
				}
				StringReader reader = new StringReader(props);
				messageMappings.load(reader);
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, "Loaded {0} message mappings", messageMappings.size());

			} catch (Exception e) {
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINE, e, "Failed to load message mappings");
			}

		}
	}
	
	public static void log(Message message, MessageStatus status, String errorCode) {
		MessageKey msgKey = message.getMessageKey();
		if(msgKey != null) {
			String msgId = msgKey.getMessageId();
			String msgDir = msgKey.getDirection().toString();
			
			if (msgId != null && msgDir != null) {
				String logMsg = "Message Id: " + msgId + ",Message Direction: " + msgDir + ",Message Status: " + status + ",Error Code: " + errorCode;
				LOGGER2.log(Level.INFO, logMsg);
			}
		}
		
	}
	
	public static void log(MessageKey msgKey, QueueMessage msg) {
		if(msgKey != null) {
			String msgId = msgKey.getMessageId();
			String msgDir = msgKey.getDirection().toString();
			
			if (msgId != null && msgDir != null) {
				MessageStatus status = msg.getMessageStatus();
				String errorCode = msg.getErrorCode();
				
				String logMsg = "Message Id: " + msgId + ",Message Direction: " + msgDir + ",Message Status: " + status + ",Error Code: " + errorCode + ",Retries: " + msg.getRetries() + ",Failed: "+msg.getTimesFailed()+",Message Size: "+msg.getMessageSize();
				LOGGER2.log(Level.INFO, logMsg);
			}
		}
		
	}
	
	public static void log(MessageKey msgKey, FinalMessageStatusData data, Integer timesFailed) {
		if(msgKey != null) {
			String msgId = msgKey.getMessageId();
			String msgDir = msgKey.getDirection().toString();
			
			if (msgId != null && msgDir != null) {
				MessageStatus status = data.getMessageStatus();
				String failed;
				if(timesFailed != null) {
					failed = timesFailed.toString();
				} else {
					failed = "0";
				}
				
				String logMsg = "Message Id: " + msgId + ",Message Direction: " + msgDir + ",Message Status: " + status + ",Failed: "+failed;
				LOGGER2.log(Level.INFO, logMsg);
			}
		}
	}

	public static void log(LinkedList<AuditLogEntry> list) {
		Iterator<AuditLogEntry> iterator = list.iterator();
		while(iterator.hasNext()) {
			AuditLogEntry entry = iterator.next();
			MessageKey msgKey = entry.getMsgKey();
			AuditLogStatus status = entry.getStatus();
			String textKey = entry.getTextKey();
			String[] params = entry.getParams();
			String timeStamp = entry.getTimestampAsString();
			log(msgKey, status, timeStamp, textKey, params);
		}
	}

	public static void log(AuditLogEntry[] entries) {
		for(AuditLogEntry entry : entries) {
			MessageKey msgKey = entry.getMsgKey();
			AuditLogStatus status = entry.getStatus();
			String textKey = entry.getTextKey();
			String[] params = entry.getParams();
			String timeStamp = entry.getTimestampAsString();
			log(msgKey, status, timeStamp, textKey, params);
		}
	}

	private static String convert(String s) {
		String t = null;
		if(messageMappings != null) {
			t = messageMappings.getProperty(s);
		}


		return t != null ? t : s;
	}

	public static void log(MessageKey msgKey, AuditLogStatus status, String ts,String origTextKey, String... params) {
		String textKey = convert(origTextKey);
		int size = 0;
		if (params != null) {
			size = params.length;
		} else {
//			NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINEST, "LogAudit params is null");
		}
		
		String auditStatus;
		if(isIgnored(textKey)) {
			return;
		}
		if(status == AuditLogStatus.SUCCESS) {
			auditStatus = "Success";
		} else if(status == AuditLogStatus.WARNING) {
			auditStatus = "Warning";
		} else if(status == AuditLogStatus.ERROR) {
			auditStatus = "Error";
		} else {
			auditStatus = "Unknown";
		}
		switch(size) {
		case 0:
			String logEntry0 = "Timestamp: "+ts+"; MessageKey: "+msgKey.getMessageId()+", "+msgKey.getDirection()+"; Status: "+auditStatus+"; TextKey: "+textKey;
			LOGGER.log(Level.INFO, logEntry0);
			break;
		case 1:
			String temp = textKey.replace("{0}", params[0]);
			String logEntry1 = "Timestamp: "+ts+"; MessageKey: "+msgKey.getMessageId()+", "+msgKey.getDirection()+"; Status: "+auditStatus+"; TextKey: "+temp;
			LOGGER.log(Level.INFO, logEntry1);
			break;
		case 2:
			String temp2 = textKey.replace("{0}", params[0]).replace("{1}", params[1]);
			String logEntry2 = "Timestamp: "+ts+"; MessageKey: "+msgKey.getMessageId()+", "+msgKey.getDirection()+"; Status: "+auditStatus+"; TextKey: "+temp2;
			LOGGER.log(Level.INFO, logEntry2);
			break;
		case 3:
			String temp3 = textKey.replace("{0}", params[0]).replace("{1}", params[1]).replace("{2}", params[2]);
			String logEntry3 = "Timestamp: "+ts+"; MessageKey: "+msgKey.getMessageId()+", "+msgKey.getDirection()+"; Status: "+auditStatus+"; TextKey: "+temp3;
			LOGGER.log(Level.INFO, logEntry3);
			break;
		case 4:
			String temp4 = textKey.replace("{0}", params[0]).replace("{1}", params[1]).replace("{2}", params[2]).replace("{3}", params[3]);
			String logEntry4 = "Timestamp: "+ts+"; MessageKey: "+msgKey.getMessageId()+", "+msgKey.getDirection()+"; Status: "+auditStatus+"; TextKey: "+temp4;
			LOGGER.log(Level.INFO, logEntry4);
			break;
		case 5:
			String temp5 = textKey.replace("{0}", params[0]).replace("{1}", params[1]).replace("{2}", params[2]).replace("{3}", params[3]).replace("{4}", params[4]);
			String logEntry5 = "Timestamp: "+ts+"; MessageKey: "+msgKey.getMessageId()+", "+msgKey.getDirection()+"; Status: "+auditStatus+"; TextKey: "+temp5;
			LOGGER.log(Level.INFO, logEntry5);
			break;

		}

	}

	public static void log(MessageKey msgKey, AuditLogStatus status, String origTextKey, Object... params) {
		String textKey = convert(origTextKey);
		int size = 0;
		if (params != null) {
			size = params.length;
		} else {
//			NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINEST, "LogAudit params is null");
		}
		
		String auditStatus;
		if(isIgnored(textKey)) {
			return;
		}
		if(status == AuditLogStatus.SUCCESS) {
			auditStatus = "Success";
		} else if(status == AuditLogStatus.WARNING) {
			auditStatus = "Warning";
		} else if(status == AuditLogStatus.ERROR) {
			auditStatus = "Error";
		} else {
			auditStatus = "Unknown";
		}
		switch(size) {
		case 0:
			String logEntry0 = "MessageKey: "+msgKey.getMessageId()+", "+msgKey.getDirection()+"; Status: "+auditStatus+"; TextKey: "+textKey;
			LOGGER.log(Level.INFO, logEntry0);
			break;
		case 1:
			String temp = textKey.replace("{0}", params[0].toString());
			String logEntry1 = "MessageKey: "+msgKey.getMessageId()+", "+msgKey.getDirection()+"; Status: "+auditStatus+"; TextKey: "+temp;
			LOGGER.log(Level.INFO, logEntry1);
			break;
		case 2:
			String temp2 = textKey.replace("{0}", params[0].toString()).replace("{1}", params[1].toString());
			String logEntry2 = "MessageKey: "+msgKey.getMessageId()+", "+msgKey.getDirection()+"; Status: "+auditStatus+"; TextKey: "+temp2;
			LOGGER.log(Level.INFO, logEntry2);
			break;
		case 3:
			String temp3 = textKey.replace("{0}", params[0].toString()).replace("{1}", params[1].toString()).replace("{2}", params[2].toString());
			String logEntry3 = "MessageKey: "+msgKey.getMessageId()+", "+msgKey.getDirection()+"; Status: "+auditStatus+"; TextKey: "+temp3;
			LOGGER.log(Level.INFO, logEntry3);
			break;
		case 4:
			String temp4 = textKey.replace("{0}", params[0].toString()).replace("{1}", params[1].toString()).replace("{2}", params[2].toString()).replace("{3}", params[3].toString());
			String logEntry4 = "MessageKey: "+msgKey.getMessageId()+", "+msgKey.getDirection()+"; Status: "+auditStatus+"; TextKey: "+temp4;
			LOGGER.log(Level.INFO, logEntry4);
			break;
		case 5:
			String temp5 = textKey.replace("{0}", params[0].toString()).replace("{1}", params[1].toString()).replace("{2}", params[2].toString()).replace("{3}", params[3].toString()).replace("{4}", params[4].toString());
			String logEntry5 = "MessageKey: "+msgKey.getMessageId()+", "+msgKey.getDirection()+"; Status: "+auditStatus+"; TextKey: "+temp5;
			LOGGER.log(Level.INFO, logEntry5);
			break;

		}

	}

	public static void log(AuditLogStatus status, String origTextKey, Object... params) {
		String textKey = convert(origTextKey);
		int size = 0;
		if (params != null) {
			size = params.length;
		} else {
//			NewRelic.getAgent().getLogger().log(java.util.logging.Level.FINEST, "LogAudit params is null");
		}
		
		String auditStatus;
		if(isIgnored(textKey)) {
			return;
		}
		if(status == AuditLogStatus.SUCCESS) {
			auditStatus = "Success";
		} else if(status == AuditLogStatus.WARNING) {
			auditStatus = "Warning";
		} else if(status == AuditLogStatus.ERROR) {
			auditStatus = "Error";
		} else {
			auditStatus = "Unknown";
		}
		switch(size) {
		case 0:
			String logEntry0 = "MessageKey: Unknown; Status: "+auditStatus+"; TextKey: "+textKey;
			LOGGER.log(Level.INFO, logEntry0);
			break;
		case 1:
			String temp = textKey.replace("{0}", params[0].toString());
			String logEntry1 = "MessageKey: Unknown; Status: "+auditStatus+"; TextKey: "+temp;
			LOGGER.log(Level.INFO, logEntry1);
			break;
		case 2:
			String temp2 = textKey.replace("{0}", params[0].toString()).replace("{1}", params[1].toString());
			String logEntry2 = "MessageKey: Unknown; Status: "+auditStatus+"; TextKey: "+temp2;
			LOGGER.log(Level.INFO, logEntry2);
			break;
		case 3:
			String temp3 = textKey.replace("{0}", params[0].toString()).replace("{1}", params[1].toString()).replace("{2}", params[2].toString());
			String logEntry3 = "MessageKey: Unknown; Status: "+auditStatus+"; TextKey: "+temp3;
			LOGGER.log(Level.INFO, logEntry3);
			break;
		case 4:
			String temp4 = textKey.replace("{0}", params[0].toString()).replace("{1}", params[1].toString()).replace("{2}", params[2].toString()).replace("{3}", params[3].toString());
			String logEntry4 = "MessageKey: Unknown; Status: "+auditStatus+"; TextKey: "+temp4;
			LOGGER.log(Level.INFO, logEntry4);
			break;
		case 5:
			String temp5 = textKey.replace("{0}", params[0].toString()).replace("{1}", params[1].toString()).replace("{2}", params[2].toString()).replace("{3}", params[3].toString()).replace("{4}", params[4].toString());
			String logEntry5 = "MessageKey: Unknown; Status: "+auditStatus+"; TextKey: "+temp5;
			LOGGER.log(Level.INFO, logEntry5);
			break;

		}

	}

	public void run() {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		AuditLogStatus status;
		String textKey;
		UUID uuid = UUID.randomUUID();
		MessageKey msgKey = new MessageKey(uuid.toString(), count % 2 == 0 ? MessageDirection.INBOUND : MessageDirection.OUTBOUND);
		int mod = count % 3;
		switch(mod) {
		case 0:
			textKey = "RESTIN_READ_XML";
			status = AuditLogStatus.SUCCESS;
			log(msgKey, status, ts.toString(), textKey);
			break;
		case 1:
			textKey = "MPA_CHANNEL_ERROR";
			status = AuditLogStatus.WARNING;
			log(msgKey, status, ts.toString(), textKey, "Not available");
			break;
		case 2:
			textKey = "MAPPING_ERROR";
			status = AuditLogStatus.ERROR;
			log(msgKey, status, ts.toString(), textKey, "MyMapping","Could not find mapping");
			break;

		}
		String[] protocols = MessagingSystem.getSupportedProtocols();
		MessageType[] msgTypes = MessageType.getMessageTypes();
		int len = protocols.length;
		
		switch(mod) {
		case 0:
			TransportableMessage msg = null;
			for(int i=0;i<len && msg == null;i++) {
				MessageFactoryImpl msgFactory = new MessageFactoryImpl(protocols[i]);
				try {
					Message message = msgFactory.createMessage(new Party("FromParty"), new Party("ToParty"), new Service("FromService"), new Service("ToService"), new Action("Action1"));
					if(message != null) {
						if(message instanceof TransportableMessage) {
							msg = (TransportableMessage)message;
						}
					}
				} catch (MessagingException e) {
				}
				
			}
			if(msg != null) {
				int index = random.nextInt(msgTypes.length);
				QueueMessage qMessage = new QueueMessage(msg, msgTypes[index], "MyConnection");
				MessageKey msgKey1 = msg.getMessageKey();
				log(msgKey1, qMessage);
			}
			break;
		case 1:
			try {
				int index = random.nextInt(len);
				MessageFactoryImpl msgFactory = new MessageFactoryImpl(protocols[index]);
				Message message = msgFactory.createMessage(new Party("FromParty"), new Party("ToParty"), new Service("FromService"), new Service("ToService"), new Action("Action1"));
				if(message != null) {
					MessageStatus[] statuses = MessageStatus.getMessageStatusList();
					int index2 = random.nextInt(statuses.length);
					log(message,statuses[index2],"ErrorCode1");
				}
			} catch (MessagingException e) {
			}
			break;
		case 2:
			TransportableMessage msg2 = null;
			for(int i=0;i<len && msg2 == null;i++) {
				MessageFactoryImpl msgFactory = new MessageFactoryImpl(protocols[i]);
				try {
					Message message = msgFactory.createMessage(new Party("FromParty"), new Party("ToParty"), new Service("FromService"), new Service("ToService"), new Action("Action1"));
					if(message != null) {
						if(message instanceof TransportableMessage) {
							msg2 = (TransportableMessage)message;
						}
					}
				} catch (MessagingException e) {
				}
				
			}
			if(msg2 != null) {
				int index = random.nextInt(msgTypes.length);
				QueueMessage qMessage = new QueueMessage(msg2, msgTypes[index], "MyConnection");
				FinalMessageStatusDataImpl data = new FinalMessageStatusDataImpl(qMessage);
				Integer failed = new Integer(random.nextInt(4));
				log(data.getMessageKey(),data, failed);
			}
			default:
				
		}
		count++;
	}

	@Override
	public void configChanged(String appName, AgentConfig agentConfig) {
		Object obj = agentConfig.getValue(SIMULATE);
		if(obj != null) {
			if(obj instanceof Boolean) {
				Boolean b = (Boolean)obj;
				if(b != simulate) {
					simulate = (Boolean)obj;
					NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set to {0}",simulate);
					processExecutor(b);
				}
			} else if(obj instanceof String) {
				Boolean b = Boolean.getBoolean((String)obj);
				if(b != simulate) {
					simulate = b;
					NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set to {0}",simulate);
					processExecutor(b);
				}
			} else {
				NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate set but was not Boolean or String",obj.getClass().getName());
			}
		} else {
			NewRelic.getAgent().getLogger().log(java.util.logging.Level.INFO, "Simulate not set ");
		}		
	}

	private static void processExecutor(boolean isStart) {
		if(isStart) {
			// simulatation not running so start
			if(executor == null) {
				executor = Executors.newSingleThreadScheduledExecutor();
			}
			executor.scheduleAtFixedRate(instance, 15L, 15L, TimeUnit.SECONDS);
		} else {
			if(executor != null) {
				executor.shutdown();
				executor = null;
			}
		}
	}

	public static boolean isIgnored(String name) {

		if(name == null || name.isEmpty()) return false;

		for (String ignoreText : auditLogIgnores) {
			if(name.contains(ignoreText)) return true;
		}
		return false;
	}

	private static final String props = "#Tue Jul 14 10:45:20 CEST 2015\n" + 
			"MPA_CHANNEL_ERROR=Could not retrieve channel. Reason\\: {0}\n" + 
			"JMS_CONVERT_XMB2BIN_SUCCESS=XI message converted to binary\n" + 
			"ACTION_USER_RETRY_MESSAGE=Trying to retry message due to administrative action of user \"{0}\"\n" + 
			"JPR_IN_APPL_ACK_SUCCESS=JPR successfully triggered the application acknowledgment\n" + 
			"SEND_ERROR=Returning to application. Exception\\: {0}\n" + 
			"JPR_OUT_RESPONSE_RECEIVED=JPR received the response from the messaging system\n" + 
			"JPR_IN_SYS_ACK_SUCCESS=JPR triggered the system acknowledgment successfully\n" + 
			"AAE_RESPONSE_MAPPING=Executing Response Mapping \"{0}/{1}\" (SWCV {2})\n" + 
			"JWS_IN_SOAP_ACTION=Inbound SOAP operation is {0}\n" + 
			"JPR_IN_APPL_ERROR_ACK_SUCCESS=JPR successfully triggered the application error acknowledgment\n" + 
			"JMS_DUPLICATE_ACKNOWLEDGED=Duplicate message with JMS message ID {0} detected and acknowledged as already processed\n" + 
			"FILE_SUC_039=Confirmation mode 'test' found - file will be resent next time\n" + 
			"FILE_SUC_038=Attempt to delete file \"{0}\" failed. Unable to proceed\\: {1}\n" + 
			"MS_BLACKLIST_QUEUE_INSERT_MSG=Message was not delivered as message size exceeds permitted limit\n" + 
			"FILE_SUC_037=File \"{0}\" successfully deleted on FTP server \"{1}\" after sending and moving to \"{2}\"\n" + 
			"REQUEST_DELIVER_ERROR_FAILED=Delivery of the message to the application using connection {0} failed, due to\\: {1}. Setting message to status failed\n" + 
			"FILE_SUC_033=File \"{0}\" deleted after processing\n" + 
			"FILE_SUC_030=Send text file \"{0}\" from FTP server \"{1}\", size {2} bytes with QoS {3}\n" + 
			"MS_BLACKLIST_NDLV_MSG_FAILOVER=Moved message to the non-delivered state. Reason\\: Blacklisted message (in delivering state) reassigned\n" + 
			"JWS_OUT_SOAP_ACTION=Outbound SOAP operation is {0}\n" + 
			"RESTOUT_PREPARING_MESSAGE_CONTE=Preparing message content\n" + 
			"RECEIVE_ASYNC_ERROR_SUCCESS=Asynchronous error reported.\n" + 
			"PERFORMANCE_CANCEL=CalledPerformanceCancel - Message Key\\: {0}; Stack\\: {1}\n" + 
			"RESTIN_READ_JSON=Read JSON payload\n" + 
			"FILE_SUC_029=Send binary file  \"{0}\" from FTP server \"{1}\", size {2} bytes with QoS {3}\n" + 
			"FILE_SUC_027=Execute OS command \"{0}\"\n" + 
			"JPR_IN_PROCESS_FAULT=Message could not be processed. Reason\\: {0}\n" + 
			"FILE_SUC_025=Confirmation mode 'test' found - file will be resent next time\n" + 
			"ACTION_MESSAGE_ACK_SUCCESS=Acknowledgment Processed Successfully\n" + 
			"FILE_SUC_023=File \"{0}\" archived after processing\n" + 
			"AFW_LMS_EXTRACTOR_ERROR=Error applying extractor of type {0} for attribute {1} on this message due to \\: {2}\n" + 
			"FILE_SUC_020=File \"{0}\" set to ''read-only'' after processing\n" + 
			"XI_SYSTEM_ERROR=Received XI System Error. ErrorCode\\: {0} ErrorText\\: {1} ErrorStack\\: {2}\n" + 
			"BTN_REMOVE_RECAUDIT=Remove Recovered Entries\n" + 
			"SEND_TRANSMIT_SUCCESS=Message was successfully transmitted to endpoint {1} using connection {0}\n" + 
			"FILE_SUC_018=File \"{0}\" deleted after processing\n" + 
			"JDBC_ERR_017=Accessing database connection {0} failed unexpectedly, due to {1}\n" + 
			"FILE_SUC_016=Channel {0}\\: File splitting - read last part of file content\n" + 
			"JDBC_ERR_016=Accessing database connection {0} failed, due to {1}\n" + 
			"SEND_TRYING=Application attempting to send an {1} message asynchronously using connection {0}\n" + 
			"FILE_SUC_015=Channel {0}\\: File splitting - read {1} bytes of the file content up to position {2}\n" + 
			"JDBC_ERR_015=Channel {0}\\: Initialization error JDBC adapter sender\\: {1}\n" + 
			"FILE_SUC_014=Send text file \"{0}\", size {1}, encoding {2} with QoS {3}\n" + 
			"FILE_SUC_013=Channel {0}\\: Send binary file  \"{1}\", size {2} with QoS {3}\n" + 
			"FILE_SUC_012=Channel {0}\\: Converted a part of file content to XML format  - message split detected\\: Converted up to position {1}\n" + 
			"ACTION_MESSAGE_RESEND_SUCCESS=Admin action\\: Message resent successfully\n" + 
			"JDBC_ERR_011=Channel {0}\\: Sending query result failed with {1} - retry\n" + 
			"RECEIVE_QUEUE_PUT_TRYING=Using connection {0}. Trying to put the message into the receive queue\n" + 
			"PERFORMANCE_ADD_MP=CalledPerformanceAddMeasuringPoint - Name\\: {0}; Stack\\: {1}\n" + 
			"JPR_IN_SYS_ERROR_ACK_SUCCESS=JPR triggered the system error acknowledgment successfully\n" + 
			"RECEIVE_RETRY=Retrying to deliver message to the application. Retry\\: {0}\n" + 
			"MS_BLACKLIST_MSG_CHKSTATUS_FAIL=Could not check message status. Reason\\: {0}\n" + 
			"PERFORMANCE_UPDATE_HEADER=CalledPerformanceUpdateHeader\n" + 
			"RESTIN_ERROR_SENDING_MESSAGE=Error from message processor\\: {0}\n" + 
			"RECEIVE_DELIVER_ERROR=Delivery of the message to the application using connection {0} failed, due to\\: {1}\n" + 
			"JDBC_ERR_405=Error during execution of batched statement\\: {0}\n" + 
			"JDBC_ERR_404=No 'action' attribute found in XML document (attribute 'action' missing or wrong XML structure)\n" + 
			"JDBC_ERR_403=Unable to execute statement for table or stored procedure. ''{0}'' (Structure ''{2}'') due to {1}\n" + 
			"FILE_ERR_502=Configuration error\\: Channel \"{0}\" is not a receiver type\n" + 
			"JDBC_ERR_402=Structure \"{0}\" processed successfully (\"{1}\")\n" + 
			"JDBC_ERR_401=Unsupported ''action'' attribute value \"{0}\" found in XML document\n" + 
			"JPR_IN_RESPONSE_ERROR=JPR failed to send the response message. Reason\\: {0}\n" + 
			"AAE_MAPPING_ERROR=Mapping \"{0}/{1}\" failed to execute\\: {2}\n" + 
			"JDBC_ERR_008=Channel {0}\\: SQL Exception retrieving resultset metadata\\: {1}\n" + 
			"JDBC_ERR_007=Channel {0}\\: SQL Exception during query or update\\: {1}\n" + 
			"JDBC_ERR_006=Channel {0}\\: Exception during query or update\\: {1}\n" + 
			"MS_BLACKLIST_ADDING_MSG=Adding message to blacklist. Reason\\: {0}\n" + 
			"SEND_TRANSMIT_SCHEDULE_ERROR=Scheduling the message to be resent at {0} failed, due to\\: {1}\n" + 
			"SEND_QUEUE_GET_SUCCESS=The message was successfully retrieved from the send queue\n" + 
			"RECEIVE_QUEUE_GET_SUCCESS=The message was successfully retrieved from the receive queue\n" + 
			"ACTION_MESSAGE_RESEND_TRYING=Admin action\\: Trying to resend message\n" + 
			"JDBC_ERR_001=Channel {0}\\: Loading JDBC driver {1} failed\\: {2}\n" + 
			"RESTOUT_HTTP_HEADER=Set HTTP Header \"{0}\" to \"{1}\"\n" + 
			"MS_BLACKLIST_CHECK_MSG_FAIL=Could not check if message is blacklisted. Reason\\: {0}\n" + 
			"PERFORMANCE_UPDATE_RETRY=CalledPerformanceUpdateRetry\n" + 
			"MPEB_ENTERED=Message entered AF MP exit bean and will now be passed to the JCA adapter\n" + 
			"CM_Transformation_license_error=Could not check CDE License\n" + 
			"MS_VERS_LOCK_RELEASE=Version lock released\n" + 
			"RESTIN_DISCOVERED_CHARSET=Discovered character set\\: {0}\n" + 
			"PERFORMANCE_END=CalledPerformanceStop - Stack\\: {0}\n" + 
			"STARTUP_BE_MESSAGE_ERROR=System startup\\: Best effort message was not delivered. Setting status to failed\n" + 
			"MS_BLACKLIST_ADDED_MSG=Message added to blacklist\n" + 
			"AAE_NO_REC_IGNORE=No receiver could be determined. According to the configuration, this will be ignored\n" + 
			"RESTIN_READ_XML=Read XML payload\n" + 
			"ACTION_MESSAGE_REDELIVER_TRYING=Admin action\\: Trying to redeliver message\n" + 
			"AFW_LMS_EXTRACTOR_NOVALUES=Extractor of type {0} for attribute {1} produced no values from this message\n" + 
			"JMS_RECEIVE_SUCCESS=JMS message delivered to XI\n" + 
			"XML_MSG_VAL_SUCCESS=Validation for message with message ID ''{0}'' successful\n" + 
			"JPR_IN_PROCESS_WARNING=JPR could not process the message at this time. Reason\\: {0}\n" + 
			"JWS_IN_PROPAGATE_PRINCIPAL=Inbound principal propagation is enabled\n" + 
			"RECEIVE_DELIVER_SUCCESS=The message was successfully delivered to the application using connection {0}\n" + 
			"RECEIVE_DELIVER_SCHEDULE_SUCCESS=The asynchronous message was successfully scheduled to be delivered at {0}\n" + 
			"ACTION_MESSAGE_COPY_SUCCESS=Admin action\\: Message was successfully copied from message {0} by user {1}\n" + 
			"REQUEST=Message was received by the messaging system. Protocol\\: {0} URL\\: {1} Credential ({2})\\: {3}\n" + 
			"SEND_ASYNC_ERROR_SUCCESS=Asynchronous error notification successfully delivered\n" + 
			"FILE_ERR_048=Channel {0}\\: Sending file \"{1}\" failed\\: File has been modified during processing. Expected {2} bytes, found {3} bytes - Retrying\n" + 
			"FILE_ERR_047=Sending file failed with {0} - non recovarable error, quit adapter run (reconfigure/reactivate configuration or restart J2EE SAP file adapter service)\n" + 
			"FILE_ERR_046=Channel {0}\\: File send failed with {1} - non-recoverable error, quit adapter run (reconfigure/reactivate config. or restart J2EE SAP file adapter service)\n" + 
			"FILE_ERR_044=Channel {0}\\: Conversion of complete file content to XML format failed around position {1} with {2}\n" + 
			"RFC_OUT_BAPI_STRUCT=BAPI response is a structure of type {0}\n" + 
			"FILE_ERR_042=Error executing OS command \"{0}\"\n" + 
			"MS_SKIP_OUTBOUND_RETRY_ACKED=Ignoring exception because message already acknowledged\n" + 
			"FILE_ERR_040=Unknown confirmation mode \"{0}\" found. Mode ignored\n" + 
			"PERFORMANCE_IMPLICIT=Performance API used without first calling start method. Implicitly creating new performance data entry for message key {0}. - Stack\\: {0}\n" + 
			"RESTOUT_RECEIVED_ASYNC_MESSAGE=Received asynchronous message\n" + 
			"SEND_SUCCESS=The application sent the message asynchronously using connection {0}. Returning to application\n" + 
			"AAE_NO_REC_DEFAULT=No receiver could be determined. According to the configuration, the default receiver {0} will be used\n" + 
			"MPEB_CONN_ERROR=Message could not be forwarded to the JCA adapter. Failed to create JCA connection. Reason\\: {0}\n" + 
			"JWS_OUT_SEND_ASYNC_MSG=Sending asynchronous message to endpoint {0}\n" + 
			"CALL_SUCCESS=Application sent message synchronously using connection {0}. Returning to application\n" + 
			"JWS_OUT_SEND_MSG_SUCCESS=Outbound message sent succesfully\n" + 
			"RECOVER_EO_SCHEDULER_SUCCESS=System recovery\\: Message successfully loaded to the scheduler. Next delivery scheduled for {0}\n" + 
			"CALL_SYNC_ERROR_RETURN=Returning synchronous error notification to calling application\\: {0}\n" + 
			"FILE_ERR_036=Confirmation failed\\: Moving sent file \"{0}\" to archive failed with {1}\n" + 
			"FILE_ERR_035=File \"{0}\"\\: Archiving file \"{1}\" after processing failed with {2} - retrying\n" + 
			"FILE_ERR_034=File \"{0}\"\\: Deleting after processing failed - retrying\n" + 
			"FILE_ERR_032=Sending file failed with {0} - continue processing\n" + 
			"XI_DUP_DET_RECEIVED=Duplicate detected by receiver. Stopping retry attempts\n" + 
			"FILE_ERR_031=Sending file failed with {0} - continue processing\n" + 
			"MS_COMPRESS=Compressed the message from {0} to {1} bytes in {2} milliseconds\n" + 
			"AFW_DELIVER_ERROR=Could not retrieve channel ID\n" + 
			"RESTIN_MAPPED_INTERFACE=Mapped message to {0}\\:{1}. Matching rule\\: {2}->{3}\n" + 
			"REQUEST_DELIVER_ERROR=Delivery of the message to the application using connection {0} failed, due to\\: {1}\n" + 
			"JMS_SEND_LOSS_POSSIBLE_WARNING=Configured to mark message as delivered to JMS. Duplicate messages not possible. Track XI and JMS message to ensure JMS delivered to JMS provider\n" + 
			"AAE_NO_REC_ERROR=No receiver could be determined. According to the configuration this will result in an error\n" + 
			"QUEUE_PUT_SUCCESS=Message successfully put into the queue\n" + 
			"STATUS_SET_SUCCESS=Message status set to {0}\n" + 
			"MS_BLACKLIST_ADDING_MSG_START=Adding message to blacklist. Reason\\: Message (in delivering state) found during startup\n" + 
			"JMS_SEND_NOCORRELATION=New JMS message cannot be correlated with the XI message although it is configured\n" + 
			"MS_RESTART_JOB=Message restart triggered by restart job\\: {0}\n" + 
			"FILE_ERR_028=Error executing OS command \"{0}\"\n" + 
			"MAPPING_ERROR=Attempt to execute mapping {0} failed. Reason\\: {1}\n" + 
			"ACTION_MESSAGE_ACK_TRYING=Acknowledgement Received\n" + 
			"FILE_ERR_026=Unknown confirmation mode \"{0}\" found. Mode ignored\n" + 
			"FILE_ERR_024=File \"{0}\"\\: Archiving after processing failed - retrying\n" + 
			"JWS_OUT_PROPAGATE_PRINCIPAL=Outbound principal propagation is enabled\n" + 
			"FILE_ERR_022=File confirmation failed (unknown attribute \"{0}\" - retrying\n" + 
			"FILE_ERR_021=File \"{0}\"\\: Setting to ''read-only'' after processing failed - retrying\n" + 
			"MS_EOIO_LOCK_ERROR2=Multiple EOIO contexts cannot be used within one transaction. Previous\\: {0} Current\\: {1}\n" + 
			"MS_EOIO_STATUS_CHECK_ERROR=Sequencer failed to check the status of message {0} in sequence {1}({2}). The EOIO sequence may stop processing. Reason\\: {3}\n" + 
			"RETURN_ERROR=Failed to pass return message for message {0} to the waiting \"call\" thread; Already expired\n" + 
			"QUEUE_EXIT=Terminating queue worker-thread due to fatal error\\: {0}\n" + 
			"MS_BLACKLIST_ADDING_MSG_FAILOVER=Adding message to blacklist. Reason\\: Message (in delivering state) reassigned\n" + 
			"MS_BLACKLIST_REMOVING_MSG=Removing message from blacklist. Reason\\: {0}\n" + 
			"ACTION_MESSAGE_COPY_ERROR=Admin action\\: Copying the message failed due to\\: {0}\n" + 
			"MS_RECOVER_JOB=Recovering from database connection loss. Message loaded into queue by recover job\\: {0}\n" + 
			"XI_PARSE_RESP_ERROR=Attempt to parse XI system response failed\n" + 
			"AAE_MSG_SPLIT_CHILD=Message Split to Message {0}\n" + 
			"FILE_ERR_019=File \"{0}\"\\: Deleting after processing failed - retrying\n" + 
			"JMS_RECEIVE_ERROR=JMS message with ID {0} cannot be processed due to an error in the XI adapter framework. Exception\\: {1}\n" + 
			"XML_BCK_VAL=Backward validation is enabled\n" + 
			"FILE_ERR_016=Channel {0}\\: Sending file failed with {1} - continue processing\n" + 
			"JWS_OUT_SEND_MSG_ERROR=Error while sending message\\: {0}\n" + 
			"ACTION_MESSAGE_FAIL_SUCCESS=Admin action\\: The message was successfully set to status 'failed'\n" + 
			"FILE_ERR_015=Channel {0}\\: Sending file failed with {1} - continue processing\n" + 
			"FILE_ERR_011=Channel {0}\\: Conversion of a part of file content to XML format failed around position {1} with {2}\n" + 
			"FILE_ERR_010=Channel {0}\\: File sender adapter stopped due to error\\: {1}\n" + 
			"RETURN_SUCCESS=Return message for message {0} passed to the waiting \"call\" thread\n" + 
			"MAPPING_SUCCESS=Mapping {0} executed\n" + 
			"XI_SOAP_FAULT=SOAPFault received from Integration Server. ErrorCode/Category\\: {0}; Params\\: {1}; AdditionalText\\: {2}; ApplicationFaultMessage\\: {3}; ErrorStack\\: {4}\n" + 
			"CALL_QUEUE_GET_SUCCESS=Message retrieved from call queue\n" + 
			"MS_TERM_SEQ_FAIL=Setting message status to {0} because of terminated sequence\n" + 
			"JDBC_SUC_209=Empty document found\n" + 
			"AAE_MSG_SPLIT_ERROR=Failed to Send Split Messages\\: {0}\n" + 
			"JDBC_SUC_205=Database request processed successfully\n" + 
			"ACTION_MESSAGE_RESEND_ERROR=Admin action\\: Resending the message failed, due to\\: {0}\n" + 
			"JDBC_SUC_204=JDBC Adapter Receiver Channel {0}\\: Processing started; party {1} / service {2}\n" + 
			"JDBC_SUC_203=JDBC Adapter Receiver Channel {0}\\: Message already processed - skip\n" + 
			"FILE_ERR_006=Channel {0}\\: \"{1}\"  - this is not a directory\n" + 
			"SEND_RETRY=Retrying to send message. Retry\\: {0}\n" + 
			"FILE_ERR_005=Channel {0}\\: Directory \"{1}\" does not exist\n" + 
			"FILE_ERR_004=Channel {0}\\: Error connecting to ftp server \"{1}\"\\: {2}\n" + 
			"FILE_ERR_003=Channel {0}\\: Error connecting to ftp server \"{1}\"\\: {2}\n" + 
			"FILE_ERR_002=Initialization error in File Adapter Sender Channel {0}\\: \" {1}\n" + 
			"RFC_OUT_SYNC_SUCCESS=RFC Adapter received a synchronous message. Trying to send sRFC for {0}\n" + 
			"RFC_OUT_BAPI_ERROR=BAPI executed with errors\n" + 
			"QUEUE_PUT_EH_REG_ERROR=EventHandler for profile {0} registered. Could not put message back in queue {1}. Reason\\: {2}\n" + 
			"REQUEST_SYNC_ERROR_RETURN_ERROR=Unable to return synchronous error message. Reason\\: {0}\n" + 
			"AFW_LMS_EXTRACTOR_VALUES=Extractor of type {0} for attribute {1} produced values {2} from this message\n" + 
			"STATUS_SET_ERROR=Setting the message status to {0} failed, due to\\: {1}\n" + 
			"JWS_IN_DELIVERY_SEMANTICS=Channel delivery semantics configured for {0}\n" + 
			"REQUEST_QUEUE_PUT_TRYING=Using connection {0}. Trying to put the message into the request queue\n" + 
			"JWS_OUT_SEND_SYNC_MSG=Sending synchronous message to endpoint {0}\n" + 
			"REQUEST_DELIVER_SCHEDULE_SUCCESS=Synchronous message scheduled to be delivered at {0}\n" + 
			"RECAUDIT_REMOVED={0} recovered audit log entries removed from the database\n" + 
			"JPR_IN_APPL_ACK_WARNING=JPR failed to trigger the application acknowledgment. Reason\\: {0}\n" + 
			"RESTOUT_CUSTOM_HANDLING=Custom event handling\\: {0}. Rule\\: {1} {2}\n" + 
			"MS_BLACKLIST_REMOVE_MSG_FAIL=Could not remove message from blacklist. Reason\\: {0}\n" + 
			"RESTOUT_SERVER_RETURNED_CODE=Server returned code\\: {0}\n" + 
			"CALL_QUEUE_PUT_TRYING=Trying to put the message into call queue\n" + 
			"XI_ACK_ERROR=Acknowledgement handling failed for type {0}. Reason\\: {1}\n" + 
			"JPR_IN_PROCESS_SUCCESS=JPR successfully processed the message\n" + 
			"RECEIVE_QUEUE_PUT_ERROR=Putting message into receive queue failed, due to\\: {0}\n" + 
			"SCHEDULE_ERROR=Could not schedule message. Reason\\: {0}\n" + 
			"JPR_IN_PROCESS_ERROR=JPR could not process the message. Reason\\: {0}\n" + 
			"RESTOUT_HTTP_ERROR=HTTP error occurred\\: {0}\n" + 
			"AFW_ERROR=Exception caught by adapter framework\\: {0}\n" + 
			"CALL_ERROR=Returning to application. Exception\\: {0}\n" + 
			"JWS_OUT_PROCESS_RESPONSE=Processing response to synchronous message\n" + 
			"ACTION_USER_FAIL_MESSAGE=Trying to fail message due to administrative action of user \"{0}\"\n" + 
			"RFC_IN_SYNC_SUCCESS=RFC Adapter received an sRFC for {0} from {1}/{2}. Trying to send message synchronously\n" + 
			"ACTION_MESSAGE_REDELIVER_ERROR=Admin action\\: Redelivering the message failed due to\\: {0}\n" + 
			"SEND_ASYNC_ERROR_ERROR=Attempt to deliver asynchronous error notification failed, due to\\: {0}\n" + 
			"RECEIVE_ASYNC_ERROR_ERROR=Failed to report the asynchronous error. Reason\\: {0}\n" + 
			"JDBC_WRN_014=JDBC Adapter Sender Channel {0} not activated\n" + 
			"JDBC_WRN_013=Channel {0}\\: JDBC Sender Adapter polling sequence was interrupted\n" + 
			"JDBC_WRN_012=Channel {0}\\: Empty query result, start poll sequence {1} secs, {2} msecs\n" + 
			"MS_SEQ_REMOTE_TRIGGER_ERROR=Could not trigger MessageSequencers for all available cluster nodes; reason\\: {0}\n" + 
			"JMS_RECEIVE_DUPLICATE_DETECTED=Duplicate JMS message found with JMS message ID {0}\n" + 
			"CM_Transformation_no_XI_license=Current CDE License does not contain SAP Conversion Agent support; contact Informatica support for more details\n" + 
			"STRICT_XML2PLAIN_SUCCESS=Strict XML to plain text conversion completed; converted {0} record(s) from XML to plain text\n" + 
			"JPR_IN_RESPONSE_OUT_ERROR=JPR failed to transfer the response message to the messaging system. Reason\\: {0}\n" + 
			"MS_BULK_EXEC=Bulk containing message is executed\n" + 
			"RECEIVE=Message was received by messaging system. Protocol\\: {0} URL\\: {1}\n" + 
			"AFW_LMS_EXTRACTOR_APPLYING=Applying extractor of type {0} for attribute {1} on this message\n" + 
			"MS_VERS_EDIT_OPEN=Message version opened for edit by user {0}. Message edit lock was acquired\n" + 
			"ACTION_MESSAGE_FAIL_TRYING=Admin action\\: Trying to fail message\n" + 
			"REQUEST_QUEUE_GET_SUCCESS=The message was successfully retrieved from the request queue\n" + 
			"SEND_DELIVER_SUCCESS=The message was successfully delivered\n" + 
			"CALL_QUEUE_PUT_ERROR=Failed to put message into call queue due to\\: {0}\n" + 
			"MS_VERS_EDIT_SUBMIT=Message version {0} submitted by user {1}\n" + 
			"JMS_SEND_DUPLICATE_WARNING=Channel configured to bypass the 'message pending' warning. Duplicate messages possible but message will not be lost. Track XI and JMS messages\n" + 
			"REQUEST_DELIVER_SCHEDULE_ERROR=Attempt to schedule the synchronous message to be delivered at {0} failed, due to\\: {1}\n" + 
			"CALL_TRANSMIT_ERROR=Transmitting the message using connection {0} failed, due to\\: {1}\n" + 
			"MS_VERS_EDIT_SUBMIT_ERROR=Submitting new version (user\\: {0}) failed. Reason\\: {1}\n" + 
			"RECOVER_EO_SCHEDULER_ERROR=System recovery\\: Could not load message to scheduler. Reason\\: {0}\n" + 
			"JPR_OUT_REQUEST=Java proxy runtime (JPR) is transferring the request message to the messaging System\n" + 
			"JWS_IN_SEND_MSG_TO_PROCESS=Sending inbound message to message processor\n" + 
			"MS_SEQUENCER_TRIGGER=Sequencer triggered by another cluster node; status will be updated to TBDL and message will be put in queue\n" + 
			"MS_EOIO_LOCK_ERROR=Could not get EOIO sequence ID lock in timeout ({0} mins). Predecessor message in {1} sequence did not release lock. Details and solution in SAP Note 881468\n" + 
			"XML_MSG_VAL_ERROR_SYNC=Forward validation of synchronous message not supported\n" + 
			"FILE_SUC_213=Start converting XML document content to plain text\n" + 
			"FILE_SUC_212=Connect to FTP server \"{0}\", directory \"{1}\"\n" + 
			"FILE_SUC_210=File processing finished\n" + 
			"JMS_SEND_SUCCESS=JMS message forwarded to the JMS provider\n" + 
			"CALL_TRANSMIT_SUCCESS=Message was successfully transmitted to endpoint {1} using connection {0}\n" + 
			"JPR_OUT_REQUEST_ERROR=JPR failed to transfer the request message to the messaging system. Reason\\: {0}\n" + 
			"PERFORMANCE_UPDATE_SIZE=CalledPerformanceUpdateSize\n" + 
			"RESTOUT_GENERATING_URL=Generating target URL\n" + 
			"MAPPING_SUCCESS_IF=Mapping {0} executed successfully. Interface changed to {1}/{2}\n" + 
			"JDBC_ERR_602=JDBC message processing failed; reason {0}\n" + 
			"MS_BLACKLIST_NDLV_MSG_STRT=Moved message to the non-delivered state. Reason\\: Blacklisted message (in delivering state) found during startup\n" + 
			"MS_BULK_MEM_EXCEED=Message will not be added to bulk because memory limit of {0} is exceeded. Message size\\: {1}\n" + 
			"AAE_UNKNOWN_MAPPING=Executing Unknown Type Mapping \"{0}/{1}\" (SWCV {2})\n" + 
			"JDBC_ERR_601=Could not connect to DB, channel\\:{0} (ID\\:{1}) Stopped or Inactive or Adapter shutdown\n" + 
			"JDBC_SUC_501=Receiver JDBC adapter\\: processing started; QoS required\\: {0}\n" + 
			"FILE_SUC_209=No message content found - nothing written to file\n" + 
			"FILE_SUC_208=Write to file \"{0}\" as text (encoding {1}) size {2} bytes\n" + 
			"JDBC_ERR_208=Establishing database connection failed with error {0}\n" + 
			"FILE_SUC_207=Write to file \"{0}\" as binary, size {1} bytes\n" + 
			"JDBC_ERR_207=Establishing database connection failed with SQL error {0}\n" + 
			"FILE_SUC_206=Transfer\\: \"{0}\" mode, size {1} bytes, encoding {2}\n" + 
			"JDBC_ERR_206=Database request processing failed due to {0}\n" + 
			"JMS_TEST_WARNING=Test mode activated. Test EO handling by throwing exception at\\: {0}\n" + 
			"MS_BLACKLIST_NDLV_MSG=Moved message to the non-delivered state. Reason\\: {0}\n" + 
			"FILE_SUC_205=Write to FTP server \"{0}\", directory \"{1}\", {2} file \"{3}\"\n" + 
			"FILE_SUC_204=File Adapter Receiver Channel {0}\\: Start processing; party \"{1}\" / service \"{2}\"\n" + 
			"FILE_SUC_203=File Adapter Receiver Channel {0}\\: Message already processed - skip\n" + 
			"JDBC_ERR_202=JDBC Adapter Receiver Channel {0}\\:  Not initialized - cannot proceed due to {1}\n" + 
			"JDBC_ERR_201=JDBC adapter receiver channel {0}\\: Configuration not activated. Unable to proceed\n" + 
			"AFW_MARK_FOR_RETRY=Message selected to be processed again when the receiver channel is started\n" + 
			"MS_BULK_NO_EXECUTOR=Message cannot be added to bulk because no executor is registered\n" + 
			"RESTIN_CONVERTED_TO_XML=Payload converted to XML\n" + 
			"JPR_IN_RESPONSE_OUT=Java proxy runtime (JPR) is transferring the response message to the messaging system\n" + 
			"MS_ASYNC_FAIL_NOTIFY_ERROR=Failed to trigger async error notification. Reason\\: {0}\n" + 
			"JWS_IN_APP_SPECIFIC_ID=Application-specific message ID is {0}\n" + 
			"SEND_ASYNC_ERROR_TRYING=Trying to deliver asynchronous error notification (message ID\\: {1}) to sending application\\: {0}\n" + 
			"REQUEST_DELIVER_SUCCESS=Message delivered to the application using connection {0}\n" + 
			"HTTP_PROCESS_SUCCESS=HTTP adapter successfully processed interface {0}\n" + 
			"MS_TERMINATE_SEQ=Received terminate sequence signal\n" + 
			"RFC_OUT_BAPI_ROLLBACK=Rollback transaction with {0}\n" + 
			"JWS_IN_DUPLICATE_IGNORED=Ignored duplicate message with application-specific ID {0}\n" + 
			"MS_RETRIEVAL_ERROR=Failed to retrieve message\\: {0}\n" + 
			"RECEIVE_DELIVER_SCHEDULE_ERROR=Attempt to schedule the asynchronous message to be delivered at {0} failed, due to\\: {1}\n" + 
			"AFW_DELIVER=Delivering to channel\\: {0}\n" + 
			"JMS_MSG_EMPTY_WARNING=JMS message {0}\\: Payload empty or cannot be read\n" + 
			"XI_ACK_SUCCESS=Acknowledgement sent successfully for type\\: {0}\n" + 
			"MPEB_ERROR=Message could not be forwarded to the JCA adapter. Reason\\: {0}\n" + 
			"RESTIN_PARSED_ATTRIBUTE=Parsed attribute \"{0}\"\\: \"{1}\"\n" + 
			"RESPONSE_ERROR=Failed to pass response message for message {0} to the waiting \"request\" thread; Already expired\n" + 
			"JMS_SEND_PENDING_WARNING=XI message with ID {0} will remain ''Pending''. Decide if you want to send the message again or mark as delivered (see ''Pending Handling'' channel parameter)\n" + 
			"RESPONSE_SUCCESS=Response message for message {0} passed to the waiting \"request\" thread\n" + 
			"SEND_TRANSMIT_ERROR=Transmitting the message to endpoint {2} using connection {0} failed, due to\\: {1}\n" + 
			"CM_Transformation_succeed=Transformation successful\n" + 
			"JWS_IN_PROCESS_SYNC_RESPONSE=Processing response to synchronous message\n" + 
			"MLS_SIG_VAL_ERROR=Attempt to validate message-level security failed. Reason\\: {0}\n" + 
			"CM_Transformation_error=Transformation error received. Verify that the Conversion Agent is installed correctly\n" + 
			"RESTIN_CUSTOM_HANDLING=Custom event handling\\: {0}. Rule\\: {1} {2}\n" + 
			"REQUEST_RETURN=Returning to receiving servlet\n" + 
			"XML_MSG_VAL_ERROR=Unable to validate message with message ID ''{0}''\n" + 
			"MPEB_PERMANENT_ERR=Message could not be forwarded permantely to the JCA adapter. Reason\\: {0}\n" + 
			"HTTP_PROCESS_ERROR=Error in processing caused by\\: {0}\n" + 
			"RFC_OUT_BAPI_SUCCESS=BAPI executed\n" + 
			"MS_BLACKLIST_NDLV_MSG_FAIL=Could not move blacklisted message to the non-delivered state. Reason\\: {0}\n" + 
			"QUEUE_PUT_EH_REG=EventHandler for profile {0} registered. Message was put back in the queue\n" + 
			"MPA_CHANNEL_FOUND=Retrieved channel\\: {0}\n" + 
			"MLS_SIG_VAL_SUCCESS=Message-level security validated successfully\n" + 
			"STARTUP_EO_SCHEDULER_SUCCESS=System Startup\\: Message successfully loaded into the scheduler. Next delivery scheduled for {0}\n" + 
			"JPR_IN_SYS_ACK_WARNING=JPR failed to trigger the system acknowledgment. Reason\\: {0}\n" + 
			"FILE_WRN_018=Channel {0}\\: File modification check not supported with File Content Conversion (CSV) message split - Ignoring setting\n" + 
			"FILE_WRN_017=Channel {0}\\: Empty document found - proceed without sending message\n" + 
			"JMS_MSGTYPE_UNKNOWN_WARNING=JMS message {0}\\: Message type {1} is unknown, payload cannot be read and will be empty\n" + 
			"REQUEST_SYNC_ERROR_RETURN=Returning synchronous error message to calling application\\: {0}\n" + 
			"ACTION_MESSAGE_FAIL_ERROR=Admin action\\: Failed to set message status to ''failed'' due to\\: {0}\n" + 
			"JMS_PENDING_WARNING=Configured to throw temporary error (default). To bypass this message, set 'Pending Handling' parameter to 'Bypass' and restart message processing\n" + 
			"JPR_IN_RESPONSE_OUT_SUCCESS=JPR transferred the response message to the messaging system successfully\n" + 
			"MS_BLACKLIST_REMOVED_MSG=Message removed from blacklist\n" + 
			"JPR_IN_APPL_ERROR_ACK_WARNING=JPR failed to trigger the application error acknowledgment. Reason\\: {0}\n" + 
			"RESTOUT_CALL_FINISHED=REST call finished\n" + 
			"JMS_SEND_CORRELATION=New JMS message will be correlated with XI message. Correlation rule\\: {0}. Correlated property\\: {1}. Correlation value\\: {2}.\n" + 
			"JPR_OUT_REQUEST_SUCCESS=JPR transferred the request message to the messaging system successfully\n" + 
			"JMS_CONVERT_BIN2XMB_SUCCESS=JMS message converted to XI message\n" + 
			"CM_TransformationName_null=Missing TransformationName value in the Module Configuration\n" + 
			"RESTOUT_CALL_SERVER=Calling server\\: {1}\n" + 
			"RESTIN_SENDING_MESSAGE=Sending message to message processor\n" + 
			"FILE_WRN_009=Channel {0}\\: File sender adapter processing finished\n" + 
			"FILE_WRN_008=Channel {0}\\: File sender adapter polling sequence was interrupted\n" + 
			"Correlation=Value is null and so cannot be set. Correlation rule\\: {0} . Correlated property\\: {1}\n" + 
			"FILE_WRN_007=No files found for file filter name \"{0}\" in directory \"{1}\" - start poll sequence {2} secs, {3} msecs\n" + 
			"JPR_IN_RESPONSE_CREATE_ERROR=Java proxy runtime (JPR) could not create the response message\n" + 
			"MS_BULK_FORMING=Message waiting for bulk completion\n" + 
			"FILE_WRN_001=File Adapter Sender Channel {0} not activated\n" + 
			"AAE_MSG_SPLIT_COMPLETE=Finished Splitting Message\n" + 
			"MLS_SIGNING_SUCCESS=Message-level security applied successfully\n" + 
			"SEND_QUEUE_PUT_ERROR=Putting message into send queue failed, due to\\: {0}\n" + 
			"RESTOUT_SETTING_HTTPVARIABLES=Setting HTTP variables\n" + 
			"RESTOUT_RECEIVED_SYNC_MESSAGE=Received synchronous message\n" + 
			"AAE_MSG_SPLIT_SUC=Message Split from Transient Message {0}\n" + 
			"JPR_IN_ACCEPTED=Java proxy runtime (JPR) accepted the message\n" + 
			"JMS_SEND_TOSERVICE_SUCCESS=XI message as binary forwarded to the SAP XI JMS service\n" + 
			"BTN_GET_RECAUDIT=Display\n" + 
			"CM_Transformation_failed=Transformation failed, CMException\\: {0}\n" + 
			"MS_SEQUENCER_TRIGGER_ERROR=Could not trigger sequencer for SequenceID ''{0}'' on this node; reason\\: {1}\n" + 
			"MLS_SIGNING_ERROR=Attempt to apply message-level security failed. Reason\\: {0}\n" + 
			"MS_SYNC_TIMEOUT_ERROR=Synchronous timeout exceeded\n" + 
			"JPR_OUT_USER=JPR received \"{0}\" as user\n" + 
			"RFC_OUT_BAPI_COMMIT=Commit transaction with {0}\n" + 
			"MS_EOIO_TRIGGER_ERROR=Sequencer failed to trigger message {0} for sequence {1}. The EOIO sequence may stop processing. Reason\\: {2}\n" + 
			"SEND_TRANSMIT_SCHEDULE_SUCCESS=The message was successfully scheduled to be resent at {0}\n" + 
			"JWS_IN_RECEIVE_MSG_SUCCESS=Successfully received inbound message\n" + 
			"RESTOUT_PROCESSING_RESULT=Processing result\n" + 
			"STRICT_XML2PLAIN_START=Starting strict XML to plain text conversion\n" + 
			"FILE_ERR_211=Attempt to process file failed with {0}\n" + 
			"RFC_OUT_BAPI_TABLE=BAPI response is a table of type {0} and has {1} rows\n" + 
			"JDBC_SUC_010=Channel {0}\\: Sending query result failed with {1}\n" + 
			"JPR_IN_SYS_ERROR_ACK_WARNING=JPR failed to trigger the system error acknowledgment. Reason\\: {0}\n" + 
			"ACTION_MESSAGE_REDELIVER_SUCCESS=Admin action\\: The message was successfully redelivered to the application\n" + 
			"RECEIVE_ASYNC_ERROR_TRYING=Asynchronous error detected\\: {0}. Trying to report it\n" + 
			"MPEB_SUCCESS=Message was successfully processed by the JCA adapter\n" + 
			"AAE_MSG_SPLITTING=Starting to Split Message\n" + 
			"JPR_IN_RESPONSE_SUCCESS=JPR successfully sent the response message\n" + 
			"RESTIN_PROCESSING_CALL=Processing HTTP call\\: {0} {1}\n" + 
			"JDBC_ERR_502=Configuration error\\: Channel \"{0}\" is not a receiver type\n" + 
			"JWS_OUT_DELIVERY_SEMANTICS=Message contains delivery semantics\\: {0}\n" + 
			"FILE_SUC_501=File adapter receiver\\: processing started; QoS required\\: {0}\n" + 
			"RFC_OUT_ASYNC_SUCCESS=RFC Adapter received an asynchronous message. Trying to send the tRFC for {0} with TID {1}\n" + 
			"AAE_REQUEST_MAPPING=Executing Request Mapping \"{0}/{1}\" (SWCV {2})\n" + 
			"JDBC_SUC_009=Send query result, size {0} characters\n" + 
			"REQUEST_RETRY=Retrying to deliver message to the application. Retry\\: {0}\n" + 
			"JDBC_SUC_005=Channel {0}\\: Updated {1} row(s)\n" + 
			"FILE_ERR_203=Message does not have a main payload\n" + 
			"JDBC_SUC_004=Channel {0}\\: Executed query successfully, start executing update\n" + 
			"FILE_ERR_202=File adapter receiver channel {0} is not initialized. Unable to proceed\\: {1}\n" + 
			"JDBC_SUC_003=Channel {0}\\: Executed query successfully, confirmation skipped - data may be sent again ({1} mode)\n" + 
			"FILE_ERR_201=File Adapter Receiver Channel {0}\\: Configuration not activated - cannot proceed\n" + 
			"JDBC_SUC_002=Channel {0}\\: Start executing query\n" + 
			"CALL_TRYING=Application attempting to send an {1} message synchronously using connection {0}\n" + 
			"QUEUE_GET_EH_ERROR=Cannot consume message from queue since EventHandler for profile {0} is not available. Message will be put back in queue as soon as EventHandler is registered\n" + 
			"XI_MMF_REQ_SUC=Processing child message of multi-message with message ID {0}\n" + 
			"REQUEST_QUEUE_PUT_ERROR=Putting message into request queue failed, due to\\: {0}\n" + 
			"AFW_MARK_FOR_RETRY_FAILED=Attempt to select message to be processed again when the receiver channel is started failed. Reason\\: {0}\n" + 
			"ACTION_MESSAGE_ACK_ERROR=Unable to Process Acknowledgment. Reason\\: {0}\n" + 
			"STARTUP_EOIO_SEQUENCER_SUCCESS=System Startup\\: Message successfully loaded into the sequencer\n" + 
			"AAE_FAULT_MAPPING=Executing Fault Mapping \"{0}/{1}\" (SWCV {2})\n" + 
			"SEND_TRANSMIT_ERROR_FAILED=Transmitting the message using connection {0} failed, due to\\: {1}. Setting message to status failed\n" + 
			"RESTOUT_PROCESSING_ERROR=Error during message processing\\: {0}\n" + 
			"PERFORMANCE_START=CalledPerformanceStart - Stack\\: {0}\n" + 
			"MS_BLACKLIST_ADD_MSG_FAIL=Could not add message to blacklist. Reason\\: {0}\n" + 
			"SEND_QUEUE_PUT_TRYING=Trying to put the message into the send queue\n" + 
			"STARTUP_EO_SCHEDULER_ERROR=System startup\\: Could not load message to scheduler. Reason\\: {0}\n" + 
			"CM_TransformationName_is=TransformationName is\\: {0}\n" + 
			"STARTUP_EO_QUEUE_SUCCESS=System Startup\\: Message successfully loaded into queue\\: {0}\n" + 
			"MPEB_TEMP_ERR=Message could not be forwarded temporarily to the JCA adapter. Reason\\: {0}\n" + 
			"MS_RECOVER_JOB_HOLD=EOIO message restart triggered by recover job\\: {0}. Predecessor message is in final state\n" + 
			"RFC_IN_ASYNC_SUCCESS=RFC Adapter received a tRFC for {0} with TID {1} from {2}/{3}. Trying to send the message asynchronously\n" + 
			"JMS_RECEIVE_NEWMESSAGE_SUCCESS=New JMS message with JMS message ID {0} received. The XI message ID for this message is {1}\n" + 
			"XML_FWD_VAL=Forward validation is enabled\n" + 
			"RESTIN_NO_INTERFACE_FOUND=No matching rule to map interface found\n" + 
			"FILE_SUC_046=Send main payload size {0} bytes, attachments approximate size {1} bytes with QoS {2}\n" + 
			"FILE_SUC_045=Channel {0}\\: Converted complete file content to XML format\n" + 
			"XI_TRIGGER_ACK=Acknowledgement creation triggered for type\\: {0}\n" + 
			"FILE_SUC_043=Connecting to FTP server \"{0}\"\n" + 
			"FILE_SUC_041=Execute OS command \"{0}\"\n" + 
			"FILE_SUC_040=File \"{0}\" successfully archived on FTP server \"{1}\" as \"{2}\"\n";
}