/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package javax.sql;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.agent.bridge.datastore.DatabaseVendor;
//import com.newrelic.agent.bridge.datastore.DatastoreInstanceDetection;
import com.newrelic.agent.bridge.datastore.DatastoreMetrics;
import com.newrelic.agent.bridge.datastore.JdbcDataSourceConnectionFactory;
import com.newrelic.agent.bridge.datastore.JdbcHelper;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

/**
 * This interface match is here to properly record every time that a connection is requested from a data source.
 * Normally this could just live in each JDBC driver module, but this is generic enough that we want to capture it for
 * all JDBC drivers.
 * 
 * This instrumentation attempts to get the connection and if it's successful it will be returned and an unscoped metric
 * will be generated, otherwise we will record a metric indicating that an error occurred and re-throw the error.
 */
@Weave(originalName = "javax.sql.DataSource", type = MatchType.Interface)
public abstract class DataSource_Weaved {

	// This is a leaf tracer because it's common for these methods to delegate to each other and we don't want double
	// counts
	@Trace(leaf = true)
	public Connection getConnection() throws Exception {
//		boolean firstInConnectPath = !DatastoreInstanceDetection.shouldDetectConnectionAddress();

		try {
//			DatastoreInstanceDetection.detectConnectionAddress();
			Connection connection = Weaver.callOriginal();
			AgentBridge.getAgent().getTracedMethod().addRollupMetricName(DatastoreMetrics.DATABASE_GET_CONNECTION);

			Connection toUse = connectionToUse(connection);
			

//			DatastoreInstanceDetection.associateAddress(toUse);


			if (!JdbcHelper.connectionFactoryExists(toUse)) {

					String url = JdbcHelper.getConnectionURL(toUse);
					if (url == null) {
						return connection;
					}
					// Detect correct vendor type and then store new connection factory based on URL
					DatabaseVendor vendor = JdbcHelper.getVendor(getClass(), url);
					JdbcHelper.putConnectionFactory(url,
							new JdbcDataSourceConnectionFactory(vendor, (DataSource) this));
			}

			return connection;
		} catch (Exception e) {
			AgentBridge.getAgent().getMetricAggregator().incrementCounter(DatastoreMetrics.DATABASE_ERRORS_ALL);
			throw e;
		} finally {
//			if (firstInConnectPath) {
//				DatastoreInstanceDetection.stopDetectingConnectionAddress();
//			}
		}
	}

	// This is a leaf tracer because it's common for these methods to delegate to each other and we don't want double
	// counts
	@Trace(leaf = true)
	public Connection getConnection(String username, String password) throws Exception {
//		boolean firstInConnectPath = !DatastoreInstanceDetection.shouldDetectConnectionAddress();

		try {
//			DatastoreInstanceDetection.detectConnectionAddress();
			Connection connection = Weaver.callOriginal();
			AgentBridge.getAgent().getTracedMethod().addRollupMetricName(DatastoreMetrics.DATABASE_GET_CONNECTION);

			Connection toUse = connectionToUse(connection);
			

//			DatastoreInstanceDetection.associateAddress(toUse);


			if (!JdbcHelper.connectionFactoryExists(toUse)) {

					String url = JdbcHelper.getConnectionURL(toUse);
					if (url == null) {
						return connection;
					}
					// Detect correct vendor type and then store new connection factory based on URL
					DatabaseVendor vendor = JdbcHelper.getVendor(getClass(), url);
					JdbcHelper.putConnectionFactory(url,
							new JdbcDataSourceConnectionFactory(vendor, (DataSource) this));
			}

			return connection;
		} catch (Exception e) {
			AgentBridge.getAgent().getMetricAggregator().incrementCounter(DatastoreMetrics.DATABASE_ERRORS_ALL);
			throw e;
		} finally {
//			if (firstInConnectPath) {
//				DatastoreInstanceDetection.stopDetectingConnectionAddress();
//			}
		}
	}

	
	private Connection connectionToUse(Connection connection) {
		
		Class<?> connectionClass = connection.getClass();
		String classname = connectionClass.getName();
		if(!classname.startsWith("com.sap") && !classname.startsWith("com.tssap")) return connection;
		
		Connection conn = connection;

		Connection toUse = null;

		while(connectionClass != null) {
			try {
				if(classname.equals("com.sap.sql.jdbc.common.CommonConnectionImpl")) {
					Field nativeConnField = connectionClass.getDeclaredField("nativeConnection");
					nativeConnField.setAccessible(true);
					Object connObj = nativeConnField.get(conn);
					if(connObj != null) {
						toUse = (Connection)connObj;
					} else {
						toUse = conn;
					}
					connectionClass = null;
				} else if(classname.equals("com.sap.engine.services.dbpool.cci.ConnectionHandle") || classname.equals("com.sap.engine.services.dbpool.cci.CommonConnectionHandle")) {
					Method getPhyConnMethod = connectionClass.getMethod("getPhysicalConnection", new Class<?>[] {});
					Object phyConn = getPhyConnMethod.invoke(conn, new Object[] {});
					if(phyConn != null) {
						connectionClass = phyConn.getClass();
						classname = connectionClass.getName();
						conn = (Connection) phyConn;
					}
				} else if(classname.equals("com.tssap.dtr.pvc.basics.transaction.SharedConnection")) {
					Field connField = connectionClass.getDeclaredField("connection");
					connField.setAccessible(true);
					Object connObj = connField.get(conn);
					if(connObj != null) {
						connectionClass = connObj.getClass();
						classname = connectionClass.getName();
						conn = (Connection)connObj;
					} else {
						connectionClass = null;
					}
					
				} else {
					connectionClass = null;
					toUse = conn;
				}
			} catch (Exception e) {
				String exceptionClass = e.getClass().getSimpleName();
				NewRelic.incrementCounter("SAP/JDBC/Datasource/FailedToFindConnection");
				NewRelic.incrementCounter("SAP/JDBC/Datasource/FailedToFindConnection/"+getClass().getSimpleName()+"/"+conn.getClass().getSimpleName()+"/"+exceptionClass);
				return connection;
			}
		}
		
		return toUse;
	}
}