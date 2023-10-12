package com.newrelic.instrumentation.labs.sap.auditlogging;

public class QueueMessageCollectionConfig {

	private static QueueMessageCollectionConfig INSTANCE = null;
	
	public static QueueMessageCollectionConfig getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new QueueMessageCollectionConfig();
		}
		return INSTANCE;
	}
	
	private boolean transportmessage = false;
	private boolean connectionName = true;
	private boolean retries = true;
	private boolean adminResend = false;
	private boolean persisent = false;
	private boolean messageStatus = true;
	private boolean messageType = true;
	private boolean nodeid = false;
	private boolean scheduletime = false;
	private boolean timesfailed = true;
	private boolean transdelivertime = true;
	private boolean errorcategory = false;
	private boolean errorcode = true;
	private boolean messagesize = true;
	private boolean messagekey_all = true;
	private boolean messagekey_messageid = true;
	private boolean messagekey_direction = true;

	public boolean isTransportMessage() {
		return transportmessage;
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

	public boolean isRetries() {
		return retries;
	}
	
	public boolean isConnectionName() {
		return connectionName;
	}

	public boolean isAdminResend() {
		return adminResend;
	}

	public boolean isPersisent() {
		return persisent;
	}

	public boolean isMessageStatus() {
		return messageStatus;
	}

	public boolean isMessageType() {
		return messageType;
	}

	public boolean isNodeid() {
		return nodeid;
	}

	public boolean isScheduletime() {
		return scheduletime;
	}

	public boolean isTimesfailed() {
		return timesfailed;
	}

	public boolean isTransdelivertime() {
		return transdelivertime;
	}

	public boolean isErrorcategory() {
		return errorcategory;
	}

	public boolean isErrorcode() {
		return errorcode;
	}

	public boolean isMessagesize() {
		return messagesize;
	}

	public Builder newBuilder() {
		return new Builder();
	}
	
	public static final class Builder {
		
		private boolean transportmessage = false;
		private boolean connectionName = true;
		private boolean retries = true;
		private boolean adminResend = false;
		private boolean persisent = false;
		private boolean messageStatus = true;
		private boolean messageType = true;
		private boolean nodeid = false;
		private boolean scheduletime = false;
		private boolean timesfailed = true;
		private boolean transdelivertime = true;
		private boolean errorcategory = false;
		private boolean errorcode = true;
		private boolean messagesize = true;
		private boolean messagekey_all = true;
		private boolean messagekey_messageid = true;
		private boolean messagekey_direction = true;
		
		public void transportmessage(boolean transportmessage) {
			this.transportmessage = transportmessage;
		}
		
		public void connectionName(boolean connectionName) {
			this.connectionName = connectionName;
		}
		
		public void retries(boolean retries) {
			this.retries = retries;
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

		public void adminResend(boolean adminResend) {
			this.adminResend = adminResend;
		}

		public void persisent(boolean persisent) {
			this.persisent = persisent;
		}

		public void messageStatus(boolean messageStatus) {
			this.messageStatus = messageStatus;
		}

		public void messageType(boolean messageType) {
			this.messageType = messageType;
		}

		public void nodeid(boolean nodeid) {
			this.nodeid = nodeid;
		}

		public void scheduletime(boolean scheduletime) {
			this.scheduletime = scheduletime;
		}

		public void timesfailed(boolean timesfailed) {
			this.timesfailed = timesfailed;
		}

		public void transdelivertime(boolean transdelivertime) {
			this.transdelivertime = transdelivertime;
		}

		public void errorcategory(boolean errorcategory) {
			this.errorcategory = errorcategory;
		}

		public void errorcode(boolean errorcode) {
			this.errorcode = errorcode;
		}

		public void messagesize(boolean messagesize) {
			this.messagesize = messagesize;
		}

		public void build() {
			QueueMessageCollectionConfig config = new QueueMessageCollectionConfig();
			
			config.transportmessage = transportmessage;
			config.adminResend = adminResend;
			config.connectionName = connectionName;
			config.errorcategory = errorcategory;
			config.errorcode = errorcode;
			config.messagesize = messagesize;
			config.messageStatus = messageStatus;
			config.messageType = messageType;
			config.nodeid = nodeid;
			config.persisent = persisent;
			config.retries = retries;
			config.scheduletime = scheduletime;
			config.timesfailed = timesfailed;
			config.transdelivertime = transdelivertime;
			config.messagekey_all = messagekey_all;
			config.messagekey_direction = messagekey_direction;
			config.messagekey_messageid = messagekey_messageid;
			
			INSTANCE = config;
		}
	}
	
}
