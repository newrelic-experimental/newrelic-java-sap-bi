package com.sap.sql.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.newrelic.agent.bridge.datastore.DatastoreMetrics;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public class CommonStatement {
	
	private Connection nativeConnection = Weaver.callOriginal();

	@Trace(leaf = true)
	public boolean execute(String sql) throws SQLException {
        DatastoreMetrics.noticeSql(nativeConnection, sql, null);
        return Weaver.callOriginal();		
	}
	
	@Trace(leaf = true)
	public ResultSet executeQuery(String sql) throws SQLException {
        DatastoreMetrics.noticeSql(nativeConnection, sql, null);
        return Weaver.callOriginal();		
	}
	
	@Trace(leaf = true)
	public int executeUpdate(String sql) throws SQLException {
        DatastoreMetrics.noticeSql(nativeConnection, sql, null);
        return Weaver.callOriginal();		
	}
	
	
}
