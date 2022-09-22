package com.sap.sql.common;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.logging.Level;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.agent.bridge.datastore.DatastoreMetrics;
import com.newrelic.agent.bridge.datastore.JdbcHelper;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public abstract class CommonPreparedStatement {

	@NewField
	private Object[] params;


	@Trace(leaf = true)
	public boolean execute() throws SQLException {
		DatastoreMetrics.noticeSql(null, getSql(), params);
		return Weaver.callOriginal();
	}

	@Trace(leaf = true)
	public ResultSet executeQuery() throws SQLException {
		DatastoreMetrics.noticeSql(null, getSql(), params);
		return Weaver.callOriginal();
	}

	@Trace(leaf = true)
	public int executeUpdate() throws SQLException {
		DatastoreMetrics.noticeSql(null, getSql(), params);
		return Weaver.callOriginal();
	}

	public void setBigDecimal(int index, BigDecimal x) throws SQLException {
        setParamValue(index, x);
		Weaver.callOriginal();
	}

	public void setBoolean(int index, boolean x) throws SQLException {
        setParamValue(index, x);
		Weaver.callOriginal();
	}

	public void setByte(int index, byte x) throws SQLException {
        setParamValue(index, x);
		Weaver.callOriginal();
	}

	public void setDate(int index, Date x) throws SQLException {
        setParamValue(index, x);
		Weaver.callOriginal();
	}

	public void setDouble(int index, double x) throws SQLException {
        setParamValue(index, x);
		Weaver.callOriginal();
	}

	public void setFloat(int index, float x) throws SQLException {
        setParamValue(index, x);
		Weaver.callOriginal();
	}

	public void setInt(int index, int x) throws SQLException {
        setParamValue(index, x);
		Weaver.callOriginal();
	}

	public void setLong(int index, long x) throws SQLException {
        setParamValue(index, x);
		Weaver.callOriginal();
	}

	public void setNull(int index, int type) throws SQLException {
        setParamValue(index, "null");
		Weaver.callOriginal();
	}

	public void setShort(int index, short x) throws SQLException {
        setParamValue(index, x);
		Weaver.callOriginal();
	}

	public void setString(int index, String x) throws SQLException {
        setParamValue(index, x);
		Weaver.callOriginal();
	}

	public void setTime(int index, Time x) throws SQLException {
        setParamValue(index, x);
		Weaver.callOriginal();
	}

	public void setTimestamp(int index, Timestamp x) throws SQLException {
        setParamValue(index, x);
		Weaver.callOriginal();
	}

	public abstract String getSql();
	
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
