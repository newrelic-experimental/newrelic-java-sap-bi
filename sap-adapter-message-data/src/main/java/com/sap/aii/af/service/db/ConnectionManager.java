package com.sap.aii.af.service.db;

import java.sql.Connection;
import java.sql.SQLException;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adapter.data.DictionaryUtils;

@Weave
public abstract class ConnectionManager {
	
	public static synchronized ConnectionManager getInstance() {
		if(!DictionaryUtils.initialized) {
			DictionaryUtils.initialize();
		}
		return Weaver.callOriginal();
	}

	protected ConnectionManager() {
		if(!DictionaryUtils.initialized) {
			DictionaryUtils.initialize();
		}
	}

	
	public abstract Connection getNoTXDBConnection() throws SQLException;
	
}
