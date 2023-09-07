package com.nr.instrumentation.sap.auditlogging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.agent.deps.org.json.simple.JSONObject;
import com.newrelic.agent.deps.org.json.simple.parser.JSONParser;
import com.newrelic.agent.deps.org.json.simple.parser.ParseException;
import com.newrelic.api.agent.NewRelic;

public class CollectionConfig implements Runnable {
	
	
	private static final File collectionConfig;
	private static final String configFileName = "saploggingconfig.json";
	private static long lastModified;
	
	static {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		collectionConfig = new File(newRelicDir, configFileName);
		if(collectionConfig.exists()) {
			lastModified = collectionConfig.lastModified();
		} else {
			lastModified = 0L;
		}
		NewRelicExecutors.submit(new CollectionConfig(), 1, 1, TimeUnit.MINUTES);
		
	}
	
	private CollectionConfig() {
		
	}
	
	public static void initialize() {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Initializing Message Logging Collection Config");
		if(collectionConfig.exists()) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Found configuration file for Message Logging Collection Config: {0}",collectionConfig.getName());
			try {
				JSONParser parser = new JSONParser();
				FileReader fileReader = new FileReader(collectionConfig);
				Object parsed = parser.parse(fileReader);
				if(parsed != null) {
					if(parsed instanceof JSONObject) {
						JSONObject outerObject = (JSONObject)parsed;
						
						Object statusConfObject = outerObject.get("MessageStatus-Config");
						if(statusConfObject != null) {
							JSONObject statusObj = (JSONObject)statusConfObject;
							Boolean value = getValue(statusObj, "status");
							if(value != null) {
								MessageStatusConfig.setMessageStatusConfig(value);
							}
						
						}
						
						Object errorcodeObj = outerObject.get("ErrorCode-Config");
						if(errorcodeObj != null) {
							JSONObject errorCodeObj = (JSONObject)errorcodeObj;
							Boolean value = getValue(errorCodeObj, "errorcode");
							if(value != null) {
								ErrorCodeConfig.setErrorConfig(value);
							}
						
						}
						
						
						Object msgConfObject = outerObject.get( "Message-Config");
						if(msgConfObject instanceof JSONObject) {
							JSONObject msgObj = (JSONObject)msgConfObject;
							MessageCollectionConfig.Builder builder = MessageCollectionConfig.getInstance().newBuilder();
							Boolean value = getValue(msgObj, "action");
							if(value != null) {
								builder.action_all(value);
							}
							value = getValue(msgObj, "action_name");
							if(value != null) {
								builder.action_name(value);
							}
							value = getValue(msgObj, "action_type");
							if(value != null) {
								builder.action_type(value);
							}
							value = getValue(msgObj, "correlationId");
							if(value != null) {
								builder.correlationId(value);
							}
							value = getValue(msgObj, "description");
							if(value != null) {
								builder.description(value);
							}
							value = getValue(msgObj,"deliverySemantics");
							if(value != null) {
								builder.deliverySemantics(value);
							}
							value = getValue(msgObj, "xmlpayload");
							if(value != null) {
								builder.xmlpayload_all(value);
							}
							value = getValue(msgObj, "xmlpayload_payload_name");
							if(value != null) {
								builder.xmlpayload_payload_name(value);
							}
							value = getValue(msgObj, "xmlpayload_payload_desc");
							if(value != null) {
								builder.xmlpayload_payload_desc(value);
							}
							value = getValue(msgObj, "xmlpayload_payload_contenttype");
							if(value != null) {
								builder.xmlpayload_payload_contenttype(value);
							}
							value = getValue(msgObj, "textpayload_encoding");
							if(value != null) {
								builder.textpayload_encoding(value);
							}
							value = getValue(msgObj, "xmlpayload_version");
							if(value != null) {
								builder.xmlpayload_version(value);
							}
							value = getValue(msgObj, "xmlpayload_schema");
							if(value != null) {
								builder.xmlpayload_schema(value);
							}
							value = getValue(msgObj, "mainpayload");
							if(value != null) {
								builder.mainpayload_all(value);
							}
							value = getValue(msgObj, "mainpayload_name");
							if(value != null) {
								builder.mainpayload_name(value);
							}
							value = getValue(msgObj, "mainpayload_desc");
							if(value != null) {
								builder.mainpayload_desc(value);
							}
							value = getValue(msgObj, "mainpayload_contenttype");
							if(value != null) {
								builder.mainpayload_contenttype(value);
							}
							value = getValue(msgObj, "fromparty");
							if(value != null) {
								builder.fromparty_all(value);
							}
							value = getValue(msgObj, "fromparty_name");
							if(value != null) {
								builder.fromparty_name(value);
							}
							value = getValue(msgObj, "fromparty_type");
							if(value != null) {
								builder.fromparty_type(value);
							}
							value = getValue(msgObj, "fromservice");
							if(value != null) {
								builder.fromservice_all(value);
							}
							value = getValue(msgObj, "fromservice_name");
							if(value != null) {
								builder.fromservice_name(value);
							}
							value = getValue(msgObj, "fromservice_type");
							if(value != null) {
								builder.fromservice_type(value);
							}
							value = getValue(msgObj, "messageclass");
							if(value != null) {
								builder.messageclass(value);
							}
							value = getValue(msgObj, "messagedirection");
							if(value != null) {
								builder.messagedirection(value);
							}
							value = getValue(msgObj, "messageid");
							if(value != null) {
								builder.messageid(value);
							}
							value = getValue(msgObj, "messagekey");
							if(value != null) {
								builder.messagekey_all(value);
							}
							value = getValue(msgObj, "messagekey_messageid");
							if(value != null) {
								builder.messagekey_messageid(value);
							}
							value = getValue(msgObj, "messagekey_direction");
							if(value != null) {
								builder.messagekey_direction(value);
							}
							value = getValue(msgObj, "protocol");
							if(value != null) {
								builder.protocol(value);
							}
							value = getValue(msgObj, "refToMessageId");
							if(value != null) {
								builder.refToMessageId(value);
							}
							value = getValue(msgObj, "sequenceid");
							if(value != null) {
								builder.sequenceid(value);
							}
							value = getValue(msgObj, "toparty");
							if(value != null) {
								builder.toparty_all(value);
							}
							value = getValue(msgObj, "toparty_name");
							if(value != null) {
								builder.toparty_name(value);
							}
							value = getValue(msgObj, "toparty_type");
							if(value != null) {
								builder.toparty_type(value);
							}
							value = getValue(msgObj, "toservice");
							if(value != null) {
								builder.toservice_all(value);
							}
							value = getValue(msgObj, "toservice_name");
							if(value != null) {
								builder.toservice_name(value);
							}
							value = getValue(msgObj, "toservice_type");
							if(value != null) {
								builder.toservice_type(value);
							}
							value = getValue(msgObj, "serializationcontext");
							if(value != null) {
								builder.serializationcontext(value);
							}
							value = getValue(msgObj, "timereceived");
							if(value != null) {
								builder.timereceived(value);
							}
							value = getValue(msgObj, "timesent");
							if(value != null) {
								builder.timesent(value);
							}
							builder.build();
						}
						Object transportConfObject = outerObject.get( "TranportableMessage-Config");
						if(transportConfObject != null) {
							JSONObject transportObj = (JSONObject)transportConfObject;
							TransportMessageCollectionConfig.Builder builder = TransportMessageCollectionConfig.getInstance().newBuilder();
							
							Boolean value = getValue(transportObj, "endpoint_all");
							if(value != null) {
								builder.endpoint_all(value);
							}
							
							value = getValue(transportObj, "endpoint_address");
							if(value != null) {
								builder.endpoint_address(value);
							}
							
							value = getValue(transportObj, "endpoint_transport");
							if(value != null) {
								builder.endpoint_transport(value);
							}
							
							value = getValue(transportObj, "messagepriority");
							if(value != null) {
								builder.messagepriority(value);
							}
							
							value = getValue(transportObj, "parentid");
							if(value != null) {
								builder.parentid(value);
							}
							
							value = getValue(transportObj, "retries");
							if(value != null) {
								builder.retries(value);
							}
							
							value = getValue(transportObj, "retryinterval");
							if(value != null) {
								builder.retryinterval(value);
							}
							
							value = getValue(transportObj, "sequencenumber");
							if(value != null) {
								builder.sequencenumber(value);
							}
							
							value = getValue(transportObj, "sequencenumber");
							if(value != null) {
								builder.sequencenumber(value);
							}
							
							value = getValue(transportObj, "persistuntil");
							if(value != null) {
								builder.persistuntil(value);
							}
							
							value = getValue(transportObj, "validuntil");
							if(value != null) {
								builder.validuntil(value);
							}
							
							value = getValue(transportObj, "versionnumber");
							if(value != null) {
								builder.versionnumber(value);
							}
							
							builder.build();
						}
						
						Object queueConfigObject = outerObject.get( "QueueMessage-Config");
						if(queueConfigObject != null) {
							JSONObject queueObj = (JSONObject)queueConfigObject;
							QueueMessageCollectionConfig.Builder builder = QueueMessageCollectionConfig.getInstance().newBuilder();
							
							Boolean value = getValue(queueObj, "connectionName");
							if(value != null) {
								builder.connectionName(value);
							}
							value = getValue(queueObj, "adminResend");
							if(value != null) {
								builder.adminResend(value);
							}
							value = getValue(queueObj, "persisent");
							if(value != null) {
								builder.persisent(value);
							}
							value = getValue(queueObj, "messageStatus");
							if(value != null) {
								builder.messageStatus(value);
							}
							value = getValue(queueObj, "messageType");
							if(value != null) {
								builder.messageType(value);
							}
							value = getValue(queueObj, "nodeid");
							if(value != null) {
								builder.nodeid(value);
							}
							value = getValue(queueObj, "scheduletime");
							if(value != null) {
								builder.scheduletime(value);
							}
							value = getValue(queueObj, "timesfailed");
							if(value != null) {
								builder.timesfailed(value);
							}
							value = getValue(queueObj, "transdelivertime");
							if(value != null) {
								builder.transdelivertime(value);
							}
							value = getValue(queueObj, "errorcategory");
							if(value != null) {
								builder.errorcategory(value);
							}
							value = getValue(queueObj, "errorcode");
							if(value != null) {
								builder.errorcode(value);
							}
							value = getValue(queueObj, "messagesize");
							if(value != null) {
								builder.messagesize(value);
							}
							value = getValue(queueObj, "messagekey");
							if(value != null) {
								builder.messagekey_all(value);
							}
							
							builder.build();
						}
					}
				}
			} catch (FileNotFoundException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Could not load SAP Message logging configuration due to FileNotFoundException");
			} catch (IOException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Could not load SAP Message logging configuration due to IOException");
			} catch (ParseException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Could not load SAP Message logging configuration due to ParseException");
			}
			
		} else {
			// Use default values
			NewRelic.getAgent().getLogger().log(Level.FINE, "Did not find SAP Message logging configuration file ({0}) in the New Relic Agent directiory, using default values", configFileName );
			MessageCollectionConfig.Builder builder = MessageCollectionConfig.getInstance().newBuilder();
			builder.build();
			QueueMessageCollectionConfig.Builder builder2 = QueueMessageCollectionConfig.getInstance().newBuilder();
			builder2.build();
		}
		
	}

	private static Boolean getValue(JSONObject json, String name) {
		Object obj = json.get(name);
		if(obj == null) return null;
		
		if(obj instanceof Boolean) {
			return ((Boolean)obj);
		}
		
		if(obj instanceof String) {
			String tmp = (String)obj;
			Boolean b = Boolean.parseBoolean(tmp);
			return b;
		}
		
		return null;
	}

	@Override
	public void run() {
		
		if(collectionConfig.exists()) {
			long l = collectionConfig.lastModified();
			// check if configuration has changed
			if(l > lastModified) {
				// configuration was updated so reinitialize
				initialize();
				lastModified = l;
			}
		}
	}
}
