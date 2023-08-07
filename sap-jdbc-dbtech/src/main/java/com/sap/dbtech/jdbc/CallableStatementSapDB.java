package com.sap.dbtech.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.logging.Level;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.agent.bridge.datastore.JdbcHelper;
import com.newrelic.agent.database.ParsedDatabaseStatement;
import com.newrelic.api.agent.DatastoreParameters;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.dbtech.Utils;
import com.newrelic.instrumentation.labs.sap.dbtech.Utils.DBInfo;

@Weave(type = MatchType.BaseClass)
public abstract class CallableStatementSapDB  {

	CallableStatementSapDB(ConnectionSapDB connection, String sql, int resultSetType, int resultSetConcurrency,int resultSetHoldability) throws SQLException  {
		preparedsql = sql;
	}
	
	public abstract Connection getConnection();
	
	@NewField
	protected String preparedsql = null;
	
    @NewField
    private Object[] params;
	
	@Trace(leaf = true)
	public boolean execute(int afterParseAgain) {
		ParsedDatabaseStatement pStmt = Utils.parseSQL(preparedsql);
		DBInfo info = Utils.getDBInfo((ConnectionSapDB) getConnection());
		
		DatastoreParameters params = Utils.getDBParameters(pStmt, info, preparedsql);
		if(params != null) {
			NewRelic.getAgent().getTracedMethod().reportAsExternal(params);
		}
		
		return Weaver.callOriginal();
	}
	
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setParamValue(parameterIndex, "null");
        Weaver.callOriginal();
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setParamValue(parameterIndex, x);
        Weaver.callOriginal();
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        setParamValue(parameterIndex, x);
        Weaver.callOriginal();
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        setParamValue(parameterIndex, x);
        Weaver.callOriginal();
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        setParamValue(parameterIndex, x);
        Weaver.callOriginal();
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        setParamValue(parameterIndex, x);
        Weaver.callOriginal();
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        setParamValue(parameterIndex, x);
        Weaver.callOriginal();
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        setParamValue(parameterIndex, x);
        Weaver.callOriginal();
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        setParamValue(parameterIndex, x);
        Weaver.callOriginal();
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        setParamValue(parameterIndex, x);
        Weaver.callOriginal();
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        setParamValue(parameterIndex, x);
        Weaver.callOriginal();
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        setParamValue(parameterIndex, x);
        Weaver.callOriginal();
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        setParamValue(parameterIndex, x);
        Weaver.callOriginal();
    }

    public void clearParameters() throws SQLException {
        params = new Object[0];

        Weaver.callOriginal();
    }
	
    private void setParamValue(int index, Object value) {
        if (params == null) {
            params = new Object[1];
        }

        index--;
        if (index < 0) {
            AgentBridge.getAgent().getLogger().log(Level.FINER,
                    "Unable to store a prepared statement parameter because the index < 0");
            return;
        } else if (index >= params.length) {
            params = JdbcHelper.growParameterArray(params, index);
        }
        params[index] = value;
    }

}
