package com.nr.instrumentation.sap.auditlogging;

import java.util.HashMap;
import java.util.Map;

import com.newrelic.api.agent.NewRelic;

public class TransportMessageCollectionConfig {

	private static TransportMessageCollectionConfig INSTANCE = null;
	private static HashMap<String, Object> currentSettings = new HashMap<>();
	
	public static TransportMessageCollectionConfig getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new TransportMessageCollectionConfig();
		}
		return INSTANCE;
	}
	
	private boolean endpoint_all = true;
	private boolean endpoint_address = true;
	private boolean endpoint_transport = true;
	private boolean messagepriority = true;
	private boolean parentid = true;
	private boolean retries = true;
	private boolean retryinterval = true;
	private boolean sequencenumber = true;

	private boolean persistuntil = false;
	private boolean transportbodysize = false;
	private boolean validuntil = false;
	private boolean versionnumber = false;
	
	public boolean isRetryInterval() {
		return retryinterval;
	}

	public boolean isSequenceNumber() {
		return sequencenumber;
	}

	public boolean isPersistUntil() {
		return persistuntil;
	}

	public boolean isTransportBodySize() {
		return transportbodysize;
	}

	public boolean isValidUntil() {
		return validuntil;
	}

	public boolean isVersionNumber() {
		return versionnumber;
	}

	public boolean isEndpoint_All() {
		return endpoint_all;
	}

	public boolean isEndpoint_address() {
		return endpoint_address;
	}

	public boolean isEndpoint_transport() {
		return endpoint_transport;
	}

	public boolean isMessagePriority() {
		return messagepriority;
	}

	public boolean isParentId() {
		return parentid;
	}

	public boolean isRetries() {
		return retries;
	}

	public Builder newBuilder() {
		return new Builder();
	}
	
	public static final class Builder {
		private boolean endpoint_all = true;
		private boolean endpoint_address = true;
		private boolean endpoint_transport = true;
		private boolean messagepriority = true;
		private boolean parentid = true;
		private boolean retries = true;
		private boolean retryinterval = true;
		private boolean sequencenumber = true;

		private boolean persistuntil = false;
		private boolean transportbodysize = false;
		private boolean validuntil = false;
		private boolean versionnumber = false;
		
		public void endpoint_all(boolean endpoint_all) {
			this.endpoint_all = endpoint_all;
		}

		public void endpoint_address(boolean endpoint_address) {
			this.endpoint_address = endpoint_address;
		}

		public void endpoint_transport(boolean endpoint_transport) {
			this.endpoint_transport = endpoint_transport;
		}

		public void messagepriority(boolean messagepriority) {
			this.messagepriority = messagepriority;
		}

		public void parentid(boolean parentid) {
			this.parentid = parentid;
		}

		public void retries(boolean retries) {
			this.retries = retries;
		}

		public void retryinterval(boolean retryinterval) {
			this.retryinterval = retryinterval;
		}

		public void sequencenumber(boolean sequencenumber) {
			this.sequencenumber = sequencenumber;
		}

		public void persistuntil(boolean persistuntil) {
			this.persistuntil = persistuntil;
		}

		public void transportbodysize(boolean transportbodysize) {
			this.transportbodysize = transportbodysize;
		}

		public void validuntil(boolean validuntil) {
			this.validuntil = validuntil;
		}

		public void versionnumber(boolean versionnumber) {
			this.versionnumber = versionnumber;
		}

		public void build() {
			TransportMessageCollectionConfig config = new TransportMessageCollectionConfig();
			
			config.persistuntil = persistuntil;
			config.transportbodysize = transportbodysize;
			config.validuntil = validuntil;
			config.versionnumber = versionnumber;
			config.endpoint_all = endpoint_all;
			config.endpoint_address = endpoint_address;
			config.endpoint_transport = endpoint_transport;
			config.messagepriority = messagepriority;
			config.parentid = parentid;
			config.retries	= retries;
			config.sequencenumber = sequencenumber;
			config.retryinterval = retryinterval;
			
			INSTANCE = config;
			setCurrent();
			NewRelic.getAgent().getInsights().recordCustomEvent("TransportMessageFieldsConfig", currentSettings);
		}
	}
	
	protected static Map<String, Object> getCurrent() {
		if(currentSettings.isEmpty()) {
			setCurrent();
			NewRelic.getAgent().getInsights().recordCustomEvent("TransportMessageFieldsConfig", currentSettings);
		}
		
		return currentSettings;
	}
	
	private static void setCurrent() {
		currentSettings.put("ConfigurationType", "TranportMessageFields");
		currentSettings.put("endpoint_all",INSTANCE.endpoint_all);
		currentSettings.put("endpoint_address",INSTANCE.endpoint_address);
		currentSettings.put("endpoint_transport",INSTANCE.endpoint_transport);
		currentSettings.put("messagepriority",INSTANCE.messagepriority);
		currentSettings.put("parentid",INSTANCE.parentid);
		currentSettings.put("retries",INSTANCE.retries);
		currentSettings.put("retryinterval",INSTANCE.retryinterval);
		currentSettings.put("sequencenumber",INSTANCE.sequencenumber);
		currentSettings.put("persistuntil",INSTANCE.persistuntil);
		currentSettings.put("transportbodysize",INSTANCE.transportbodysize);
		currentSettings.put("validuntil",INSTANCE.validuntil);
		currentSettings.put("versionnumber",INSTANCE.versionnumber);
		
	}
	
}
