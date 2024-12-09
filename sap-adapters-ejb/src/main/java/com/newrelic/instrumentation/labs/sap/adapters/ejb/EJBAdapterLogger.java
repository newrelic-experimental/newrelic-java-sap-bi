package com.newrelic.instrumentation.labs.sap.adapters.ejb;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;

public class EJBAdapterLogger {

	public static boolean initialized = false;
	private static PrintWriter LOGGER;
	private static PrintWriter LOGGER2;
	private static PrintWriter LOGGER3;
	private static AdapterLoggingConfig config;
	private static final Set<String> principalAttributesList;
	private static final Set<String> loggedAttributes = new HashSet<String>();
	private static final Set<String> loggedSupplemental = new HashSet<String>();
	private static final Set<String> loggedPrincipal = new HashSet<String>();

	protected static final String CONTEXT_LOGFILENAME = "context-data-attributes-ejb.log";
	protected static final String DATA_LOGFILENAME = "moduledata-supplemental-attributes-ejb.log";
	protected static final String DATA_PRINCIPAL_LOGFILENAME = "moduledata-principal-attributes-ejb.log";
	
	static {
		principalAttributesList = new HashSet<String>();
		principalAttributesList.add("Message-Action");
		principalAttributesList.add("Message-CorrelationId");
		principalAttributesList.add("Message-FromParty");
		principalAttributesList.add("Message-FromService");
		principalAttributesList.add("Message-Id");
		principalAttributesList.add("Message-Protocol");
		principalAttributesList.add("Message-SequenceId");
		principalAttributesList.add("Message-ToParty");
		principalAttributesList.add("Message-ToService");
		principalAttributesList.add("Message-AttachmentCount");
		principalAttributesList.add("Message-AttachmentNames");
		principalAttributesList.add("Message-ErrorInfo");
		principalAttributesList.add("Message-Description");
		principalAttributesList.add("Message-RefToMessageId");
		principalAttributesList.add("Message-TimeReceived");
		principalAttributesList.add("Message-TimeSent");

		principalAttributesList.add("TransportableMessage-MessagePriority");
		principalAttributesList.add("TransportableMessage-Retries");
		principalAttributesList.add("TransportableMessage-SequenceNumber");
		principalAttributesList.add("TransportableMessage-PersistUntil");
		principalAttributesList.add("TransportableMessage-ValidUntil");
		principalAttributesList.add("TransportableMessage-VersionNumber");
		principalAttributesList.add("TransportableMessage-ParentId");

		principalAttributesList.add("MessageKey-Direction");
		principalAttributesList.add("MessageKey-ID");
		
		principalAttributesList.add("Endpoint-Address");
		principalAttributesList.add("Endpoint-Transport");

		principalAttributesList.add("Payload-Name");
		principalAttributesList.add("Payload-Description");
		principalAttributesList.add("Payload-ContentType");
		principalAttributesList.add("Payload-AttributeNames");

	}

	public static void initialize() {
		
			if(initialized) return;
			config = new AdapterLoggingConfig();
			String logFileName = config.getContextLog();
			try {
				FileOutputStream fos = new FileOutputStream(logFileName, false);
				LOGGER = new PrintWriter(fos);
			} catch (FileNotFoundException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to find file {0} for logging", logFileName);
			}
			
			logFileName = config.getSupplementalLog();
			try {
				FileOutputStream fos2 = new FileOutputStream(logFileName, false);
				LOGGER2 = new PrintWriter(fos2);
			} catch (FileNotFoundException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to find file {0} for logging", logFileName);
			}
			
			logFileName = config.getPrincipaladapterLog();
			try {
				FileOutputStream fos3 = new FileOutputStream(logFileName, false);
				LOGGER3 = new PrintWriter(fos3);
			} catch (FileNotFoundException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to find file {0} for logging", logFileName);
			}
			initialized = true;
	}
	
	public static void logNewAttribute(String attribute) {
		if(!initialized) {
			initialize();
		}
		if(!loggedAttributes.contains(attribute)) {
			loggedAttributes.add(attribute);
			NewRelic.getAgent().getLogger().log(Level.FINE, "Adding attribute {0} to loggedAttributes, size is now {1}", attribute, loggedAttributes.size());
			LOGGER.println(attribute);
			LOGGER.flush();
		}
	}
	
	public static void logNewSupplementalAttribute(String attribute) {
		if(!initialized) {
			initialize();
		}
		if(!loggedSupplemental.contains(attribute)) {
			loggedSupplemental.add(attribute);
			
			NewRelic.getAgent().getLogger().log(Level.FINE, "Adding attribute {0} to loggedSupplemental, size is now {1}", attribute, loggedSupplemental.size());
			LOGGER2.println(attribute);
			LOGGER2.flush();
		}
	}

	public static void logNewPrincipalAttribute(String type, String attribute) {
		if(!initialized) {
			initialize();
		}
		String attrName = type+": " +attribute;
		
		if(!loggedPrincipal.contains(attrName)) {
			loggedPrincipal.add(attrName);
			NewRelic.getAgent().getLogger().log(Level.FINE, "Adding attrName {0} to loggedPrincipal, size is now {1}", attrName, loggedPrincipal.size());
			LOGGER3.println(type+": " +attribute);
			LOGGER3.flush();
		}
	}
}
