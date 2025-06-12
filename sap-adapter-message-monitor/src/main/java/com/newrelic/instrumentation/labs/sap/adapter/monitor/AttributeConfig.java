package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.agent.deps.com.google.gson.Gson;
import com.newrelic.agent.deps.com.google.gson.JsonSyntaxException;
import com.newrelic.api.agent.NewRelic;

public class AttributeConfig extends TimerTask {
	
	private static final String CONFIGFILENAME = "adapter-message-config.json";

	private boolean ApplicationComponent = false;
	private boolean ConnectionName = true;
	private boolean CorrelationId = false;
	private boolean Direction = false;
	private boolean Duration = false;
	private boolean Endpoint = false;
	private boolean EndTime= true;
	private boolean ErrorCategory = false;
	private boolean ErrorCode = false;
	private boolean ErrorLabel = false;

	private boolean Interface_all = false;
	private boolean Interface_name = false;
	private boolean Interface_namespace = false;
	private boolean Interface_receiverComponent = false;
	private boolean Interface_receiverParty = false;
	private boolean Interface_senderComponent = false;
	private boolean Interface_senderParty = false;
	private boolean IsPersistent = false;
	private boolean MessageId = false;
	private boolean MessageKey = true;
	private boolean MessagePriority = false;
	private boolean MessageType = true;
	private boolean NodeId = false;
	private boolean ParentId = false;
	private boolean Passport = false;
	private boolean PersistUntil = false;
	private boolean Protocol = true;
	private boolean QualityOfService = false;
	private boolean ReceiverInterface_all = false;
	private boolean ReceiverInterface_name = false;
	private boolean ReceiverInterface_namespace = false;
	private boolean ReceiverInterface_receiverComponent = false;
	private boolean ReceiverInterface_receiverParty = false;
	private boolean ReceiverInterface_senderComponent = false;
	private boolean ReceiverInterface_senderParty = false;
	private boolean ReceiverName = false;
	private boolean ReceiverParty_all = false;
	private boolean ReceiverParty_name = false;
	private boolean ReceiverParty_agency = false;
	private boolean ReceiverParty_schema = false;
	private boolean ReferenceID = false;
	private boolean Retries = false;
	private boolean RetryInterval = false;
	private boolean RootID = false;
	private boolean ScenarioIdentifier = false;
	private boolean ScheduleTime = false;
	private boolean SenderInterface_all = false;
	private boolean SenderInterface_name = false;
	private boolean SenderInterface_namespace = false;
	private boolean SenderInterface_receiverComponent = false;
	private boolean SenderInterface_receiverParty = false;
	private boolean SenderInterface_senderComponent = false;
	private boolean SenderInterface_senderParty = false;
	private boolean SenderName = false;
	private boolean SenderParty_all = false;
	private boolean SenderParty_name = false;
	private boolean SenderParty_agency = false;
	private boolean SenderParty_schema = false;
	private boolean SequenceID = false;
	private boolean SequenceNumber = false;
	private boolean SerializationContext = false;
	private boolean ServiceDef = false;
	private boolean Size = false;
	private boolean SoftwareComponent = false;
	private boolean StartTime = false;
	private boolean Status = false;
	private boolean TimesFailed = false;
	private boolean Transport = false;
	private boolean ValidUntil = false;
	private boolean Version = false;
	private boolean BusinessMessage = false;
	private static AttributeConfig INSTANCE = null;
	private static File configFile;
	private static long lastMod = System.currentTimeMillis();
	
	public static AttributeConfig getInstance() {
		if(INSTANCE == null) {
			INSTANCE = constructInstance();
		}
		return INSTANCE;
	}
	
	static {
		AttributeConfig config = getInstance();
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(config, 60000L, 60000L);
	}
	
	private static void setConfigFile() {
		File nrDir = ConfigFileHelper.getNewRelicDirectory();
		configFile = new File(nrDir,CONFIGFILENAME);
		lastMod = configFile.lastModified();
	}
	
	private static AttributeConfig constructInstance() {
		if(configFile == null) {
			setConfigFile();
		}
		if(configFile.exists()) {
			try {
				FileInputStream fis = new FileInputStream(configFile);
				int size = fis.available();
				byte[] buffer = new byte[size];
				fis.read(buffer);
				String json = new String(buffer);
				Gson gson = new Gson();
				AttributeConfig config = gson.fromJson(json, AttributeConfig.class);
				NewRelic.getAgent().getLogger().log(Level.FINE, "Created AttributeConfig from config file, configuation is {0}", config);
				NewRelic.getAgent().getInsights().recordCustomEvent("AttributeConfig", config.configurationMap());
				fis.close();
				return config;
			} catch (JsonSyntaxException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER,e, "Unable to parse JSON from adapter-message-config.json");
			} catch (FileNotFoundException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER,e, "File adapter-message-config.json was not found");
			} catch (IOException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER,e, "IOException while trying to parse JSON in adapter-message-config.json");
			}
		}
		return new AttributeConfig();
	}
	
	public boolean collectApplicationComponent() {
		return ApplicationComponent;
	}
	public boolean collectConnectionName() {
		return ConnectionName;
	}
	public boolean collectCorrelationId() {
		return CorrelationId;
	}
	public boolean collectDirection() {
		return Direction;
	}
	public boolean collectDuration() {
		return Duration;
	}
	public boolean collectEndpoint() {
		return Endpoint;
	}
	public boolean collectEndTime() {
		return EndTime;
	}
	public boolean collectErrorCategory() {
		return ErrorCategory;
	}
	public boolean collectErrorCode() {
		return ErrorCode;
	}
	public boolean collectErrorLabel() {
		return ErrorLabel;
	}
	public boolean collectPersistent() {
		return IsPersistent;
	}
	public boolean collectMessageId() {
		return MessageId;
	}
	public boolean collectMessageKey() {
		return MessageKey;
	}
	public boolean collectMessagePriority() {
		return MessagePriority;
	}
	public boolean collectMessageType() {
		return MessageType;
	}
	public boolean collectNodeId() {
		return NodeId;
	}
	public boolean collectParentId() {
		return ParentId;
	}
	public boolean collectPassport() {
		return Passport;
	}
	public boolean collectPersistUntil() {
		return PersistUntil;
	}
	public boolean collectProtocol() {
		return Protocol;
	}
	public boolean collectQualityOfService() {
		return QualityOfService;
	}
	public boolean collectReceiverName() {
		return ReceiverName;
	}
	public boolean collectReceiverParty_all() {
		return ReceiverParty_all;
	}

	public boolean collectReceiverParty_name() {
		return ReceiverParty_name;
	}

	public boolean collectReceiverParty_agency() {
		return ReceiverParty_agency;
	}

	public boolean collectReceiverParty_schema() {
		return ReceiverParty_schema;
	}

	public boolean collectSenderParty_all() {
		return SenderParty_all;
	}

	public boolean collectSenderParty_name() {
		return SenderParty_name;
	}

	public boolean collectSenderParty_agency() {
		return SenderParty_agency;
	}

	public boolean collectSenderParty_schema() {
		return SenderParty_schema;
	}

	public boolean collectReferenceID() {
		return ReferenceID;
	}
	public boolean collectRetries() {
		return Retries;
	}
	public boolean collectRetryInterval() {
		return RetryInterval;
	}
	public boolean collectRootID() {
		return RootID;
	}
	public boolean collectScenarioIdentifier() {
		return ScenarioIdentifier;
	}
	public boolean collectScheduleTime() {
		return ScheduleTime;
	}
	public boolean collectSenderName() {
		return SenderName;
	}
	public boolean collectSenderPartyAll() {
		return SenderParty_all;
	}
	public boolean collectSequenceID() {
		return SequenceID;
	}
	public boolean collectSequenceNumber() {
		return SequenceNumber;
	}
	public boolean collectSerializationContext() {
		return SerializationContext;
	}
	public boolean collectServiceDef() {
		return ServiceDef;
	}
	public boolean collectSize() {
		return Size;
	}
	public boolean collectSoftwareComponent() {
		return SoftwareComponent;
	}
	public boolean collectStartTime() {
		return StartTime;
	}
	public boolean collectStatus() {
		return Status;
	}
	public boolean collectTimesFailed() {
		return TimesFailed;
	}
	public boolean collectTransport() {
		return Transport;
	}
	public boolean collectValidUntil() {
		return ValidUntil;
	}
	public boolean collectVersion() {
		return Version;
	}
	public boolean collectBusinessMessage() {
		return BusinessMessage;
	}
	
	public boolean collectInterface_all() {
		return Interface_all;
	}

	public boolean collectInterface_name() {
		return Interface_name;
	}

	public boolean collectInterface_namespace() {
		return Interface_namespace;
	}

	public boolean collectInterface_receiverComponent() {
		return Interface_receiverComponent;
	}

	public boolean collectInterface_receiverParty() {
		return Interface_receiverParty;
	}

	public boolean collectInterface_senderComponent() {
		return Interface_senderComponent;
	}

	public boolean collectInterface_senderParty() {
		return Interface_senderParty;
	}

	public boolean collectReceiverInterface_all() {
		return ReceiverInterface_all;
	}

	public boolean collectReceiverInterface_name() {
		return ReceiverInterface_name;
	}

	public boolean collectReceiverInterface_namespace() {
		return ReceiverInterface_namespace;
	}

	public boolean collectReceiverInterface_receiverComponent() {
		return ReceiverInterface_receiverComponent;
	}

	public boolean collectReceiverInterface_receiverParty() {
		return ReceiverInterface_receiverParty;
	}

	public boolean collectReceiverInterface_senderComponent() {
		return ReceiverInterface_senderComponent;
	}

	public boolean collectReceiverInterface_senderParty() {
		return ReceiverInterface_senderParty;
	}

	public boolean collectSenderInterface_all() {
		return SenderInterface_all;
	}

	public boolean collectSenderInterface_name() {
		return SenderInterface_name;
	}

	public boolean collectSenderInterface_namespace() {
		return SenderInterface_namespace;
	}

	public boolean collectSenderInterface_receiverComponent() {
		return SenderInterface_receiverComponent;
	}

	public boolean collectSenderInterface_receiverParty() {
		return SenderInterface_receiverParty;
	}

	public boolean collectSenderInterface_senderComponent() {
		return SenderInterface_senderComponent;
	}

	public boolean collectSenderInterface_senderParty() {
		return SenderInterface_senderParty;
	}

	@Override
	public void run() {
		if(configFile == null) {
			setConfigFile();
		}
		if(configFile != null) {
			long last = configFile.lastModified();
			if(last > lastMod) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Contents of {0} have been modified, reloading AttributeConfig", CONFIGFILENAME);
				INSTANCE = constructInstance();
				lastMod = last;
			}
		}
	}

	@Override
	public String toString() {
		return "AttributeConfig [ApplicationComponent=" + ApplicationComponent + ", ConnectionName=" + ConnectionName
				+ ", CorrelationId=" + CorrelationId + ", Direction=" + Direction + ", Duration=" + Duration
				+ ", Endpoint=" + Endpoint + ", EndTime=" + EndTime + ", ErrorCategory=" + ErrorCategory
				+ ", ErrorCode=" + ErrorCode + ", ErrorLabel=" + ErrorLabel + ", Interface_all=" + Interface_all
				+ ", Interface_name=" + Interface_name + ", Interface_namespace=" + Interface_namespace
				+ ", Interface_receiverComponent=" + Interface_receiverComponent + ", Interface_receiverParty="
				+ Interface_receiverParty + ", Interface_senderComponent=" + Interface_senderComponent
				+ ", Interface_senderParty=" + Interface_senderParty + ", IsPersistent=" + IsPersistent + ", MessageId="
				+ MessageId + ", MessageKey=" + MessageKey + ", MessagePriority=" + MessagePriority + ", MessageType="
				+ MessageType + ", NodeId=" + NodeId + ", ParentId=" + ParentId + ", Passport=" + Passport
				+ ", PersistUntil=" + PersistUntil + ", Protocol=" + Protocol + ", QualityOfService=" + QualityOfService
				+ ", ReceiverInterface_all=" + ReceiverInterface_all + ", ReceiverInterface_name="
				+ ReceiverInterface_name + ", ReceiverInterface_namespace=" + ReceiverInterface_namespace
				+ ", ReceiverInterface_receiverComponent=" + ReceiverInterface_receiverComponent
				+ ", ReceiverInterface_receiverParty=" + ReceiverInterface_receiverParty
				+ ", ReceiverInterface_senderComponent=" + ReceiverInterface_senderComponent
				+ ", ReceiverInterface_senderParty=" + ReceiverInterface_senderParty + ", ReceiverName=" + ReceiverName
				+ ", ReceiverParty_all=" + ReceiverParty_all + ", ReceiverParty_name=" + ReceiverParty_name
				+ ", ReceiverParty_agency=" + ReceiverParty_agency + ", ReceiverParty_schema=" + ReceiverParty_schema
				+ ", ReferenceID=" + ReferenceID + ", Retries=" + Retries + ", RetryInterval=" + RetryInterval
				+ ", RootID=" + RootID + ", ScenarioIdentifier=" + ScenarioIdentifier + ", ScheduleTime=" + ScheduleTime
				+ ", SenderInterface_all=" + SenderInterface_all + ", SenderInterface_name=" + SenderInterface_name
				+ ", SenderInterface_namespace=" + SenderInterface_namespace + ", SenderInterface_receiverComponent="
				+ SenderInterface_receiverComponent + ", SenderInterface_receiverParty=" + SenderInterface_receiverParty
				+ ", SenderInterface_senderComponent=" + SenderInterface_senderComponent
				+ ", SenderInterface_senderParty=" + SenderInterface_senderParty + ", SenderName=" + SenderName
				+ ", SenderParty_all=" + SenderParty_all + ", SenderParty_name=" + SenderParty_name
				+ ", SenderParty_agency=" + SenderParty_agency + ", SenderParty_schema=" + SenderParty_schema
				+ ", SequenceID=" + SequenceID + ", SequenceNumber=" + SequenceNumber + ", SerializationContext="
				+ SerializationContext + ", ServiceDef=" + ServiceDef + ", Size=" + Size + ", SoftwareComponent="
				+ SoftwareComponent + ", StartTime=" + StartTime + ", Status=" + Status + ", TimesFailed=" + TimesFailed
				+ ", Transport=" + Transport + ", ValidUntil=" + ValidUntil + ", Version=" + Version
				+ ", BusinessMessage=" + BusinessMessage + "]";
	}
	
	public Map<String,Boolean> configurationMap() {
		
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		
		map.put("ApplicationComponent",ApplicationComponent);
		map.put("ConnectionName",ConnectionName);
		map.put("CorrelationId",CorrelationId);
		map.put("Direction",Direction);
		map.put("Duration",Duration);
		map.put("Endpoint",Endpoint);
		map.put("EndTime",EndTime);
		map.put("ErrorCategory",ErrorCategory);
		
		map.put("ErrorCode",ErrorCode);
		map.put("ErrorLabel",ErrorLabel);
		map.put("Interface_all",Interface_all);
		map.put("Interface_name",Interface_name);
		map.put("Interface_namespace",Interface_namespace);
		map.put("Interface_receiverComponent",Interface_receiverComponent);
		map.put("Interface_receiverParty",Interface_receiverParty);
		map.put("Interface_senderComponent",Interface_senderComponent);
		map.put("Interface_senderParty",Interface_senderParty);
		map.put("IsPersistent",IsPersistent);
		map.put("MessageId",MessageId);
		map.put("MessageKey",MessageKey);
		map.put("MessagePriority",MessagePriority);
		map.put("MessageType",MessageType);
		map.put("NodeId",NodeId);
		map.put("ParentId",ParentId);
		map.put("Passport",Passport);
		map.put("PersistUntil",PersistUntil);
		map.put("Protocol",Protocol);
		map.put("QualityOfService",QualityOfService);
		map.put("ReceiverInterface_all",ReceiverInterface_all);
		map.put("ReceiverInterface_name",ReceiverInterface_name);
		map.put("ReceiverInterface_namespace",ReceiverInterface_namespace);
		map.put("ReceiverInterface_receiverComponent",ReceiverInterface_receiverComponent);
		map.put("ReceiverInterface_receiverParty",ReceiverInterface_receiverParty);
		map.put("ReceiverInterface_senderComponent",ReceiverInterface_senderComponent);
		map.put("ReceiverInterface_senderParty",ReceiverInterface_senderParty);
		map.put("ReceiverName",ReceiverName);
		map.put("ReceiverParty_all",ReceiverParty_all);
		map.put("ReceiverParty_name",ReceiverParty_name);
		map.put("ReceiverParty_agency",ReceiverParty_agency);
		map.put("ReceiverParty_schema",ReceiverParty_schema);
		map.put("ReferenceID",ReferenceID);
		map.put("Retries",Retries);
		map.put("RetryInterval",RetryInterval);
		map.put("RootID",RootID);
		map.put("ScenarioIdentifier",ScenarioIdentifier);
		map.put("ScheduleTime",ScheduleTime);
		map.put("SenderInterface_all",SenderInterface_all);
		map.put("SenderInterface_name",SenderInterface_name);
		map.put("SenderInterface_namespace",SenderInterface_namespace);
		map.put("SenderInterface_receiverComponent",SenderInterface_receiverComponent);
		map.put("SenderInterface_receiverParty",SenderInterface_receiverParty);
		map.put("SenderInterface_senderComponent",SenderInterface_senderComponent);
		map.put("SenderInterface_senderParty",SenderInterface_senderParty);
		map.put("SenderName",SenderName);
		map.put("SenderParty_all",SenderParty_all);
		map.put("SenderParty_name",SenderParty_name);
		map.put("SenderParty_agency",SenderParty_agency);
		map.put("SenderParty_schema",SenderParty_schema);
		map.put("SequenceID",SequenceID);
		map.put("SequenceNumber",SequenceNumber);
		map.put("SerializationContext",SerializationContext);
		map.put("ServiceDef",ServiceDef);
		map.put("Size",Size);
		map.put("SoftwareComponent",SoftwareComponent);
		map.put("StartTime",StartTime);
		map.put("Status",Status);
		map.put("TimesFailed",TimesFailed);
		map.put("Transport",Transport);
		map.put("ValidUntil",ValidUntil);
		map.put("Version",Version);
		map.put("BusinessMessage",BusinessMessage);
		
		return map;
		
	}
	

}
