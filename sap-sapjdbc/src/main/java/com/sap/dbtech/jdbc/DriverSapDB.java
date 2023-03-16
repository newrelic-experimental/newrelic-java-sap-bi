package com.sap.dbtech.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.newrelic.agent.bridge.datastore.JdbcHelper;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.sapjdbc.SAPJDBCUtils;
import com.newrelic.instrumentation.sapjdbc.SAPJDBCVendor;

@Weave
public abstract class DriverSapDB {
	
	public static final String buildURL(String host, String dbname) {
		String returnValue = Weaver.callOriginal();
		JdbcHelper.putDatabaseName(returnValue, dbname);
		return returnValue;
	}

	@Trace(leaf = true, excludeFromTransactionTrace = true)
	public Connection connect(String url, Properties info) throws SQLException {
		JdbcHelper.putVendor(getClass(), SAPJDBCVendor.INSTANCE);
		Connection connection = Weaver.callOriginal();
		if(connection != null && !JdbcHelper.databaseNameExists(connection)) {
			String dbName = SAPJDBCUtils.getDatabaseNameFromURL(url);
			if(dbName != null && !dbName.isEmpty()) {
				JdbcHelper.putDatabaseName(url, dbName);
			}
		}
		
		return connection;
	}
}
