/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package java.sql;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.newrelic.agent.Transaction;
import com.newrelic.agent.TransactionActivity;
import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.agent.bridge.datastore.DatastoreMetrics;
import com.newrelic.agent.bridge.datastore.JdbcHelper;
import com.newrelic.agent.tracers.ClassMethodSignature;
import com.newrelic.agent.tracers.DefaultSqlTracer;
import com.newrelic.agent.tracers.Tracer;
import com.newrelic.agent.tracers.TracerFlags;
import com.newrelic.agent.tracers.metricname.SimpleMetricNameFormat;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(originalName = "java.sql.PreparedStatement", type = MatchType.Interface)
public abstract class PreparedStatement_Weaved {

	@NewField
	private static List<String> skipped = new ArrayList<>();

	@NewField
	private Object[] params;

	@NewField
	String preparedSql;

	public ResultSet executeQuery() throws SQLException {
		Transaction transaction = null;
		DefaultSqlTracer tracer = null;
		boolean isMatch = checkClass();
		if(isMatch) {
			if (preparedSql == null) {
				preparedSql = JdbcHelper.getSql((Statement) this);
			}
			transaction = Transaction.getTransaction();
			if (transaction != null) {
				ClassMethodSignature sig = new ClassMethodSignature(getClass().getName(), "executeQuery",
						"()Ljava.sql.ResultSet;");
				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
				tracer = new DefaultSqlTracer(transaction, sig, this,
						new SimpleMetricNameFormat("JDBC/PreparedStatement/executeQuery"), tracerFlags);
				TransactionActivity txa = transaction.getTransactionActivity();
				Tracer parent = txa.getLastTracer();
				tracer.setParentTracer(parent);
				DatastoreMetrics.noticeSql(getConnection(), preparedSql, params);
				txa.tracerStarted(tracer);
			}

		} else {
			String classname = getClass().getName();
			if(!skipped.contains(classname)) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "In prepared statement {0}, skipping tracer creation", getClass().getName());
				skipped.add(classname);
			}
		}

		ResultSet rs = Weaver.callOriginal();

		if(isMatch) {
			if(transaction != null && tracer != null) {
				tracer.finish(176, (Object)null);
			}
		}
		return rs;
	}

	public int executeUpdate() throws SQLException {
		Transaction transaction = null;
		DefaultSqlTracer tracer = null;
		boolean isMatch = checkClass();
		if(isMatch) {
			if (preparedSql == null) {
				preparedSql = JdbcHelper.getSql((Statement) this);
			}
			transaction = Transaction.getTransaction();
			if (transaction != null) {
				ClassMethodSignature sig = new ClassMethodSignature(getClass().getName(), "executeUpdate", "()I");
				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
				tracer = new DefaultSqlTracer(transaction, sig, this,
						new SimpleMetricNameFormat("JDBC/PreparedStatement/executeUpdate"), tracerFlags);
				TransactionActivity txa = transaction.getTransactionActivity();
				Tracer parent = txa.getLastTracer();
				tracer.setParentTracer(parent);
				txa.tracerStarted(tracer);
				DatastoreMetrics.noticeSql(getConnection(), preparedSql, params);
			}

		} else {
			String classname = getClass().getName();
			if(!skipped.contains(classname)) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "In prepared statement {0}, skipping tracer creation", getClass().getName());
				skipped.add(classname);
			}
		}

		int rs = Weaver.callOriginal();

		if(isMatch) {
			if(transaction != null && tracer != null) {
				tracer.finish(176, (Object)null);
			}
		}
		return rs;
	}

	public boolean execute() throws SQLException {
		Transaction transaction = null;
		DefaultSqlTracer tracer = null;
		boolean isMatch = checkClass();
		if(isMatch) {
			if (preparedSql == null) {
				preparedSql = JdbcHelper.getSql((Statement) this);
			}
			transaction = Transaction.getTransaction();
			if (transaction != null) {
				ClassMethodSignature sig = new ClassMethodSignature(getClass().getName(), "execute", "()Z");
				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
				tracer = new DefaultSqlTracer(transaction, sig, this,
						new SimpleMetricNameFormat("JDBC/PreparedStatement/execute"), tracerFlags);
				TransactionActivity txa = transaction.getTransactionActivity();
				Tracer parent = txa.getLastTracer();
				tracer.setParentTracer(parent);
				txa.tracerStarted(tracer);
				DatastoreMetrics.noticeSql(getConnection(), preparedSql, params);
			}

		} else {
			String classname = getClass().getName();
			if(!skipped.contains(classname)) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "In prepared statement {0}, skipping tracer creation", getClass().getName());
				skipped.add(classname);
			}
		}

		boolean b = Weaver.callOriginal();

		if(isMatch) {
			if(transaction != null && tracer != null) {
				tracer.finish(176, (Object)null);
			}
		}
		return b;
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

	public abstract Connection getConnection() throws SQLException;

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

	
	private boolean checkClass() {
		if(this instanceof java.sql.PreparedStatement) {
			Class<?> clazz = getClass();
			Package clazzPackage = clazz.getPackage();
			if(clazzPackage != null) {
				String packageName = clazzPackage.getName();
				if(!packageName.startsWith("com.sap")) return true;
			}
		}
		return false;

	}

}