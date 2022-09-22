package com.sap.sql.jdbc.direct;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.newrelic.agent.bridge.datastore.DatastoreMetrics;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.sap.sql.trace.TraceContext;

@Weave(type = MatchType.BaseClass)
public abstract class DirectStatement {

	
	protected DirectConnection directConn = Weaver.callOriginal();
	protected String sql = Weaver.callOriginal();
	
    @Trace(leaf = true)
    public ResultSet executeQuery(String sql) throws SQLException {
        DatastoreMetrics.noticeSql(directConn, sql, null);
        return Weaver.callOriginal();
    }

    @Trace(leaf = true)
    public int executeUpdate(String sql) throws SQLException {
        DatastoreMetrics.noticeSql(directConn, sql, null);
        return Weaver.callOriginal();
    }

    @Trace(leaf = true)
    public boolean execute(String sql) throws SQLException {
        DatastoreMetrics.noticeSql(directConn, sql, null);
        return Weaver.callOriginal();
    }

    @Trace(leaf = true)
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        DatastoreMetrics.noticeSql(directConn, sql, null);
        return Weaver.callOriginal();
    }

    @Trace(leaf = true)
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        DatastoreMetrics.noticeSql(directConn, sql, null);
        return Weaver.callOriginal();
    }

    @Trace(leaf = true)
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        DatastoreMetrics.noticeSql(directConn, sql, null);
        return Weaver.callOriginal();
    }

    @Trace(leaf = true)
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        DatastoreMetrics.noticeSql(directConn, sql, null);
        return Weaver.callOriginal();
    }

    @Trace(leaf = true)
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        DatastoreMetrics.noticeSql(directConn, sql, null);
        return Weaver.callOriginal();
    }

    @Trace(leaf = true)
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        DatastoreMetrics.noticeSql(directConn, sql, null);
        return Weaver.callOriginal();
    }
    
    @Trace(leaf = true)
    public boolean execute(String sql, TraceContext traceContext) throws SQLException {
        DatastoreMetrics.noticeSql(directConn, sql, null);
        return Weaver.callOriginal();
	}

    @Trace(leaf = true)
	public ResultSet executeQuery(String sql, TraceContext traceContext) throws SQLException {
        DatastoreMetrics.noticeSql(directConn, sql, null);
        return Weaver.callOriginal();
	}

    @Trace(leaf = true)
	public int executeUpdate(String sql, TraceContext traceContext) throws SQLException {
        DatastoreMetrics.noticeSql(directConn, sql, null);
        return Weaver.callOriginal();
	}

}