package com.newrelic.instrumentation.labs.sap.adapter.data;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.service.db.ConnectionManager;

public class DictionaryUtils {

	public static boolean DICTIONARY_DBTOOLS_DEFINED = false;
	private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private static ScheduledFuture<?> future = null;
	public static boolean initialized = false;
	private static final String TABLE_NAME = "NR_MESSAGE_ATTRIBUTES";
	private static final String CommonConnectionImpl_Class = "com.sap.sql.jdbc.common.CommonConnectionImpl";
	private static final String CommonConnectionHandle_Class = "com.sap.engine.services.dbpool.cci.CommonConnectionHandle";
	private static final String CommonPooledConnection_Class = "com.sap.sql.jdbc.common.CommonPooledConnection";

	static {
		future = executor.scheduleAtFixedRate(() -> {
			if(initialized) {
				try {
					runCheck();
				} catch (SQLException e) {
					NewRelic.getAgent().getLogger().log(Level.FINE, e, "Call to runcheck failed");
				}
			}
		}, 1, 1, TimeUnit.MINUTES);

	}

	public static  void initialize() {
		initialized = true;
	}
	private static void runCheck() throws SQLException {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Call to runCheck");

		Connection connection = ConnectionManager.getInstance().getNoTXDBConnection();
		

		NewRelic.getAgent().getLogger().log(Level.FINE, "In Call to runCheck, using connection {0}", connection);
		Class<?> connectionClass = connection.getClass();
		String classname = connectionClass.getName();
		
		
		if (classname.equalsIgnoreCase("com.sap.engine.services.dbpool.cci.ConnectionHandle")
				|| classname.equalsIgnoreCase(CommonConnectionHandle_Class)
				|| classname.equalsIgnoreCase(CommonConnectionImpl_Class)) {
			try {
				Connection connectionToUse = null;
				Method getPhysicalConnection = connectionClass.getMethod("getPhysicalConnection",new Class<?>[0]);
				Object physicalConn = getPhysicalConnection.invoke(connection, new Object[0]);

				if (physicalConn != null) {
					NewRelic.getAgent().getLogger().log(Level.FINE, "In call to runCheck, physicalConnection={0}",physicalConn);
					Class<?> clazz = physicalConn.getClass();
					String physicalConnectionType = physicalConn.getClass().getName();
					if (physicalConnectionType.equalsIgnoreCase("com.sap.sql.jdbc.common.CommonConnectionImpl")) {
						try {
							Method getExtendedPooledConnectionMethod = clazz.getMethod("getExtendedPooledConnection",new Class[0]);
							Object extendedConn = getExtendedPooledConnectionMethod.invoke(physicalConn, new Object[0]);
							if (extendedConn != null) {
								Class<?> clazz2 = extendedConn.getClass();
								Method getWrappedConnectionMethod = clazz2.getMethod("getWrappedConnection",new Class<?>[0]);
								Object wrappedCon = getWrappedConnectionMethod.invoke(extendedConn, new Object[0]);
								if (wrappedCon != null && wrappedCon instanceof Connection) {
									connectionToUse = (Connection) wrappedCon;
								}
							}
						} catch (Exception e) {
							NewRelic.getAgent().getLogger().log(Level.FINE, e,"Failed to get connection to use from CommonConnectionImpl");
						}
					}
					if (connectionToUse == null) {
						connectionToUse = (Connection) physicalConn;
					}
					try {
						String dropTable = "DROP TABLE NR_ADAPTER_ATTRIBUTES";
						
//						String createSQL = "CREATE TABLE NR_ADAPTER_ATTRIBUTES(MESSAGEID VARCHAR(50) NOT NULL UNIQUE, MESSAGE_ATTRIBUTES VARCHAR(500) NOT NULL)";
						Statement stmt = connectionToUse.createStatement();
						try {
							stmt.execute(dropTable);
							NewRelic.getAgent().getLogger().log(Level.FINE, "Dropped table {0}",TABLE_NAME);
						} catch (Exception e) {
							NewRelic.getAgent().getLogger().log(Level.FINE, e, "Call to drop table failed");
						}
						
//						stmt.execute(createSQL);
//						NewRelic.getAgent().getLogger().log(Level.FINE, "Created table {0}",TABLE_NAME);
//						getTableInfo(connectionToUse);
						stmt.close();
						future.cancel(false);
						return;
					} catch (SQLException e) {
						NewRelic.getAgent().getLogger().log(Level.FINE, e, "Call to create table failed");
					} 
				}

			} catch(Exception e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Call to drop table failed");
			}
		}
	}
	
	private static void getTableInfo(Connection connection) {
		try {
			Statement stmt = connection.createStatement();
			try {
				int count = stmt.executeUpdate("INSERT INTO " + TABLE_NAME +"(MESSAGEID,MESSAGE_ATTRIBUTES) VALUES('xxx','attributes')");
				NewRelic.getAgent().getLogger().log(Level.FINE, "Inserted {0} rows in getTableInfo", count);
			} catch (SQLException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e,"Failed to insert row into {0} due to error", TABLE_NAME);
			}
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM " + TABLE_NAME);
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			NewRelic.getAgent().getLogger().log(Level.FINE, "{0} has {1} columns", TABLE_NAME, columnCount);
			for(int i=1; i<=columnCount;i++) {
				String columnName = metaData.getColumnName(i);
				String columnType = metaData.getColumnClassName(i);
				NewRelic.getAgent().getLogger().log(Level.FINE, "\tColumn Name: {0}, Type {1}", columnName,columnType);
			}
			stmt.close();
		} catch (SQLException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e,"Failed to get table information");
		}
	}
}
