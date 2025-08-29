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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.newrelic.api.agent.Logger;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.adapter.xi.ms.XIMessage;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.sdk.xi.mo.MessageContext;
import com.sap.aii.af.service.db.ConnectionManager;
import com.sap.engine.interfaces.messaging.api.MessageKey;

public class AttributeProcessor {

	protected static Map<String, Map<String,String>> message_Attributes = new HashMap<String, Map<String,String>>(10);
	private static final long THRESHOLD = 86400 * 1000L;
	private static final int HOURS_BETWEEN_PURGES = 4;
	private static final String MESSAGEID_COLUMN = "MESSAGEID";
	private static final String MESSAGEATTRIBUTES_COLUMN = "MESSAGE_ATTRIBUTES";
	private static final String ATTRIBUTE_NAME_COLUMN = "ATTRIBUTE_NAME";
	private static final String ATTRIBUTE_VALUE_COLUMN = "ATTRIBUTE_VALUE";
	
	private static final String LAST_MODIFIED_COLUMN = "LAST_MODIFIED";
	
	protected static final String TABLE_NAME = "NR_ADAPTER_ATTRIBUTES";
	private static final String INSERT_QUERY = "INSERT INTO " + TABLE_NAME + "(" + MESSAGEID_COLUMN +"," + ATTRIBUTE_NAME_COLUMN + "," + ATTRIBUTE_VALUE_COLUMN + "," + LAST_MODIFIED_COLUMN +") VALUES(?,?,?,?)";
	private static final String SELECT_QUERY = "SELECT " + ATTRIBUTE_NAME_COLUMN + "," + ATTRIBUTE_VALUE_COLUMN + " FROM " + TABLE_NAME + " WHERE " + MESSAGEID_COLUMN +"=?";
	private static final String UPDATE_QUERY = "UPDATE " + TABLE_NAME + " SET " + ATTRIBUTE_VALUE_COLUMN +"=?, "+ LAST_MODIFIED_COLUMN + "=?"+ " WHERE " + MESSAGEID_COLUMN + "=? AND " + ATTRIBUTE_NAME_COLUMN + "=?";
	private static final String DELETE_QUERY = "DELETE FROM " + TABLE_NAME + "  WHERE " + LAST_MODIFIED_COLUMN + " < ?";
	private static final String CREATE_SQL = "CREATE TABLE " + TABLE_NAME + "(" + MESSAGEID_COLUMN + " VARCHAR(255) NOT NULL, " + ATTRIBUTE_NAME_COLUMN 
			+ " VARCHAR(255) NOT NULL, " + ATTRIBUTE_VALUE_COLUMN +  " VARCHAR(255) NOT NULL, " + LAST_MODIFIED_COLUMN + " TIMESTAMP NOT NULL)";
	private static final String DROP_TABLE_SQL = "DROP TABLE " + TABLE_NAME;
	private static Integer id = 1;
	private static boolean loggedDBType = false;
	private static Method prepareDirectStatementMethod1 = null;
	private static Method prepareDirectStatementMethod2 = null;
	private static Method getWrappedConnectionMethod = null;
	private static boolean initialized = false;
	private static final String CommonConnectionImpl_Class = "com.sap.sql.jdbc.common.CommonConnectionImpl";
	private static final String CommonConnectionHandle_Class = "com.sap.engine.services.dbpool.cci.CommonConnectionHandle";
	private static final String CommonPooledConnection_Class = "com.sap.sql.jdbc.common.CommonPooledConnection";
	public static final String MESSAGEID = "MessageId";
	public static final String INPUT = "Input";
	public static final String OUTPUT = "Output";
	public static final String MESSAGE = "Message";
	
	public static final String MAP_TYPE = "MapType";
	
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
		}, 180, 60, TimeUnit.SECONDS);
	}
	
	private static synchronized void initialize() {
		if(initialized) return;
		Logger logger = NewRelic.getAgent().getLogger();
		Connection connection = getConnectionToUse();
		if(connection != null) {
			try {
				DatabaseMetaData dbMetaData = connection.getMetaData();
				boolean found = false;
				boolean recreateTable = false;
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
						        ResultSet columns = dbMetaData.getColumns(null, null, TABLE_NAME, null);
						        Set<String> columnNames = new HashSet<String>();
						        
						        while(columns.next()) {
						        	String columnName = columns.getString("COLUMN_NAME");
						        	columnNames.add(columnName.toLowerCase());
						        }
						        boolean hasAttributeName = columnNames.contains(ATTRIBUTE_NAME_COLUMN.toLowerCase());
						        boolean hasAttributeValue = columnNames.contains(ATTRIBUTE_VALUE_COLUMN.toLowerCase());
						        boolean hasMessageAttributes = columnNames.contains(MESSAGEATTRIBUTES_COLUMN.toLowerCase());
						        recreateTable = hasMessageAttributes || !hasAttributeName || !hasAttributeValue;
						        if(recreateTable) {
						        	logMessage("Found table " + tableName + " and need to recreate");
						        } else {
						        	logMessage("Found table " + tableName + " and do not need to recreate");
						        }
						        
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
							createTable(connection);
							initialized = true;
						} catch (SQLException e) {
							logger.log(Level.FINER, e, "Failed while trying create table {0}",TABLE_NAME);
							logErrorWithMessage("Failed to create table " + TABLE_NAME,e);
						}
					} else if(recreateTable) {
						try {
							dropTable(connection);
							createTable(connection);
							initialized = true;
						} catch (Exception e) {
							logErrorWithMessage("Failed to recreate table " + TABLE_NAME,e);
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
	
	private static void createTable(Connection connection) throws SQLException {
		Statement stmt = connection.createStatement();
		stmt.executeUpdate(CREATE_SQL);
		NewRelic.getAgent().getLogger().log(Level.FINE, "Created table {0}",TABLE_NAME);
	}

	private static void dropTable(Connection connection) throws SQLException {
		Statement stmt = connection.createStatement();
		stmt.executeUpdate(DROP_TABLE_SQL);
		NewRelic.getAgent().getLogger().log(Level.FINE, "Dropped table {0}",TABLE_NAME);
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

	protected static synchronized void setAttributes(MessageKey messageKey, Map<String,String> attributes) {
		long startTime = System.nanoTime();
		
		if(!initialized) {
			initialize();
		}

		if(attributes == null || attributes.isEmpty()) return;
		NewRelic.recordMetric("SAP/AttributeProcessor/AttributesToProcess", attributes.size());
		logMessage("Checking to insert 0r update attributes for message id "+ messageKey.getMessageId() + ": " + attributes);

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
				Map<String, String> existingAttributes = new HashMap<String, String>();
				while(existing.next()) {
					hasExisting = true;
					String name = existing.getString(ATTRIBUTE_NAME_COLUMN);
					String value = existing.getString(ATTRIBUTE_VALUE_COLUMN);
					existingAttributes.put(name, value);
				}
				logMessage("Retrived existing attributes for message id " +messageKey.getMessageId() + ": "+ existingAttributes);
				existing.close();
				try {
					queryStatement.close();
				} catch (SQLException e) {
					logErrorWithMessage("Failed to close select statement while checking for existing attributes", e);
				}
	
				if(hasExisting) {
					logMessage("Found existing attributes for message id " + messageKey.getMessageId() + ": " + existingAttributes);
					PreparedStatement updateStatement = getPreparedStatement(conn,UPDATE_QUERY);
					PreparedStatement insertStatement = getPreparedStatement(conn,INSERT_QUERY);
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					boolean modified = false;
					
					for(String attribute : attributes.keySet()) {
						if(existingAttributes.containsKey(attribute)) {
							String value = existingAttributes.get(attribute);
							String inValue = attributes.get(attribute);
							// Only update if value has changed
							if(!value.toLowerCase().equals(inValue.toLowerCase())) {
								updateStatement.clearParameters();
								updateStatement.setString(1, inValue);
								updateStatement.setTimestamp(2, timestamp);
								updateStatement.setString(3, messageKey.getMessageId());
								updateStatement.setString(4, attribute);
								updateStatement.executeUpdate();
								modified = true;
							}
						} else {
							// new attribute 
							String value = attributes.get(attribute);
							insertStatement.clearParameters();
							insertStatement.setString(1, messageKey.getMessageId());
							insertStatement.setString(2, attribute);
							insertStatement.setString(3, value);
							insertStatement.setTimestamp(4, timestamp);
							insertStatement.executeUpdate();
							modified = true;
						}
					}
					try {
						updateStatement.close();
						
					} catch (SQLException e) {
						logErrorWithMessage("Failed to close update statement while updating existing attributes", e);
					}
					try {
						insertStatement.close();
						
					} catch (SQLException e) {
						logErrorWithMessage("Failed to close update statement while updating existing attributes", e);
					}
					if (modified) {
						Map<String, String> all = new HashMap<String, String>();
						all.putAll(attributes);
						all.putAll(existingAttributes);
						logMessage("Updated attributes for message id " + messageKey.getMessageId() + " to: " + all);
						NewRelic.recordMetric("SAP/AttributeProcessor/UpdatedAttributes", 1.0f);
					} else {
						logMessage("Attributes for message id " + messageKey.getMessageId() + " were identical to existing");
					}
	
				} else {
					PreparedStatement insertStatement = getPreparedStatement(conn,INSERT_QUERY);
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());

					synchronized (id) {
						for(String attribute : attributes.keySet()) {
							String value = attributes.get(attribute);
							insertStatement.clearParameters();
							insertStatement.setString(1, messageKey.getMessageId());
							insertStatement.setString(2, attribute);
							insertStatement.setString(3, value);
							insertStatement.setTimestamp(4, timestamp);
							insertStatement.executeUpdate();
						}
						logMessage("Inserted attributes for message id " + messageKey.getMessageId() + " to: " + attributes);
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
		long endTime = System.nanoTime();
		NewRelic.recordMetric("SAP/AttributeProcessor/SetAttributesTimer(ms)", (endTime-startTime)/1000000.0f);
	}
	
	@SuppressWarnings("rawtypes")
	public static void recordObject(Object requestMessage) {
		
		if(requestMessage instanceof XIMessage) {
			XIMessage xiMessage = (XIMessage)requestMessage;
			MessageKey msgKey = xiMessage.getMessageKey();
			MessageContext msgCtx = xiMessage.getMessageContext();
			Hashtable<String, String> in_attributes = new Hashtable<String, String>();
			
			Enumeration inkeys = msgCtx.getAttributeKeys(0);
			while(inkeys.hasMoreElements()) {
				String key = inkeys.nextElement().toString();
				Object value = msgCtx.getAttribute(key, 0);
				in_attributes.put(key, value.toString());
			}
			if(!in_attributes.isEmpty()) {
				in_attributes.put(AttributeProcessor.MAP_TYPE, AttributeProcessor.INPUT);
				AttributeChecker.addDataToQueue(new MapDataHolder(msgKey, in_attributes));
			}
			Enumeration outkeys = msgCtx.getAttributeKeys(1);
			Hashtable<String, String> out_attributes = new Hashtable<String, String>();
			while(outkeys.hasMoreElements()) {
				String key = outkeys.nextElement().toString();
				Object value = msgCtx.getAttribute(key, 1);
				out_attributes.put(key, value.toString());
			}
			if(!out_attributes.isEmpty()) {
				out_attributes.put(AttributeProcessor.MAP_TYPE, AttributeProcessor.OUTPUT);
				AttributeChecker.addDataToQueue(new MapDataHolder(msgKey, out_attributes));
			}
			
		}
	}
	
	public static void record(ModuleContext moduleContext, ModuleData moduleData) {
		if(!AttributeChecker.initialized) {
			AttributeChecker.startChecker();
		}
		AttributeChecker.addDataToQueue(new ModuleDataHolder(moduleData, moduleContext));
	}

	public static Map<String,String> getMessageAttributes(MessageKey messageKey) {
		Map<String,String> attributes = new LinkedHashMap<String, String>();
		PreparedStatement pstmt = null;
		try {
			Connection connection = ConnectionManager.getInstance().getNoTXDBConnection();
			pstmt = getPreparedStatement(connection, SELECT_QUERY);
			
			pstmt.setString(1, messageKey.getMessageId());
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				String name = rs.getString(ATTRIBUTE_NAME_COLUMN);
				String value = rs.getString(ATTRIBUTE_VALUE_COLUMN);
				attributes.put(name, value);
				
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
