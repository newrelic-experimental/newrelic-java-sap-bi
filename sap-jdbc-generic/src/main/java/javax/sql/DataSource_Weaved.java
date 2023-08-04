/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package javax.sql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

//import java.util.logging.Level;
//
//import javax.sql.DataSource;
//
//import com.newrelic.agent.Transaction;
//import com.newrelic.agent.TransactionActivity;
import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.agent.bridge.datastore.DatabaseVendor;
import com.newrelic.agent.bridge.datastore.DatastoreInstanceDetection;
//import com.newrelic.agent.bridge.datastore.DatastoreInstanceDetection;
import com.newrelic.agent.bridge.datastore.DatastoreMetrics;
import com.newrelic.agent.bridge.datastore.JdbcDataSourceConnectionFactory;
import com.newrelic.agent.bridge.datastore.JdbcHelper;
import com.newrelic.api.agent.NewRelic;
//import com.newrelic.agent.tracers.ClassMethodSignature;
//import com.newrelic.agent.tracers.DefaultSqlTracer;
//import com.newrelic.agent.tracers.DefaultTracer;
//import com.newrelic.agent.tracers.Tracer;
//import com.newrelic.agent.tracers.TracerFlags;
//import com.newrelic.agent.tracers.metricname.SimpleMetricNameFormat;
//import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
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
	
	@NewField
	private Boolean runChecks = null;

	public Connection getConnection() throws Exception {
		
		if(runChecks == null) {
			runChecks = checkClass();
		}
		
        boolean firstInConnectPath = runChecks ? !DatastoreInstanceDetection.shouldDetectConnectionAddress() : false;

        try {
        	
        	if(runChecks) {
        		DatastoreInstanceDetection.detectConnectionAddress();
        	}
            Connection connection = Weaver.callOriginal();
            AgentBridge.getAgent().getTracedMethod().addRollupMetricName(DatastoreMetrics.DATABASE_GET_CONNECTION);
            
            if(runChecks) {
            	NewRelic.getAgent().getLogger().log(Level.FINE, "In DataSource({0}).getConnection(), current address: {1}", getClass().getName(),DatastoreInstanceDetection.getCurrentAddress());
            }

            if (runChecks) {
				DatastoreInstanceDetection.associateAddress(connection);
				if (!JdbcHelper.connectionFactoryExists(connection)) {

					String url = JdbcHelper.getConnectionURL(connection);
					if (url == null) {
						return connection;
					}

					// Detect correct vendor type and then store new connection factory based on URL
					DatabaseVendor vendor = JdbcHelper.getVendor(getClass(), url);
					JdbcHelper.putConnectionFactory(url,
							new JdbcDataSourceConnectionFactory(vendor, (DataSource) this));
				} 
			}
			return connection;
        } catch (Exception e) {
            AgentBridge.getAgent().getMetricAggregator().incrementCounter(DatastoreMetrics.DATABASE_ERRORS_ALL);
            throw e;
        } finally {
            if (firstInConnectPath) {
                DatastoreInstanceDetection.stopDetectingConnectionAddress();
            }
        }


//		Transaction transaction = null;
//		DefaultTracer tracer = null;
//		boolean isMatch = checkClass();
//
//		if(isMatch) {
//			transaction = Transaction.getTransaction();
//			if (transaction != null) {
//				ClassMethodSignature sig = new ClassMethodSignature(getClass().getName(), "getConnection",
//						"()Ljava.sql.Connection;");
//				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
//				tracer = new DefaultTracer(transaction, sig, this,
//						new SimpleMetricNameFormat("JDBC/PreparedStatement/executeQuery"), tracerFlags);
//				TransactionActivity txa = transaction.getTransactionActivity();
//				Tracer parent = txa.getLastTracer();
//				tracer.setParentTracer(parent);
//				txa.tracerStarted(tracer);
//			}
//
//		} else {
//			String classname = getClass().getName();
//			if(!skipped.contains(classname)) {
//				NewRelic.getAgent().getLogger().log(Level.FINE, "In datasource {0}, skipping tracer creation", getClass().getName());
//				skipped.add(classname);
//			}
//		}
//		Connection connection = Weaver.callOriginal();
//
//		if(isMatch) {
//			boolean firstInConnectPath = !DatastoreInstanceDetection.shouldDetectConnectionAddress();
//			try {
//
//				DatastoreInstanceDetection.detectConnectionAddress();
//				AgentBridge.getAgent().getTracedMethod().addRollupMetricName(DatastoreMetrics.DATABASE_GET_CONNECTION);
//
//				DatastoreInstanceDetection.associateAddress(connection);
//
//
//				if (!JdbcHelper.connectionFactoryExists(connection)) {
//
//					String url = JdbcHelper.getConnectionURL(connection);
//					if (url == null) {
//						return connection;
//					}
//
//					// Detect correct vendor type and then store new connection factory based on URL
//					DatabaseVendor vendor = JdbcHelper.getVendor(getClass(), url);
//					JdbcHelper.putConnectionFactory(url, new JdbcDataSourceConnectionFactory(vendor, (DataSource) this));
//				}
//
//				if(transaction != null && tracer != null) {
//					tracer.finish(176, (Object)null);
//				}
//
//				return connection;
//			} catch (Exception e) {
//				AgentBridge.getAgent().getMetricAggregator().incrementCounter(DatastoreMetrics.DATABASE_ERRORS_ALL);
//				throw e;
//			} finally {
//				if (firstInConnectPath) {
//					DatastoreInstanceDetection.stopDetectingConnectionAddress();
//				}
//			}
//
//		} else {
//			return connection;
//		}
	}

	public Connection getConnection(String username, String password) throws Exception {
        boolean firstInConnectPath = !DatastoreInstanceDetection.shouldDetectConnectionAddress();
        try {

            DatastoreInstanceDetection.detectConnectionAddress();
            Connection connection = Weaver.callOriginal();
            AgentBridge.getAgent().getTracedMethod().addRollupMetricName(DatastoreMetrics.DATABASE_GET_CONNECTION);

            DatastoreInstanceDetection.associateAddress(connection);

            if (!JdbcHelper.connectionFactoryExists(connection)) {
                String url = JdbcHelper.getConnectionURL(connection);
                if (url == null) {
                    return connection;
                }

                // Detect correct vendor type and then store new connection factory based on URL
                DatabaseVendor vendor = JdbcHelper.getVendor(getClass(), url);
                JdbcHelper.putConnectionFactory(url, new JdbcDataSourceConnectionFactory(vendor, (DataSource) this,
                        username, password));
            }

            return connection;
        } catch (Exception e) {
            AgentBridge.getAgent().getMetricAggregator().incrementCounter(DatastoreMetrics.DATABASE_ERRORS_ALL);
            throw e;
        } finally {
            if (firstInConnectPath) {
                DatastoreInstanceDetection.stopDetectingConnectionAddress();
            }
        }

//		Transaction transaction = null;
//		DefaultTracer tracer = null;
//		boolean isMatch = checkClass();
//
//		if(isMatch) {
//			transaction = Transaction.getTransaction();
//			if (transaction != null) {
//				ClassMethodSignature sig = new ClassMethodSignature(getClass().getName(), "getConnection",
//						"()Ljava.sql.Connection;");
//				int tracerFlags = DefaultSqlTracer.DEFAULT_TRACER_FLAGS | TracerFlags.LEAF;
//				tracer = new DefaultTracer(transaction, sig, this,
//						new SimpleMetricNameFormat("JDBC/PreparedStatement/executeQuery"), tracerFlags);
//				TransactionActivity txa = transaction.getTransactionActivity();
//				Tracer parent = txa.getLastTracer();
//				tracer.setParentTracer(parent);
//				txa.tracerStarted(tracer);
//			}
//		}	else {
//				String classname = getClass().getName();
//				if(!skipped.contains(classname)) {
//					NewRelic.getAgent().getLogger().log(Level.FINE, "In datasource {0}, skipping tracer creation", getClass().getName());
//					skipped.add(classname);
//				}
//
//			}
//			Connection connection = Weaver.callOriginal();
//
//			if(isMatch) {
//				boolean firstInConnectPath = !DatastoreInstanceDetection.shouldDetectConnectionAddress();
//				try {
//
//					DatastoreInstanceDetection.detectConnectionAddress();
//					AgentBridge.getAgent().getTracedMethod().addRollupMetricName(DatastoreMetrics.DATABASE_GET_CONNECTION);
//
//					DatastoreInstanceDetection.associateAddress(connection);
//
//
//					if (!JdbcHelper.connectionFactoryExists(connection)) {
//
//						String url = JdbcHelper.getConnectionURL(connection);
//						if (url == null) {
//							return connection;
//						}
//
//						// Detect correct vendor type and then store new connection factory based on URL
//						DatabaseVendor vendor = JdbcHelper.getVendor(getClass(), url);
//						JdbcHelper.putConnectionFactory(url, new JdbcDataSourceConnectionFactory(vendor, (DataSource) this));
//					}
//
//					if(transaction != null && tracer != null) {
//						tracer.finish(176, (Object)null);
//					}
//
//					return connection;
//				} catch (Exception e) {
//					AgentBridge.getAgent().getMetricAggregator().incrementCounter(DatastoreMetrics.DATABASE_ERRORS_ALL);
//					throw e;
//				} finally {
//					if (firstInConnectPath) {
//						DatastoreInstanceDetection.stopDetectingConnectionAddress();
//					}
//				}
//
//			} else {
//				return connection;
//			}
		}


		//	private Connection connectionToUse(Connection connection) {
		//
		//		Class<?> connectionClass = connection.getClass();
		//		String classname = connectionClass.getName();
		//		if(!classname.startsWith("com.sap") && !classname.startsWith("com.tssap")) return connection;
		//
		//		Connection conn = connection;
		//
		//		Connection toUse = null;
		//
		//		while(connectionClass != null) {
		//			try {
		//				if(classname.equals("com.sap.sql.jdbc.common.CommonConnectionImpl")) {
		//					Field nativeConnField = connectionClass.getDeclaredField("nativeConnection");
		//					nativeConnField.setAccessible(true);
		//					Object connObj = nativeConnField.get(conn);
		//					if(connObj != null) {
		//						toUse = (Connection)connObj;
		//					} else {
		//						toUse = conn;
		//					}
		//					connectionClass = null;
		//				} else if(classname.equals("com.sap.engine.services.dbpool.cci.ConnectionHandle") || classname.equals("com.sap.engine.services.dbpool.cci.CommonConnectionHandle")) {
		//					Method getPhyConnMethod = connectionClass.getMethod("getPhysicalConnection", new Class<?>[] {});
		//					Object phyConn = getPhyConnMethod.invoke(conn, new Object[] {});
		//					if(phyConn != null) {
		//						connectionClass = phyConn.getClass();
		//						classname = connectionClass.getName();
		//						conn = (Connection) phyConn;
		//					}
		//				} else if(classname.equals("com.tssap.dtr.pvc.basics.transaction.SharedConnection")) {
		//					Field connField = connectionClass.getDeclaredField("connection");
		//					connField.setAccessible(true);
		//					Object connObj = connField.get(conn);
		//					if(connObj != null) {
		//						connectionClass = connObj.getClass();
		//						classname = connectionClass.getName();
		//						conn = (Connection)connObj;
		//					} else {
		//						connectionClass = null;
		//					}
		//
		//				} else {
		//					connectionClass = null;
		//					toUse = conn;
		//				}
		//			} catch (Exception e) {
		//				String exceptionClass = e.getClass().getSimpleName();
		//				NewRelic.incrementCounter("SAP/JDBC/Datasource/FailedToFindConnection");
		//				NewRelic.incrementCounter("SAP/JDBC/Datasource/FailedToFindConnection/"+getClass().getSimpleName()+"/"+conn.getClass().getSimpleName()+"/"+exceptionClass);
		//				return connection;
		//			}
		//		}
		//
		//		return toUse;
		//	}

		private boolean checkClass() {
			if(this instanceof javax.sql.DataSource) {
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