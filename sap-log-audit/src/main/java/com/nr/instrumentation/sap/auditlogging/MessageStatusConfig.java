package com.nr.instrumentation.sap.auditlogging;

public class MessageStatusConfig {

	private static MessageStatusConfig INSTANCE = null;
	
	public static boolean isEnabled() {
		if(INSTANCE == null) {
			INSTANCE = new MessageStatusConfig(true);
		}
		return INSTANCE.enabled();
	}
	
	public static void setMessageStatusConfig(boolean enable) {
		INSTANCE = new MessageStatusConfig(enable);
	}
	
	private boolean enabled = true;
	
	private MessageStatusConfig(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean enabled() {
		return enabled;
	}
	
	
}
