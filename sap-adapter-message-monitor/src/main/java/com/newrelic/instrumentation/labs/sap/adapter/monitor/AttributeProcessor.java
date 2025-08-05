package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import static com.newrelic.instrumentation.labs.sap.adapter.monitor.AdapterMonitorLogger.logErrorWithMessage;
import static com.newrelic.instrumentation.labs.sap.adapter.monitor.AdapterMonitorLogger.logMessage;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.newrelic.api.agent.Logger;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.service.db.ConnectionManager;
import com.sap.engine.interfaces.messaging.api.MessageKey;

public class AttributeProcessor {

	protected static Map<String, Map<String,String>> message_Attributes = new HashMap<String, Map<String,String>>(10);
	private static final long THRESHOLD = 86400 * 1000L;
	private static final int HOURS_BETWEEN_PURGES = 4;
	private static final String MESSAGEID_COLUMN = "MESSAGEID";
	private static final String MESSAGEATTRIBUTES_COLUMN = "MESSAGE_ATTRIBUTES";
	private static final String LAST_MODIFIED_COLUMN = "LAST_MODIFIED";
	
	protected static final String TABLE_NAME = "NR_ADAPTER_ATTRIBUTES";
	private static final String INSERT_QUERY = "INSERT INTO " + TABLE_NAME + "(" + MESSAGEID_COLUMN +"," + MESSAGEATTRIBUTES_COLUMN + "," + LAST_MODIFIED_COLUMN +") VALUES(?,?,?)";
	private static final String SELECT_QUERY = "SELECT " + MESSAGEATTRIBUTES_COLUMN + " FROM " + TABLE_NAME + " WHERE " + MESSAGEID_COLUMN +"=?";
	private static final String UPDATE_QUERY = "UPDATE " + TABLE_NAME + " SET " + MESSAGEATTRIBUTES_COLUMN +"=?, "+ LAST_MODIFIED_COLUMN + "=?"+ " WHERE " + MESSAGEID_COLUMN + "=?";
	private static final String DELETE_QUERY = "DELETE FROM " + TABLE_NAME + "  WHERE " + LAST_MODIFIED_COLUMN + " < ?";
	private static Integer id = 1;
	private static boolean loggedDBType = false;
	private static Method prepareDirectStatementMethod1 = null;
	private static Method prepareDirectStatementMethod2 = null;
	private static Method getWrappedConnectionMethod = null;
	private static boolean initialized = false;
	private static final String CommonConnectionImpl_Class = "com.sap.sql.jdbc.common.CommonConnectionImpl";
	private static final String CommonConnectionHandle_Class = "com.sap.engine.services.dbpool.cci.CommonConnectionHandle";
	private static final String CommonPooledConnection_Class = "com.sap.sql.jdbc.common.CommonPooledConnection";

	static {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(() -> {purgeOldAttributes();}, HOURS_BETWEEN_PURGES, HOURS_BETWEEN_PURGES, TimeUnit.HOURS);
		executorService.scheduleAtFixedRate(() -> {
			try {
				Connection connection = ConnectionManager.getInstance().getNoTXDBConnection();
				PreparedStatement stmt = getPreparedStatement(connection, "Select count(*) from " + TABLE_NAME);
				ResultSet rs = stmt.executeQuery();
				if(rs.next()) {
					int count = rs.getInt(1);
					NewRelic.recordMetric("SAP/AttributeProcessor/CurrentTableSize",count);
				}
				rs.close();
				connection.close();
			} catch (SQLException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to get table size");
				logErrorWithMessage("Failed to get table size",e);
			}
		}, 20, 60, TimeUnit.SECONDS);
	}
	
	private static synchronized void initialize() {
		if(initialized) return;
		Logger logger = NewRelic.getAgent().getLogger();
		Connection connection = getConnectionToUse();
		if(connection != null) {
			try {
				DatabaseMetaData dbMetaData = connection.getMetaData();
				boolean found = false;
				if(dbMetaData != null) {
					ResultSet tables;
					try {
						tables = dbMetaData.getTables(null, null, TABLE_NAME, null);
						found = false;
						while(tables.next()) {
							String tableName = tables.getString("TABLE_NAME");
						    if (tableName.equalsIgnoreCase(TABLE_NAME)) { // Important for case-insensitive databases
						        found = true;
						        logMessage("Found table " + tableName);
						        break;
						    }
						
						}
						tables.close();
					} catch (SQLException e) {
						logger.log(Level.FINER, e, "Failed while trying to find if table {0} exists",TABLE_NAME);
						logErrorWithMessage("Failed to find table " + TABLE_NAME,e);
					}
					if(!found) {
						logger.log(Level.FINE, "{0} does not exist, creating", TABLE_NAME);
				        logMessage("table " + TABLE_NAME + " does not exist, creating");
						try {
							createTable();
							initialized = true;
						} catch (SQLException e) {
							logger.log(Level.FINER, e, "Failed while trying create table {0}",TABLE_NAME);
							logErrorWithMessage("Failed to create table " + TABLE_NAME,e);
						}
					} else {
						logger.log(Level.FINE, "{0} exists", TABLE_NAME);
				        logMessage("table " + TABLE_NAME + " exists");
						initialized = true;
					}
				}
				connection.close();
			} catch (SQLException e) {
				logger.log(Level.FINER, e, "Failed while trying find or create table {0}",TABLE_NAME);
				logErrorWithMessage("Failed while trying to find or create table " + TABLE_NAME,e);
			}
		}
	}
	
	private static void createTable() throws SQLException {
		String createSQL = "CREATE TABLE NR_ADAPTER_ATTRIBUTES(MESSAGEID VARCHAR(50) NOT NULL PRIMARY KEY, MESSAGE_ATTRIBUTES VARCHAR(500) NOT NULL, LAST_MODIFIED TIMESTAMP NOT NULL)";
		Connection connection = ConnectionManager.getInstance().getNoTXDBConnection();
		Statement stmt = getPreparedStatement(connection, createSQL);
		stmt.execute(createSQL);
		NewRelic.getAgent().getLogger().log(Level.FINE, "Created table {0}",TABLE_NAME);
		connection.close();
	}

	private static void purgeOldAttributes() {
		long threshold_time = System.currentTimeMillis() - THRESHOLD;
		Timestamp timestamp = new Timestamp(threshold_time);
		int removed = 0;
		try {
			Connection connection = ConnectionManager.getInstance().getNoTXDBConnection();
			PreparedStatement deleteStatement = getPreparedStatement(connection, DELETE_QUERY);
			deleteStatement.setTimestamp(1, timestamp);
			
			removed = deleteStatement.executeUpdate();
			NewRelic.getAgent().getLogger().log(Level.FINE,"Call to purge attribute table, purged {0} entries", removed);
			connection.close();
		} catch (SQLException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE,e, "Failed to purge table entries older than {0}",timestamp);
			logErrorWithMessage("Failed to purge table entries older than " + timestamp,e);
		}
		
		if(removed > 0) {
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			attributes.put("NumberOfEntriesPurged", removed);
			NewRelic.getAgent().getInsights().recordCustomEvent("AdapterAttributesPurge", attributes);
		}
	}

	protected static void setAttributes(MessageKey messageKey, Map<String,String> attributes) {
		
		if(!initialized) {
			initialize();
		}

		if(attributes == null || attributes.isEmpty()) return;
		NewRelic.recordMetric("SAP/AttributeProcessor/AttributesToProcess", attributes.size());
		logMessage("Processing attributes for message id "+ messageKey.getMessageId() + ": " + attributes);

		Connection conn = null;
		try {
			conn = ConnectionManager.getInstance().getNoTXDBConnection();
		} catch (SQLException e) {
			logErrorWithMessage("Failed to get a connection from the connection manager", e);
		}
		
		if(conn != null) {
			try {
				PreparedStatement queryStatement = getPreparedStatement(conn,SELECT_QUERY);
				queryStatement.setString(1, messageKey.getMessageId());
				ResultSet existing = queryStatement.executeQuery();
				boolean hasExisting = false;
				String existingAttributes = null;
				while(existing.next()) {
					hasExisting = true;
					existingAttributes = existing.getString(MESSAGEATTRIBUTES_COLUMN);
					logMessage("Retrived existing attributes for message id " +messageKey.getMessageId() + ": "+ existingAttributes);
				}
				existing.close();
				try {
					queryStatement.close();
				} catch (SQLException e) {
					logErrorWithMessage("Failed to close select statement while checking for existing attributes", e);
				}
	
				if(hasExisting) {
					Map<String, String> existingMap = stringToMap(existingAttributes);
					logMessage("Found existing attributes for message id " + messageKey.getMessageId() + ": " + existingMap);
					attributes.putAll(existingMap);
					PreparedStatement updateStatement = getPreparedStatement(conn,UPDATE_QUERY);
					updateStatement.setString(1, attributes.toString());
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					updateStatement.setTimestamp(2, timestamp);
					updateStatement.setString(3, messageKey.getMessageId());
					updateStatement.executeUpdate();
					try {
						updateStatement.close();
					} catch (SQLException e) {
						logErrorWithMessage("Failed to close update statement while updating existing attributes", e);
					}
					logMessage("Updated attributes for message id " + messageKey.getMessageId() + " to: " + attributes);
					NewRelic.recordMetric("SAP/AttributeProcessor/UpdatedAttributes", 1.0f);
	
				} else {
					PreparedStatement insertStatement = getPreparedStatement(conn,INSERT_QUERY);
	
					synchronized (id) {
						insertStatement.setString(1, messageKey.getMessageId());
						insertStatement.setString(2, attributes.toString());
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						insertStatement.setTimestamp(3, timestamp);
						int updated = insertStatement.executeUpdate();
						if(updated > 0) {
							NewRelic.recordMetric("SAP/AttributeProcessor/InsertedAttributes", 1.0f);
							logMessage("Inserted attributes for message id " + messageKey.getMessageId() + " to: " + attributes);
						}
					}
	
					try {
						insertStatement.close();
					} catch (SQLException e) {
						logErrorWithMessage("Failed to close update statement while inserting attributes", e);
					}
				}
				conn.close();
			} catch (SQLException e) {
				logErrorWithMessage("Failed to close update statement while inserting attributes", e);
			}
		}

	}

	public static void record(ModuleContext moduleContext, ModuleData moduleData) {
		if(!AttributeChecker.initialized) {
			AttributeChecker.startChecker();
		}
		AttributeChecker.addDataToQueue(new DataHolder(moduleData, moduleContext));
	}

	public static Map<String,String> getMessageAttributes(MessageKey messageKey) {
		Map<String,String> attributes = new LinkedHashMap<String, String>();
		PreparedStatement pstmt = null;
		try {
			Connection connection = ConnectionManager.getInstance().getNoTXDBConnection();
			pstmt = getPreparedStatement(connection, SELECT_QUERY);
			
			pstmt.setString(1, messageKey.getMessageId());
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				String attrs = rs.getString(MESSAGEATTRIBUTES_COLUMN);
				attributes = stringToMap(attrs);
				logMessage("Retrieved attributes for message id " + messageKey.getMessageId() + ": " + attributes);
			}
			rs.close();
			connection.close();
		} catch (SQLException e) {
			NewRelic.getAgent().getLogger().log(Level.FINEST, e, "Failed to retrive message attributes for {0}", messageKey.getMessageId());
			NewRelic.recordMetric("SAP/AttributeProcessor/AttributesRetrievalFailed", attributes.size());
			logErrorWithMessage("Failed during call to getMessageAttributes for message id " + messageKey.getMessageId(), e);
			
		} finally {
			if(pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					logErrorWithMessage("Failed in getMessageAttributes while trying to close statement " + messageKey.getMessageId(), e);
				}
			}
		}
		
		NewRelic.recordMetric("SAP/AttributeProcessor/AttributesRetrieved", attributes.size());
		return attributes;
	}

	private static Map<String, String> stringToMap(String attrs) {
		Map<String,String> attributes = new LinkedHashMap<String, String>();
		if(attrs != null) {
			attrs = attrs.replace("{","").replace("}", "");
			String[] pairs = attrs.split(",");
			for(String pair : pairs) {
				String[] entry = pair.split("=");
				if(entry.length == 2) {
					attributes.put(entry[0], entry[1]);
				}
			}
		}
		
		return attributes;
	}
	
	private static void setWrappedConnection(Class<?> clazz) {
		try {
			getWrappedConnectionMethod = clazz.getDeclaredMethod("getWrappedConnection", new Class<?>[] {});
		} catch (NoSuchMethodException e) {
			AdapterMonitorLogger.logErrorWithMessage("Failed to find prepareDirectStatement due to NoSuchMethodException", e);
		} catch (SecurityException e) {
			AdapterMonitorLogger.logErrorWithMessage("Failed to find prepareDirectStatement due to SecurityException", e);
		}
	}
	
	private static void setupConnection1(Class<?> clazz) {
		try {
			prepareDirectStatementMethod1 = clazz.getDeclaredMethod("prepareDirectStatement", new Class[] {java.lang.String.class});
		} catch (NoSuchMethodException e) {
			AdapterMonitorLogger.logErrorWithMessage("Failed to find prepareDirectStatement due to NoSuchMethodException", e);
		} catch (SecurityException e) {
			AdapterMonitorLogger.logErrorWithMessage("Failed to find prepareDirectStatement due to SecurityException", e);
		}
	}
	
	private static void setupConnection2(Class<?> clazz) {
		try {
			prepareDirectStatementMethod2 = clazz.getDeclaredMethod("prepareDirectStatement", new Class[] {java.lang.String.class});
		} catch (NoSuchMethodException e) {
			AdapterMonitorLogger.logErrorWithMessage("Failed to find prepareDirectStatement due to NoSuchMethodException", e);
		} catch (SecurityException e) {
			AdapterMonitorLogger.logErrorWithMessage("Failed to find prepareDirectStatement due to SecurityException", e);
		}
	}
	
	private static PreparedStatement getPreparedStatement(Object connection, String sql) {
		Class<?> clazz = connection.getClass();
		if(prepareDirectStatementMethod1 == null) {
				setupConnection1(clazz);
		}
		
		String classname = clazz.getName();
		if(classname.equals(CommonConnectionHandle_Class)) {
			return getPreparedStatement1(connection, sql);
		}
		if(classname.equals(CommonConnectionImpl_Class)) {
			return getPreparedStatement2(connection, sql);
		}
		if(classname.equals(CommonPooledConnection_Class)) {
			return getWrappedPreparedStatement(connection, sql);
		}
		if(connection instanceof Connection) {
			try {
				return ((Connection)connection).prepareStatement(sql);
			} catch (SQLException e) {
				logErrorWithMessage("Failed to get PreparedStatement from connection " + connection, e);
			}
		}
		return null;
	}
	
	private static PreparedStatement getWrappedPreparedStatement(Object connection, String sql) {
		if(getWrappedConnectionMethod == null) {
			setWrappedConnection(connection.getClass());
		}
		try {
			Object wrappedConnection = getWrappedConnectionMethod.invoke(connection, new Object[] {});
			return ((Connection)wrappedConnection).prepareStatement(sql);
		} catch (Exception e) {
			logErrorWithMessage("Failed to get wrapped connection", e);
		}
		return null;
	}
	
	private static PreparedStatement getPreparedStatement1(Object connection, String sql) {
		if(prepareDirectStatementMethod1 == null) {
			setupConnection1(connection.getClass());
		}
		if(connection != null && sql != null && prepareDirectStatementMethod1 != null) {
			try {
				Object obj = prepareDirectStatementMethod1.invoke(connection, new Object[] {sql});
				if(obj != null && obj instanceof PreparedStatement) {
					return (PreparedStatement)obj;
				}
			} catch (Exception e) {
				logErrorWithMessage("Failed to get PreparedStatement", e);
			}
		}
		return null;
	}
	
	private static PreparedStatement getPreparedStatement2(Object connection, String sql) {
		if(prepareDirectStatementMethod2 == null) {
			setupConnection2(connection.getClass());
		}
		if(connection != null && sql != null && prepareDirectStatementMethod2 != null) {
			try {
				Object obj = prepareDirectStatementMethod2.invoke(connection, new Object[] {sql});
				if(obj != null && obj instanceof PreparedStatement) {
					return (PreparedStatement)obj;
				}
			} catch (Exception e) {
				logErrorWithMessage("Failed to get PreparedStatement", e);
			}
		}
		return null;
	}
	
	
	private static Connection getConnectionToUse() {
		try {
			ConnectionManager connectionMgr = ConnectionManager.getInstance();
			logMessage("ConnectionManager type: " + connectionMgr.getClass().getName());
			Connection connectionFromMgr = connectionMgr.getNoTXDBConnection();
			if (connectionFromMgr != null) {
				DatabaseMetaData metaData = connectionFromMgr.getMetaData();
				Map<String, Object> metaDataMap = new HashMap<String, Object>();
				if (!loggedDBType) {
					metaDataMap.put("Parent-Database-Product", metaData.getDatabaseProductName());
					metaDataMap.put("Parent-Database-Product-Version", metaData.getDatabaseProductVersion());
					metaDataMap.put("Parent-Database-Driver", metaData.getDriverName());
					metaDataMap.put("Parent-Database-Driver-Version", metaData.getDriverVersion());
				}
				Class<?> connectionClass = connectionFromMgr.getClass();
				String classname = connectionClass.getName();
				logMessage("Connection from ConnectionManager: " + classname);
				
				if (classname.equalsIgnoreCase("com.sap.engine.services.dbpool.cci.ConnectionHandle")
						|| classname.equalsIgnoreCase(CommonConnectionHandle_Class)
						|| classname.equalsIgnoreCase(CommonConnectionImpl_Class)) {
					try {
						Method getPhysicalConnection = connectionClass.getMethod("getPhysicalConnection",new Class<?>[0]);
						Object physicalConn = getPhysicalConnection.invoke(connectionFromMgr, new Object[0]);
						logMessage("Got PhysicalConnection: " + physicalConn);
						if (physicalConn != null) {
							Class<?> physicalConnClass = physicalConn.getClass();
							String physicalConnectionType = physicalConnClass.getName();
							if (physicalConnectionType.equalsIgnoreCase(CommonConnectionImpl_Class)) {
								try {
									Method getExtendedPooledConnectionMethod = physicalConnClass.getMethod("getExtendedPooledConnection", new Class[0]);
									Object extendedConn = getExtendedPooledConnectionMethod.invoke(physicalConn,new Object[0]);
									logMessage("Got ExtendedConnection: " + extendedConn);
									if (extendedConn != null) {
										Class<?> extendedConnectionClasss = extendedConn.getClass();
										Method getWrappedConnectionMethod = extendedConnectionClasss.getMethod("getWrappedConnection", new Class<?>[0]);
										Object wrappedCon = getWrappedConnectionMethod.invoke(extendedConn,new Object[0]);
										if (wrappedCon != null && wrappedCon instanceof Connection) {
											Connection wrappedConnection = (Connection)wrappedCon;
											if (!loggedDBType) {
												metaData = wrappedConnection.getMetaData();
												metaDataMap.put("Wrapped-Database-Product", metaData.getDatabaseProductName());
												metaDataMap.put("Wrapped-Database-Product-Version", metaData.getDatabaseProductVersion());
												metaDataMap.put("Wrapped-Database-Driver", metaData.getDriverName());
												metaDataMap.put("Wrapped-Database-Driver-Version", metaData.getDriverVersion());
												NewRelic.getAgent().getInsights().recordCustomEvent("SAP_Adapter_DBConnection", metaDataMap);
												loggedDBType = true;
											}
											logMessage("Connection to use set to " + wrappedConnection);
											return wrappedConnection;
										}
									}
								} catch (Exception e) {
									NewRelic.getAgent().getLogger().log(Level.FINE, e,"Failed to get connection to use from CommonConnectionImpl");
									logErrorWithMessage("Failed to get connection to use from CommonConnectionImpl", e);
								}
							} else {
								
							}
						}
					} catch (Exception e) {
						NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to get connection");
						logErrorWithMessage("Failed to get connection", e);
					}
				} else {
					return connectionFromMgr;
				} 
			}
			
			
		} catch (SQLException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE,e, "AttributeProcessor failed to aquire connection");
			logErrorWithMessage("AttributeProcessor failed to aquire connection", e);
		}		
		return null;
	}
}
