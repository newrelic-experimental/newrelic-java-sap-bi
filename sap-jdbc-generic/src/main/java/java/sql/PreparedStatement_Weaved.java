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
			String query = JdbcHelper.getSql((Statement) this);
			if(query == null) {
				query = getSQLFromPrepared();
			}
			preparedSql = query;
		}
		if(preparedSql == null || preparedSql.isEmpty()) {
			NewRelic.getAgent().getLogger().log(Level.FINER, "Value of preparedSql in {0}.executeQuery is {1}", this,preparedSql);
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
			String query = JdbcHelper.getSql((Statement) this);
			if(query == null) {
				query = getSQLFromPrepared();
			}
			preparedSql = query;
		}
		if(preparedSql == null || preparedSql.isEmpty()) {
			NewRelic.getAgent().getLogger().log(Level.FINER, "Value of preparedSql in {0}.executeQuery is {1}", this,preparedSql);
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
			String query = JdbcHelper.getSql((Statement) this);
			if(query == null) {
				query = getSQLFromPrepared();
			}
			preparedSql = query;
		}
		if(preparedSql == null || preparedSql.isEmpty()) {
			NewRelic.getAgent().getLogger().log(Level.FINER, "Value of preparedSql in {0}.executeQuery is {1}", this,preparedSql);
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

	private String getSQLFromPrepared() {
		Class<?> stmtClass = getClass();

		String classname = stmtClass.getName();
		Object objToUse = this;
		while(stmtClass != null) {
		try {
			if(classname.equals("com.sap.engine.services.dbpool.wrappers.PreparedStatementWrapper")) {
				Field pstmtField = stmtClass.getDeclaredField("pstmt");
				pstmtField.setAccessible(true);
				Object pstmt = pstmtField.get(objToUse);
				if(pstmt != null) {
					PreparedStatement_Weaved newStmt = (PreparedStatement_Weaved)pstmt;
					if(newStmt.preparedSql != null) {
						return newStmt.preparedSql;
					}
					stmtClass = pstmt.getClass();
					classname = stmtClass.getName();
					objToUse = pstmt;
				} else {
					stmtClass = null;
				}
				
			} else if(classname.equals("com.sap.sql.jdbc.common.CommonPreparedStatement")) {
				PreparedStatement_Weaved newStmt = (PreparedStatement_Weaved)objToUse;
				if(newStmt.preparedSql != null) {
					return newStmt.preparedSql;
				}
				
				Method getSqlMethod = stmtClass.getDeclaredMethod("getSql", new Class<?>[] {});
				Object result = getSqlMethod.invoke(objToUse, new Object[] {});
				if(result != null) {
					return result.toString();
				}
				
				Field pstmtField = stmtClass.getDeclaredField("wrappedPrepStmt");
				pstmtField.setAccessible(true);
				Object pstmt = pstmtField.get(objToUse);
				if(pstmt != null) {
					stmtClass = pstmt.getClass();
					classname = stmtClass.getName();
					objToUse = pstmt;
				} else {
					stmtClass = null;
				}
				
			} else if(classname.equals("com.sap.sql.jdbc.direct.DirectPreparedStatement")) {
				PreparedStatement_Weaved newStmt = (PreparedStatement_Weaved)objToUse;
				if(newStmt.preparedSql != null) {
					return newStmt.preparedSql;
				}
			
				Field prepStmtField = stmtClass.getDeclaredField("prepStmt");
				prepStmtField.setAccessible(true);
				Object prepStmt = prepStmtField.get(objToUse);
				if(prepStmt != null) {
					stmtClass = prepStmt.getClass();
					classname = stmtClass.getName();
					objToUse = prepStmt;
				} else {
					stmtClass = null;
				}
			} else if(classname.equals("com.sap.dbtech.jdbc.trace.PreparedStatement")) {
				PreparedStatement_Weaved newStmt = (PreparedStatement_Weaved)objToUse;
				if(newStmt.preparedSql != null) {
					return newStmt.preparedSql;
				}
				
				Field innerField = stmtClass.getDeclaredField("_inner");
				innerField.setAccessible(true);
				Object prepStmt = innerField.get(objToUse);
				if(prepStmt != null) {
					stmtClass = prepStmt.getClass();
					classname = stmtClass.getName();
					objToUse = prepStmt;
				} else {
					stmtClass = null;
				}
			} else if(classname.equals("com.sap.dbtech.jdbc.CallableStatementSapDBFinalize") || classname.equals("com.sap.dbtech.jdbc.CallableStatementSapDB")) {
				PreparedStatement_Weaved newStmt = (PreparedStatement_Weaved)objToUse;
				if(newStmt.preparedSql != null) {
					return newStmt.preparedSql;
				}
				
				if(classname.equals("com.sap.dbtech.jdbc.CallableStatementSapDBFinalize")) {
					stmtClass = stmtClass.getSuperclass();
				}
				Field parseinfoField = stmtClass.getDeclaredField("parseinfo");
				parseinfoField.setAccessible(true);
				Object parseInfo = parseinfoField.get(objToUse);
				if(parseInfo != null) {
					Class<?> parseInfoClass = parseInfo.getClass();
					
					Field sqlCmdField = parseInfoClass.getDeclaredField("sqlCmd");
					sqlCmdField.setAccessible(true);
					Object sqlCmd = sqlCmdField.get(parseInfo);
					if(sqlCmd != null) {
						return sqlCmd.toString();
					}
				}
			} else {
				stmtClass = null;
				NewRelic.getAgent().getLogger().log(Level.FINE, "Could not find SQL for {0}", objToUse);
			}
		} catch (Exception e) {
			NewRelic.incrementCounter("/SAP/JDBC/getSQLFromPrepared/Failed");
			NewRelic.incrementCounter("/SAP/JDBC/getSQLFromPrepared/Failed/"+e.getClass().getSimpleName());
			stmtClass = null;
		}
		}
		return null;
		
	}

	private Connection getConnectionFromPrepared() {
		Class<?> stmtClass = getClass();

		String classname = stmtClass.getName();
		Object objToUse = this;

		while(stmtClass != null) {
			try {
				if(classname.equals("com.sap.sql.jdbc.common.CommonPreparedStatement")) {
					Field extPreStmt = stmtClass.getDeclaredField("wrappedPrepStmt");
					extPreStmt.setAccessible(true);;
					Object extStmt = extPreStmt.get(objToUse);
					if(extStmt != null) {
						objToUse = extStmt;
						stmtClass = extStmt.getClass();
						classname = stmtClass.getName();
					} else {
						stmtClass = null;
						NewRelic.incrementCounter("SAP/JDBC/CommonPreparedStatement/Failed");
					}
				} else if(classname.equals("com.sap.sql.jdbc.direct.DirectPreparedStatement")) {
					Field pStmtField = stmtClass.getDeclaredField("prepStmt");
					pStmtField.setAccessible(true);
					Object pStmt = pStmtField.get(objToUse);
					Class<?> prepStmtClass = pStmt.getClass();
					if (!prepStmtClass.getName().startsWith("com.sap.sql")) {
						Method getConnMethod = prepStmtClass.getDeclaredMethod("getConnection", new Class[] {});
						Object obj = getConnMethod.invoke(pStmt, new Object[] {});
						if (obj != null && obj instanceof Connection) {
							return (Connection) obj;
						} 
					} else {
						stmtClass = prepStmtClass;
						objToUse = pStmt;
						classname = stmtClass.getName();
					}

				} else if(classname.equals("com.sap.engine.services.dbpool.wrappers.PreparedStatementWrapper")) {
					Field pstmtField = stmtClass.getDeclaredField("pstmt");
					pstmtField.setAccessible(true);
					Object obj = pstmtField.get(objToUse);
					if(obj != null && obj instanceof PreparedStatement_Weaved) {
						stmtClass = obj.getClass();
						classname = stmtClass.getName();
						objToUse = obj;
					} else {
						stmtClass = null;
						NewRelic.incrementCounter("SAP/JDBC/PreparedStatementWrapper/Failed");
					}
				} else if(classname.equals("com.tssap.dtr.pvc.basics.transaction.PreparedStatementReleasingConnection")) {
					Field wrappedField = stmtClass.getDeclaredField("wrappedPreparedStatement");
					wrappedField.setAccessible(true);
					Object stmtObj = wrappedField.get(objToUse);
					if(stmtObj != null) {
						stmtClass = stmtObj.getClass();
						classname = stmtClass.getName();
						objToUse = stmtObj;
					}
				} else if(classname.startsWith("com.sap.jdbc")) {
					Class<?> superClass = stmtClass.getSuperclass();
					if(superClass != null) {
						String cName = superClass.getName();
						if(cName.equals("com.sap.sql.jdbc.basic.BasicPreparedStatement")) {
							Field pstmtField = stmtClass.getDeclaredField("prepStmt");
							pstmtField.setAccessible(true);
							Object pStmtObj = pstmtField.get(objToUse);
							if(pStmtObj != null) {
								stmtClass = pStmtObj.getClass();
								objToUse = pStmtObj;
								classname = stmtClass.getName();
							}
						} else {
							stmtClass = null;
						}
					} else {
						stmtClass = null;
					}


				} else {
					stmtClass = null;
				}
			} catch (Exception e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, "Failed to get connection from {0} due to exception of type {1}",objToUse,e.getClass().getSimpleName());
				NewRelic.incrementCounter("SAP/JDBC/FailedToGetConnection");
				NewRelic.incrementCounter("SAP/JDBC/FailedToGetConnection/"+objToUse.getClass().getSimpleName()+"/"+e.getClass().getSimpleName());

				stmtClass = null;
			}
		}

		try {
			stmtClass = objToUse.getClass();
			Method getConnMethod = stmtClass.getDeclaredMethod("getConnection", new Class<?>[] {});
			Object conn = getConnMethod.invoke(objToUse, new Object[] {});
			return (Connection)conn;

		} catch (Exception e) {
			return null;
		}
	}


}