/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package java.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.newrelic.agent.Transaction;
import com.newrelic.agent.TransactionActivity;
import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.agent.bridge.ExitTracer;
import com.newrelic.agent.bridge.datastore.DatastoreMetrics;
import com.newrelic.agent.tracers.ClassMethodSignature;
import com.newrelic.agent.tracers.ClassMethodSignatures;
import com.newrelic.agent.tracers.DefaultSqlTracer;
import com.newrelic.agent.tracers.DefaultTracer;
import com.newrelic.agent.tracers.Tracer;
import com.newrelic.agent.tracers.TracerFlags;
import com.newrelic.agent.tracers.metricname.SimpleMetricNameFormat;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(originalName = "java.sql.Statement", type = MatchType.Interface)
public abstract class Statement_Weaved {

	@NewField
	private static List<String> skipped = new ArrayList<>();


	public ResultSet executeQuery(String sql) throws SQLException {
		ExitTracer tracer = null;
		boolean isMatch = checkClass();
		if(isMatch) {
			// Create tracer using AgentBridge.createSqlTracer pattern
			ClassMethodSignature signature = new ClassMethodSignature(getClass().getName(), "executeQuery", "(Ljava.lang.String;)Ljava.sql.ResultSet;");
			int index = ClassMethodSignatures.get().getIndex(signature);
			if(index == -1) {
				index = ClassMethodSignatures.get().add(signature);
			}
			
			if(index >= 0) {
				String metricName = "JDBC/Statement/executeQuery";
				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
				tracer = AgentBridge.instrumentation.createSqlTracer(this, index, metricName, tracerFlags);
				if(tracer != null) {
					DatastoreMetrics.noticeSql(getConnection(), sql, null);
				}
			}
		} else {
			String classname = getClass().getName();
			if(!skipped.contains(classname)) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "In statement {0}, skipping tracer creation", getClass().getName());
				skipped.add(classname);
			}
		}

		ResultSet rs = null;
		try {
			rs = Weaver.callOriginal();
		} catch (Exception e) {
			if(tracer != null) {
				tracer.finish(e);
			}
			throw e;
		}
		
		if(tracer != null) {
			tracer.finish(0, rs);
		}
		return rs;
	}

	public int executeUpdate(String sql) throws SQLException {
		ExitTracer tracer = null;
		boolean isMatch = checkClass();
		if(isMatch) {
			// Create tracer using AgentBridge.createSqlTracer pattern
			ClassMethodSignature signature = new ClassMethodSignature(getClass().getName(), "executeUpdate", "(Ljava.lang.String;)I");
			int index = ClassMethodSignatures.get().getIndex(signature);
			if(index == -1) {
				index = ClassMethodSignatures.get().add(signature);
			}
			
			if(index >= 0) {
				String metricName = "JDBC/Statement/executeUpdate";
				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
				tracer = AgentBridge.instrumentation.createSqlTracer(this, index, metricName, tracerFlags);
				if(tracer != null) {
					DatastoreMetrics.noticeSql(getConnection(), sql, null);
				}
			}
		} else {
			String classname = getClass().getName();
			if(!skipped.contains(classname)) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "In statement {0}, skipping tracer creation", getClass().getName());
				skipped.add(classname);
			}
		}

		int rs = 0;
		try {
			rs = Weaver.callOriginal();
		} catch (Exception e) {
			if(tracer != null) {
				tracer.finish(e);
			}
			throw e;
		}
		
		if(tracer != null) {
			tracer.finish(0, rs);
		}
		return rs;	
	}

	public boolean execute(String sql) throws SQLException {
		ExitTracer tracer = null;
		boolean isMatch = checkClass();
		if(isMatch) {
			// Create tracer using AgentBridge.createSqlTracer pattern
			ClassMethodSignature signature = new ClassMethodSignature(getClass().getName(), "execute", "(Ljava.lang.String;)Z");
			int index = ClassMethodSignatures.get().getIndex(signature);
			if(index == -1) {
				index = ClassMethodSignatures.get().add(signature);
			}
			
			if(index >= 0) {
				String metricName = "JDBC/Statement/execute";
				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
				tracer = AgentBridge.instrumentation.createSqlTracer(this, index, metricName, tracerFlags);
				if(tracer != null) {
					DatastoreMetrics.noticeSql(getConnection(), sql, null);
				}
			}
		} else {
			String classname = getClass().getName();
			if(!skipped.contains(classname)) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "In statement {0}, skipping tracer creation", getClass().getName());
				skipped.add(classname);
			}
		}
		
		boolean rs = false;
		try {
			rs = Weaver.callOriginal();
		} catch (Exception e) {
			if(tracer != null) {
				tracer.finish(e);
			}
			throw e;
		}
		
		if(tracer != null) {
			tracer.finish(0, rs);
		}
		return rs;		
	}

	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		ExitTracer tracer = null;
		boolean isMatch = checkClass();
		if(isMatch) {
			// Create tracer using AgentBridge.createSqlTracer pattern
			ClassMethodSignature signature = new ClassMethodSignature(getClass().getName(), "executeUpdate", "(Ljava.lang.String;I)I");
			int index = ClassMethodSignatures.get().getIndex(signature);
			if(index == -1) {
				index = ClassMethodSignatures.get().add(signature);
			}
			
			if(index >= 0) {
				String metricName = "JDBC/Statement/executeUpdate";
				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
				tracer = AgentBridge.instrumentation.createSqlTracer(this, index, metricName, tracerFlags);
				if(tracer != null) {
					DatastoreMetrics.noticeSql(getConnection(), sql, null);
				}
			}
		} else {
			String classname = getClass().getName();
			if(!skipped.contains(classname)) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "In statement {0}, skipping tracer creation", getClass().getName());
				skipped.add(classname);
			}
		}

		int rs = 0;
		try {
			rs = Weaver.callOriginal();
		} catch (Exception e) {
			if(tracer != null) {
				tracer.finish(e);
			}
			throw e;
		}
		
		if(tracer != null) {
			tracer.finish(0, rs);
		}
		return rs;		
	}

	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		ExitTracer tracer = null;
		boolean isMatch = checkClass();
		if(isMatch) {
			// Create tracer using AgentBridge.createSqlTracer pattern
			ClassMethodSignature signature = new ClassMethodSignature(getClass().getName(), "executeUpdate", "(Ljava.lang.String;[I)I");
			int index = ClassMethodSignatures.get().getIndex(signature);
			if(index == -1) {
				index = ClassMethodSignatures.get().add(signature);
			}
			
			if(index >= 0) {
				String metricName = "JDBC/Statement/executeUpdate";
				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
				tracer = AgentBridge.instrumentation.createSqlTracer(this, index, metricName, tracerFlags);
				if(tracer != null) {
					DatastoreMetrics.noticeSql(getConnection(), sql, null);
				}
			}
		} else {
			String classname = getClass().getName();
			if(!skipped.contains(classname)) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "In statement {0}, skipping tracer creation", getClass().getName());
				skipped.add(classname);
			}
		}

		int rs = 0;
		try {
			rs = Weaver.callOriginal();
		} catch (Exception e) {
			if(tracer != null) {
				tracer.finish(e);
			}
			throw e;
		}
		
		if(tracer != null) {
			tracer.finish(0, rs);
		}
		return rs;		
	}

	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		ExitTracer tracer = null;
		boolean isMatch = checkClass();
		if(isMatch) {
			// Create tracer using AgentBridge.createSqlTracer pattern
			ClassMethodSignature signature = new ClassMethodSignature(getClass().getName(), "execute", "(Ljava.lang.String;I)Z");
			int index = ClassMethodSignatures.get().getIndex(signature);
			if(index == -1) {
				index = ClassMethodSignatures.get().add(signature);
			}
			
			if(index >= 0) {
				String metricName = "JDBC/Statement/execute";
				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
				tracer = AgentBridge.instrumentation.createSqlTracer(this, index, metricName, tracerFlags);
				if(tracer != null) {
					DatastoreMetrics.noticeSql(getConnection(), sql, null);
				}
			}
		} else {
			String classname = getClass().getName();
			if(!skipped.contains(classname)) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "In statement {0}, skipping tracer creation", getClass().getName());
				skipped.add(classname);
			}
		}
		
		boolean rs = false;
		try {
			rs = Weaver.callOriginal();
		} catch (Exception e) {
			if(tracer != null) {
				tracer.finish(e);
			}
			throw e;
		}
		
		if(tracer != null) {
			tracer.finish(0, rs);
		}
		return rs;		
	}

	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		ExitTracer tracer = null;
		boolean isMatch = checkClass();
		if(isMatch) {
			// Create tracer using AgentBridge.createSqlTracer pattern
			ClassMethodSignature signature = new ClassMethodSignature(getClass().getName(), "executeUpdate", "(Ljava.lang.String;[Ljava.lang.String;)I");
			int index = ClassMethodSignatures.get().getIndex(signature);
			if(index == -1) {
				index = ClassMethodSignatures.get().add(signature);
			}
			
			if(index >= 0) {
				String metricName = "JDBC/Statement/executeUpdate";
				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
				tracer = AgentBridge.instrumentation.createSqlTracer(this, index, metricName, tracerFlags);
				if(tracer != null) {
					DatastoreMetrics.noticeSql(getConnection(), sql, null);
				}
			}
		} else {
			String classname = getClass().getName();
			if(!skipped.contains(classname)) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "In statement {0}, skipping tracer creation", getClass().getName());
				skipped.add(classname);
			}
		}

		int rs = 0;
		try {
			rs = Weaver.callOriginal();
		} catch (Exception e) {
			if(tracer != null) {
				tracer.finish(e);
			}
			throw e;
		}
		
		if(tracer != null) {
			tracer.finish(0, rs);
		}
		return rs;		
	}

	public boolean execute(String sql, String[] columnNames) throws SQLException {
		ExitTracer tracer = null;
		boolean isMatch = checkClass();
		if(isMatch) {
			// Create tracer using AgentBridge.createSqlTracer pattern
			ClassMethodSignature signature = new ClassMethodSignature(getClass().getName(), "execute", "(Ljava.lang.String;[Ljava.lang.String;)Z");
			int index = ClassMethodSignatures.get().getIndex(signature);
			if(index == -1) {
				index = ClassMethodSignatures.get().add(signature);
			}
			
			if(index >= 0) {
				String metricName = "JDBC/Statement/execute";
				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
				tracer = AgentBridge.instrumentation.createSqlTracer(this, index, metricName, tracerFlags);
				if(tracer != null) {
					DatastoreMetrics.noticeSql(getConnection(), sql, null);
				}
			}
		} else {
			String classname = getClass().getName();
			if(!skipped.contains(classname)) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "In statement {0}, skipping tracer creation", getClass().getName());
				skipped.add(classname);
			}
		}
		
		boolean rs = false;
		try {
			rs = Weaver.callOriginal();
		} catch (Exception e) {
			if(tracer != null) {
				tracer.finish(e);
			}
			throw e;
		}
		
		if(tracer != null) {
			tracer.finish(0, rs);
		}
		return rs;		
	}

	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		ExitTracer tracer = null;
		boolean isMatch = checkClass();
		if(isMatch) {
			// Create tracer using AgentBridge.createSqlTracer pattern
			ClassMethodSignature signature = new ClassMethodSignature(getClass().getName(), "execute", "(Ljava.lang.String;[I)Z");
			int index = ClassMethodSignatures.get().getIndex(signature);
			if(index == -1) {
				index = ClassMethodSignatures.get().add(signature);
			}
			
			if(index >= 0) {
				String metricName = "JDBC/Statement/execute";
				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
				tracer = AgentBridge.instrumentation.createSqlTracer(this, index, metricName, tracerFlags);
				if(tracer != null) {
					DatastoreMetrics.noticeSql(getConnection(), sql, null);
				}
			}
		} else {
			String classname = getClass().getName();
			if(!skipped.contains(classname)) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "In statement {0}, skipping tracer creation", getClass().getName());
				skipped.add(classname);
			}
		}
		
		boolean rs = false;
		try {
			rs = Weaver.callOriginal();
		} catch (Exception e) {
			if(tracer != null) {
				tracer.finish(e);
			}
			throw e;
		}
		
		if(tracer != null) {
			tracer.finish(0, rs);
		}
		return rs;		
	}

	public abstract Connection getConnection() throws SQLException;

//	private Connection getConnectionFromStatement() {
//		Class<?> stmtClass = getClass();
//		String classname = stmtClass.getName();
//		Object objToUse = this;
//
//		while(stmtClass != null) {
//			try {
//				if(classname.equals("com.sap.sql.jdbc.common.CommonStatement")) {
//					Field nativeConnectionField = stmtClass.getDeclaredField("nativeConnection");
//					nativeConnectionField.setAccessible(true);;
//					Object nativeConnection = nativeConnectionField.get(objToUse);
//					if(nativeConnectionField != null) {
//						if(nativeConnection != null && nativeConnection instanceof Connection) {
//							return (Connection)nativeConnection;
//						}
//					}
//				} else if(classname.equals("com.sap.sql.jdbc.direct.DirectStatement")) {
//					Field pStmtField = stmtClass.getDeclaredField("vendorStmt");
//					pStmtField.setAccessible(true);
//					Object pStmt = pStmtField.get(objToUse);
//					Class<?> prepStmtClass = pStmt.getClass();
//					Method getConnMethod = prepStmtClass.getDeclaredMethod("getConnection", new Class[] {});
//					Object obj = getConnMethod.invoke(pStmt, new Object[] {});
//
//					if(obj != null && obj instanceof Connection) {
//						return (Connection)obj;
//					}
//
//				} else if(classname.equals("com.sap.engine.services.dbpool.wrappers.StatementWrapper")) {
//					Field pstmtField = stmtClass.getDeclaredField("stmt");
//					pstmtField.setAccessible(true);
//					Object obj = pstmtField.get(this);
//					if(obj != null && obj instanceof Statement_Weaved) {
//						stmtClass = obj.getClass();
//						classname = stmtClass.getName();
//						objToUse = obj;
//					}
//				} else {
//					stmtClass = null;
//				}
//			} catch (Exception e) {
//				NewRelic.getAgent().getLogger().log(Level.FINER, "/{0} due to exception of type {1}",this,e.getClass().getSimpleName());
//				NewRelic.incrementCounter("SAP/JDBC/FailedToGetConnection");
//				NewRelic.incrementCounter("SAP/JDBC/FailedToGetConnection/"+objToUse.getClass().getSimpleName()+"/"+e.getClass().getSimpleName());
//				stmtClass = null;
//			}
//		}
//
//		try {
//			return this.getConnection();
//		} catch (SQLException e) {
//			return null;
//		}
//	}

	private boolean checkClass() {
		if(this instanceof java.sql.Statement) {
			Class<?> clazz = getClass();
			Package clazzPackage = clazz.getPackage();
			if(clazzPackage != null) {
				String packageName = clazzPackage.getName();
				if(!packageName.startsWith("com.sap")) return true;
//				if(packageName.startsWith("com.sap.dbtech")) return true;
			}
		}
		return false;

	}


}