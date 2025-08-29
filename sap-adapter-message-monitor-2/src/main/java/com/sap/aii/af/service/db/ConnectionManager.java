package com.sap.aii.af.service.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.adapter.monitor.AdapterMonitorLogger;

@Weave
public abstract class ConnectionManager {
	
	public static synchronized ConnectionManager getInstance() {
		return Weaver.callOriginal();
	}
	
	public static void setConnectData(String host, String dbName, String username, String password) {
		AdapterMonitorLogger.logMessage("Setting connectdata, host: " + host + ", dbName: " + dbName + ", username: " + username != null ? "xxxxxx" : null + ", password: " + password != null ? "xxxxxx" : null);
		Weaver.callOriginal();
	}

	private DataSource dataSource = Weaver.callOriginal();
	private DataSource notxDataSource = Weaver.callOriginal();

	public void initDataSource(String dataSourceName) {
		Weaver.callOriginal();
		AdapterMonitorLogger.logMessage("Initialized NoTx Datasource with name " + dataSourceName + " to " + notxDataSource);
		AdapterMonitorLogger.logMessage("Initialized Tx Datasource with name " + dataSourceName + " to " + dataSource);
	}
	
	public abstract Connection getNoTXDBConnection() throws SQLException;
	
}
