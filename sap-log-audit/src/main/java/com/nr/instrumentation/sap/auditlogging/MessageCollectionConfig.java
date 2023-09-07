package com.nr.instrumentation.sap.auditlogging;

import java.util.HashMap;
import java.util.Map;

import com.newrelic.api.agent.NewRelic;

public class MessageCollectionConfig {

	private static MessageCollectionConfig INSTANCE = null;
	private static HashMap<String, Object> currentSettings = new HashMap<>();
	
	public static MessageCollectionConfig getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new MessageCollectionConfig();
		}
		return INSTANCE;
	}
	
	// Enabled by default
	private boolean messagedirection = true;
	private boolean messageid = true;
	private boolean messagekey_all = true;
	private boolean messagekey_messageid = true;
	private boolean protocol = true;
	private boolean timereceived = true;

	// not enabled by default
	private boolean action_all = false;
	private boolean action_name = false;
	private boolean action_type = false;
	private boolean correlationId = false;
	private boolean deliverySemantics = false;
	private boolean description = false;
	private boolean xmlpayload_all = false;
	private boolean xmlpayload_payload_name = false;
	private boolean xmlpayload_payload_desc = false;
	private boolean xmlpayload_payload_contenttype = false;
	private boolean textpayload_encoding = false;
	private boolean xmlpayload_version = false;
	private boolean xmlpayload_schema = false;
	private boolean mainpayload_all = false;
	private boolean mainpayload_name = false;
	private boolean mainpayload_desc = false;
	private boolean mainpayload_contenttype = false;
	private boolean fromparty_all = false;
	private boolean fromparty_name = false;
	private boolean fromparty_type = false;
	private boolean fromservice_all = false;
	private boolean fromservice_name = false;
	private boolean fromservice_type = false;
	private boolean messageclass = false;
	private boolean messagekey_direction = false;
	private boolean refToMessageId = false;
	private boolean sequenceid = false;
	private boolean toparty_all = false;
	private boolean toparty_name = false;
	private boolean toparty_type = false;
	private boolean toservice_all = false;
	private boolean toservice_name = false;
	private boolean toservice_type = false;
	private boolean serializationcontext = false;
	private boolean timesent = false;
	
	public boolean isActionAll() {
		return action_all;
	}

	public boolean isAction_name() {
		return action_name;
	}

	public boolean isAction_type() {
		return action_type;
	}

	public boolean isCorrelationId() {
		return correlationId;
	}

	public boolean isDeliverySemantics() {
		return deliverySemantics;
	}

	public boolean isDescription() {
		return description;
	}

	public boolean isXmlpayload_all() {
		return xmlpayload_all;
	}

	public boolean isXmlpayload_payload_name() {
		return xmlpayload_payload_name;
	}

	public boolean isXmlpayload_payload_desc() {
		return xmlpayload_payload_desc;
	}

	public boolean isXmlpayload_payload_contenttype() {
		return xmlpayload_payload_contenttype;
	}

	public boolean isTextpayload_encoding() {
		return textpayload_encoding;
	}

	public boolean isXmlpayload_version() {
		return xmlpayload_version;
	}

	public boolean isXmlpayload_schema() {
		return xmlpayload_schema;
	}

	public boolean isMainpayload_all() {
		return mainpayload_all;
	}

	public boolean isMainpayload_name() {
		return mainpayload_name;
	}

	public boolean isMainpayload_desc() {
		return mainpayload_desc;
	}

	public boolean isMainpayload_contenttype() {
		return mainpayload_contenttype;
	}

	public boolean isFromparty_all() {
		return fromparty_all;
	}

	public boolean isFromparty_name() {
		return fromparty_name;
	}

	public boolean isFromparty_type() {
		return fromparty_type;
	}

	public boolean isFromservice_all() {
		return fromservice_all;
	}

	public boolean isFromservice_name() {
		return fromservice_name;
	}

	public boolean isFromservice_type() {
		return fromservice_type;
	}

	public boolean isMessageclass() {
		return messageclass;
	}

	public boolean isMessagedirection() {
		return messagedirection;
	}

	public boolean isMessageid() {
		return messageid;
	}

	public boolean isMessagekey_all() {
		return messagekey_all;
	}

	public boolean isMessagekey_messageid() {
		return messagekey_messageid;
	}

	public boolean isMessagekey_direction() {
		return messagekey_direction;
	}

	public boolean isProtocol() {
		return protocol;
	}

	public boolean isRefToMessageId() {
		return refToMessageId;
	}

	public boolean isSequenceid() {
		return sequenceid;
	}

	public boolean isToparty_all() {
		return toparty_all;
	}

	public boolean isToparty_name() {
		return toparty_name;
	}

	public boolean isToparty_type() {
		return toparty_type;
	}

	public boolean isToservice_all() {
		return toservice_all;
	}

	public boolean isToservice_name() {
		return toservice_name;
	}

	public boolean isToservice_type() {
		return toservice_type;
	}

	public boolean isSerializationcontext() {
		return serializationcontext;
	}

	public boolean isTimereceived() {
		return timereceived;
	}

	public boolean isTimesent() {
		return timesent;
	}

	public Builder newBuilder() {
		return new Builder();
	}
	
	public static final class Builder {
		private boolean action_all = false;
		private boolean action_name = false;
		private boolean action_type = false;
		private boolean correlationId = false;
		private boolean deliverySemantics = false;
		private boolean description = false;
		private boolean xmlpayload_all = false;
		private boolean xmlpayload_payload_name = false;
		private boolean xmlpayload_payload_desc = false;
		private boolean xmlpayload_payload_contenttype = false;
		private boolean textpayload_encoding = false;
		private boolean xmlpayload_version = false;
		private boolean xmlpayload_schema = false;
		private boolean mainpayload_all = false;
		private boolean mainpayload_name = false;
		private boolean mainpayload_desc = false;
		private boolean mainpayload_contenttype = false;
		private boolean fromparty_all = false;
		private boolean fromparty_name = false;
		private boolean fromparty_type = false;
		private boolean fromservice_all = false;
		private boolean fromservice_name = false;
		private boolean fromservice_type = false;
		private boolean messageclass = false;
		private boolean messagedirection = true;
		private boolean messageid = true;
		private boolean messagekey_all = true;
		private boolean messagekey_messageid = true;
		private boolean messagekey_direction = false;
		private boolean protocol = true;
		private boolean refToMessageId = false;
		private boolean sequenceid = false;
		private boolean toparty_all = false;
		private boolean toparty_name = false;
		private boolean toparty_type = false;
		private boolean toservice_all = false;
		private boolean toservice_name = false;
		private boolean toservice_type = false;
		private boolean serializationcontext = false;
		private boolean timereceived = true;
		private boolean timesent = false;
		
		public void action_all(boolean action) {
			this.action_all = action;
		}
		public void action_name(boolean action_name) {
			this.action_name = action_name;
		}
		public void action_type(boolean action_type) {
			this.action_type = action_type;
		}
		public void correlationId(boolean correlationId) {
			this.correlationId = correlationId;
		}
		public void deliverySemantics(boolean deliverySemantics) {
			this.deliverySemantics = deliverySemantics;
		}
		public void description(boolean description) {
			this.description = description;
		}
		public void xmlpayload_all(boolean xmlpayload) {
			this.xmlpayload_all = xmlpayload;
		}
		public void xmlpayload_payload_name(boolean xmlpayload_payload_name) {
			this.xmlpayload_payload_name = xmlpayload_payload_name;
		}
		public void xmlpayload_payload_desc(boolean xmlpayload_payload_desc) {
			this.xmlpayload_payload_desc = xmlpayload_payload_desc;
		}
		public void xmlpayload_payload_contenttype(boolean xmlpayload_payload_contenttype) {
			this.xmlpayload_payload_contenttype = xmlpayload_payload_contenttype;
		}
		public void textpayload_encoding(boolean textpayload_encoding) {
			this.textpayload_encoding = textpayload_encoding;
		}
		public void xmlpayload_version(boolean xmlpayload_version) {
			this.xmlpayload_version = xmlpayload_version;
		}
		public void xmlpayload_schema(boolean xmlpayload_schema) {
			this.xmlpayload_schema = xmlpayload_schema;
		}
		public void mainpayload_all(boolean mainpayload) {
			this.mainpayload_all = mainpayload;
		}
		public void mainpayload_name(boolean mainpayload_name) {
			this.mainpayload_name = mainpayload_name;
		}
		public void mainpayload_desc(boolean mainpayload_desc) {
			this.mainpayload_desc = mainpayload_desc;
		}
		public void mainpayload_contenttype(boolean mainpayload_contenttype) {
			this.mainpayload_contenttype = mainpayload_contenttype;
		}
		public void fromparty_all(boolean fromparty) {
			this.fromparty_all = fromparty;
		}
		public void fromparty_name(boolean fromparty_name) {
			this.fromparty_name = fromparty_name;
		}
		public void fromparty_type(boolean fromparty_type) {
			this.fromparty_type = fromparty_type;
		}
		public void fromservice_all(boolean fromservice) {
			this.fromservice_all = fromservice;
		}
		public void fromservice_name(boolean fromservice_name) {
			this.fromservice_name = fromservice_name;
		}
		public void fromservice_type(boolean fromservice_type) {
			this.fromservice_type = fromservice_type;
		}
		public void messageclass(boolean messageclass) {
			this.messageclass = messageclass;
		}
		public void messagedirection(boolean messagedirection) {
			this.messagedirection = messagedirection;
		}
		public void messageid(boolean messageid) {
			this.messageid = messageid;
		}
		public void messagekey_all(boolean messagekey) {
			this.messagekey_all = messagekey;
		}
		public void messagekey_messageid(boolean messagekey_messageid) {
			this.messagekey_messageid = messagekey_messageid;
		}
		public void messagekey_direction(boolean messagekey_direction) {
			this.messagekey_direction = messagekey_direction;
		}
		public void protocol(boolean protocol) {
			this.protocol = protocol;
		}
		public void refToMessageId(boolean refToMessageId) {
			this.refToMessageId = refToMessageId;
		}
		public void sequenceid(boolean sequenceid) {
			this.sequenceid = sequenceid;
		}
		public void toparty_all(boolean toparty) {
			this.toparty_all = toparty;
		}
		public void toparty_name(boolean toparty_name) {
			this.toparty_name = toparty_name;
		}
		public void toparty_type(boolean toparty_type) {
			this.toparty_type = toparty_type;
		}
		public void toservice_all(boolean toservice) {
			this.toservice_all = toservice;
		}
		public void toservice_name(boolean toservice_name) {
			this.toservice_name = toservice_name;
		}
		public void toservice_type(boolean toservice_type) {
			this.toservice_type = toservice_type;
		}
		public void serializationcontext(boolean serializationcontext) {
			this.serializationcontext = serializationcontext;
		}
		public void timereceived(boolean timereceived) {
			this.timereceived = timereceived;
		}
		public void timesent(boolean timesent) {
			this.timesent = timesent;
		}
		
		public void build() {
			MessageCollectionConfig config = new MessageCollectionConfig();
			
			config.action_all = action_all;
			config.action_name = action_name;
			config.action_type = action_type;
			config.correlationId = correlationId;
			config.deliverySemantics = deliverySemantics;
			config.description = description;
			config.fromparty_all = fromparty_all;
			config.fromparty_name = fromparty_name;
			config.fromparty_type = fromparty_type;
			config.fromservice_all = fromservice_all;
			config.fromservice_name = fromservice_name;
			config.fromservice_type = fromservice_type;
			config.mainpayload_all = mainpayload_all;
			config.mainpayload_contenttype = mainpayload_contenttype;
			config.mainpayload_desc = mainpayload_desc;
			config.mainpayload_name = mainpayload_name;
			config.messageclass = messageclass;
			config.messagedirection = messagedirection;
			config.messageid = messageid;
			config.messagekey_all = messagekey_all;
			config.messagekey_direction = messagekey_direction;
			config.messagekey_messageid = messagekey_messageid;
			config.protocol = protocol;
			config.refToMessageId = refToMessageId;
			config.sequenceid = sequenceid;
			config.serializationcontext = serializationcontext;
			config.textpayload_encoding = textpayload_encoding;
			config.timereceived	= timereceived;
			config.timesent = timesent;
			config.toparty_all = toparty_all;
			config.toparty_name = toparty_name;
			config.toparty_type = toparty_type;
			config.toservice_all = toservice_all;
			config.toservice_name = toservice_name;
			config.toservice_type = toservice_type;
			config.xmlpayload_all = xmlpayload_all;
			config.xmlpayload_payload_contenttype = xmlpayload_payload_contenttype;
			config.xmlpayload_payload_desc = xmlpayload_payload_desc;
			config.xmlpayload_payload_name = xmlpayload_payload_name;
			config.xmlpayload_schema = xmlpayload_schema;
			config.xmlpayload_version = xmlpayload_version;
			
			INSTANCE = config;
			setCurrent();
			NewRelic.getAgent().getInsights().recordCustomEvent("MessageFieldsConfig", currentSettings);
		}
	}
	
	protected static Map<String, Object> getCurrent() {
		if(currentSettings.isEmpty()) {
			setCurrent();
			NewRelic.getAgent().getInsights().recordCustomEvent("MessageFieldsConfig", currentSettings);
		}
		
		return currentSettings;
	}
	
	private static void setCurrent() {
		currentSettings.put("ConfigurationType", "MessageFields");
		currentSettings.put("messagedirection",INSTANCE.messagedirection);
		currentSettings.put("messageid",INSTANCE.messageid);
		currentSettings.put("messagekey_all",INSTANCE.messagekey_all);
		currentSettings.put("messagekey_messageid",INSTANCE.messagekey_messageid);
		currentSettings.put("protocol",INSTANCE.protocol);
		currentSettings.put("timereceived",INSTANCE.timereceived);
		currentSettings.put("action_all",INSTANCE.action_all);
		currentSettings.put("action_name",INSTANCE.action_name);
		currentSettings.put("action_type",INSTANCE.action_type);
		currentSettings.put("correlationId",INSTANCE.correlationId);
		currentSettings.put("deliverySemantics",INSTANCE.deliverySemantics);
		currentSettings.put("description",INSTANCE.description);
		currentSettings.put("xmlpayload_all",INSTANCE.xmlpayload_all);
		currentSettings.put("xmlpayload_payload_name",INSTANCE.xmlpayload_payload_name);
		currentSettings.put("xmlpayload_payload_desc",INSTANCE.xmlpayload_payload_desc);
		currentSettings.put("xmlpayload_payload_contenttype",INSTANCE.xmlpayload_payload_contenttype);
		currentSettings.put("textpayload_encoding",INSTANCE.textpayload_encoding);
		currentSettings.put("xmlpayload_version",INSTANCE.xmlpayload_version);
		currentSettings.put("xmlpayload_schema",INSTANCE.xmlpayload_schema);
		currentSettings.put("mainpayload_all",INSTANCE.mainpayload_all);
		currentSettings.put("mainpayload_name",INSTANCE.mainpayload_name);
		currentSettings.put("mainpayload_desc",INSTANCE.mainpayload_desc);
		currentSettings.put("mainpayload_contenttype",INSTANCE.mainpayload_contenttype);
		currentSettings.put("fromparty_all",INSTANCE.fromparty_all);
		currentSettings.put("fromparty_name",INSTANCE.fromparty_name);
		currentSettings.put("fromparty_type",INSTANCE.fromparty_type);
		currentSettings.put("fromservice_all",INSTANCE.fromservice_all);
		currentSettings.put("fromservice_name",INSTANCE.fromservice_name);
		currentSettings.put("fromservice_type",INSTANCE.fromservice_type);
		currentSettings.put("messageclass",INSTANCE.messageclass);
		currentSettings.put("messagekey_direction",INSTANCE.messagekey_direction);
		currentSettings.put("refToMessageId",INSTANCE.refToMessageId);
		currentSettings.put("sequenceid",INSTANCE.sequenceid);
		currentSettings.put("toparty_all",INSTANCE.toparty_all);
		currentSettings.put("toparty_name",INSTANCE.toparty_name);
		currentSettings.put("toparty_type",INSTANCE.toparty_type);
		currentSettings.put("toservice_all",INSTANCE.toservice_all);
		currentSettings.put("toservice_name",INSTANCE.toservice_name);
		currentSettings.put("toservice_type",INSTANCE.toservice_type);
		currentSettings.put("serializationcontext",INSTANCE.serializationcontext);
		currentSettings.put("timesent",INSTANCE.timesent);
		
	}
	
}
