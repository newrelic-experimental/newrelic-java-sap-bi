package com.sap.db.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.newrelic.agent.bridge.datastore.DatastoreMetrics;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.BaseClass)
public abstract class StatementSapDB {

	@Trace(leaf = true)
	public boolean execute(String sql) throws SQLException {
        DatastoreMetrics.noticeSql(getConnection(), sql, null);
        return Weaver.callOriginal();
	}
	
	@Trace(leaf = true)
	public ResultSet executeQuery(String sql) throws SQLException {
        DatastoreMetrics.noticeSql(getConnection(), sql, null);
        return Weaver.callOriginal();
	}
	
	@Trace(leaf = true)
	public int executeUpdate(String sql) throws SQLException {
        DatastoreMetrics.noticeSql(getConnection(), sql, null);
        return Weaver.callOriginal();
	}
	
	public abstract Connection getConnection() throws SQLException;
}
