package com.nr.instrumentation.sap.auditlogging;

public class ErrorCodeConfig {

	private static ErrorCodeConfig INSTANCE = null;
	
	public static boolean isEnabled() {
		if(INSTANCE == null) {
			INSTANCE = new ErrorCodeConfig(true);
		}
		return INSTANCE.enabled();
	}
	
	public static void setErrorConfig(boolean enabled) {
		INSTANCE = new ErrorCodeConfig(enabled);
	}
	
	private boolean enabled = true;
	
	public ErrorCodeConfig(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean enabled() {
		return enabled;
	}
}
