package com.newrelic.instrumentation.sapjdbc;

public class SAPJDBCUtils {

	public static String getDatabaseNameFromURL(String url) {
		String[] s = url.split("/");
		if(s != null && s.length > 2) {
			return s[3];
		}
		return null;
	}
	
}
