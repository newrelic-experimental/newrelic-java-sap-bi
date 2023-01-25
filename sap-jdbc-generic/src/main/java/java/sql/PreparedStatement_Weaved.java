/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package java.sql;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.logging.Level;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.agent.bridge.datastore.DatastoreMetrics;
import com.newrelic.agent.bridge.datastore.JdbcHelper;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(originalName = "java.sql.PreparedStatement", type = MatchType.Interface)
public abstract class PreparedStatement_Weaved {

    @NewField
    private Object[] params;

    @NewField
    String preparedSql;

    @Trace(leaf = true)
    public ResultSet executeQuery() throws SQLException {
        if (preparedSql == null) {
            preparedSql = JdbcHelper.getSql((Statement) this);
        }
        String classname = getClass().getName();
        if(!classname.startsWith("com.sap.sql") && !classname.startsWith("com.sap.engine.services.dbpool")) {
        	DatastoreMetrics.noticeSql(getConnection(), preparedSql, params);
        } else {
        	Connection conn = getConnectionFromPrepared();
        	DatastoreMetrics.noticeSql(conn, preparedSql, params);
        }
        return Weaver.callOriginal();
    }

    @Trace(leaf = true)
    public int executeUpdate() throws SQLException {
        if (preparedSql == null) {
            preparedSql = JdbcHelper.getSql((Statement) this);
        }
        String classname = getClass().getName();
        if(!classname.startsWith("com.sap.sql") && !classname.startsWith("com.sap.engine.services.dbpool")) {
        	DatastoreMetrics.noticeSql(getConnection(), preparedSql, params);
        } else {
        	Connection conn = getConnectionFromPrepared();
        	DatastoreMetrics.noticeSql(conn, preparedSql, params);
        }
        return Weaver.callOriginal();
    }

    @Trace(leaf = true)
    public boolean execute() throws SQLException {
        if (preparedSql == null) {
            preparedSql = JdbcHelper.getSql((Statement) this);
        }
        String classname = getClass().getName();
        if(!classname.startsWith("com.sap.sql") && !classname.startsWith("com.sap.engine.services.dbpool")) {
        	DatastoreMetrics.noticeSql(getConnection(), preparedSql, params);
        } else {
        	Connection conn = getConnectionFromPrepared();
        	DatastoreMetrics.noticeSql(conn, preparedSql, params);
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
    
	private Connection getConnectionFromPrepared() {
		Class<?> stmtClass = getClass();

		String classname = stmtClass.getName();

		try {
			if(classname.equals("com.sap.sql.jdbc.common.CommonPreparedStatement")) {
				Field extPreStmt = stmtClass.getDeclaredField("wrappedPrepStmt");
				extPreStmt.setAccessible(true);;
				Object extStmt = extPreStmt.get(this);
				if(extPreStmt != null) {
					Class<?> epsClass = extStmt.getClass();
					Field pStmtField = epsClass.getDeclaredField("prepStmt");
					pStmtField.setAccessible(true);
					Object pStmt = pStmtField.get(extStmt);
					Class<?> prepStmtClass = pStmt.getClass();
					
					Method getConnMethod = prepStmtClass.getDeclaredMethod("getConnection", new Class[] {});
					Object obj = getConnMethod.invoke(pStmt, new Object[] {});

					if(obj != null && obj instanceof Connection) {
						return (Connection)obj;
					}
				}
			} else if(classname.equals("com.sap.sql.jdbc.direct.DirectPreparedStatement")) {
				Field pStmtField = stmtClass.getDeclaredField("prepStmt");
				pStmtField.setAccessible(true);
				Object pStmt = pStmtField.get(this);
				Class<?> prepStmtClass = pStmt.getClass();
				Method getConnMethod = prepStmtClass.getDeclaredMethod("getConnection", new Class[] {});
				Object obj = getConnMethod.invoke(pStmt, new Object[] {});

				if(obj != null && obj instanceof Connection) {
					return (Connection)obj;
				}

			} else if(classname.equals("com.sap.engine.services.dbpool.wrappers.PreparedStatementWrapper")) {
				Field pstmtField = stmtClass.getDeclaredField("pstmt");
				pstmtField.setAccessible(true);
				Object obj = pstmtField.get(this);
				if(obj != null && obj instanceof PreparedStatement_Weaved) {
					stmtClass = obj.getClass();
					classname = stmtClass.getName();
					if(classname.equals("com.sap.sql.jdbc.common.CommonPreparedStatement")) {
						Field extPreStmt = stmtClass.getDeclaredField("wrappedPrepStmt");
						extPreStmt.setAccessible(true);
						Object extStmt = extPreStmt.get(obj);
						if(extPreStmt != null) {
							Class<?> epsClass = extStmt.getClass();
							Field pStmtField = epsClass.getDeclaredField("prepStmt");
							pStmtField.setAccessible(true);
							Object pStmt = pStmtField.get(extStmt);
							Class<?> prepStmtClass = pStmt.getClass();
							Method getConnMethod = prepStmtClass.getDeclaredMethod("getConnection", new Class[] {});
							Object obj2 = getConnMethod.invoke(pStmt, new Object[] {});

							if(obj2 != null && obj2 instanceof Connection) {
								return (Connection)obj2;
							}
						}
					} else if(classname.equals("com.sap.sql.jdbc.direct.DirectPreparedStatement")) {
						Field pStmtField = stmtClass.getDeclaredField("prepStmt");
						pStmtField.setAccessible(true);
						Object pStmt = pStmtField.get(obj);
						Class<?> prepStmtClass = pStmt.getClass();
						Method getConnMethod = prepStmtClass.getDeclaredMethod("getConnection", new Class[] {});
						Object obj3 = getConnMethod.invoke(pStmt, new Object[] {});

						if(obj3 != null && obj3 instanceof Connection) {
							return (Connection)obj3;
						}
					}
				}
			}
		} catch (Exception e) {
			NewRelic.getAgent().getLogger().log(Level.FINER, "Failed to get connection from {0} due to exception of type {1}",this,e.getClass().getSimpleName());
			NewRelic.incrementCounter("SAP/JDBC/FailedToGetConnection");
		}

		try {
			return this.getConnection();
		} catch (SQLException e) {
			return null;
		}
	}


}