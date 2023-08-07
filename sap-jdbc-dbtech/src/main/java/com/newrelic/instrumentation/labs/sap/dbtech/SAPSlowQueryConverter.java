package com.newrelic.instrumentation.labs.sap.dbtech;

import com.newrelic.agent.database.SqlObfuscator;
import com.newrelic.api.agent.QueryConverter;

public class SAPSlowQueryConverter implements QueryConverter<String> {
	
	SqlObfuscator obfuscator = null;
	
	public SAPSlowQueryConverter() {
		obfuscator = SqlObfuscator.getDefaultSqlObfuscator();
	}
	

	@Override
	public String toRawQueryString(String rawQuery) {
		return rawQuery;
	}

	@Override
	public String toObfuscatedQueryString(String rawQuery) {
		
		return obfuscator.obfuscateSql(rawQuery);
	}

}
