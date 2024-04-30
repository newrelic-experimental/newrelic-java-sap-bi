package com.newrelic.instrumentation.labs.sap.dbtech;

import java.net.InetSocketAddress;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newrelic.agent.bridge.datastore.DatabaseVendor;
import com.newrelic.agent.bridge.datastore.DatastoreInstanceDetection;
import com.newrelic.agent.bridge.datastore.JdbcHelper;
import com.newrelic.agent.database.DatabaseService;
import com.newrelic.agent.database.DefaultDatabaseStatementParser;
import com.newrelic.agent.database.ParsedDatabaseStatement;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.DatastoreParameters;
import com.newrelic.api.agent.DatastoreParameters.CollectionParameter;
import com.newrelic.api.agent.DatastoreParameters.DatabaseParameter;
import com.newrelic.api.agent.DatastoreParameters.InstanceParameter;
import com.newrelic.api.agent.DatastoreParameters.SlowQueryParameter;
import com.sap.dbtech.jdbc.ConnectionSapDB;

public class Utils {

	public static boolean initialized = false;
	private static DatabaseVendor vendor;
	private static final String SAPVENDOR_PREFIX = "jdbc:sapdb";
	private static final String SAPVENDOR = "SAP DB";
	private static DefaultDatabaseStatementParser parser = null;
	private static final Map<ConnectionSapDB, DBInfo> infos = new HashMap<>();

	public static void init() {
		DatabaseService dbService = ServiceFactory.getDatabaseService();
		if(dbService != null) {
			parser = (DefaultDatabaseStatementParser)dbService.getDatabaseStatementParser();
			initialized  = true;
		}
		vendor = new SAPDBVendor();
	}
	
	private static String getSAPHost(String url) {
		if(url.startsWith(SAPVENDOR_PREFIX)) {
			int index = url.indexOf("//:");
			
			String temp = index > -1 ? url.substring(index+3) : url;
			index = temp.indexOf(':');
			int index2 = temp.indexOf('/');
			if(index2 > -1) {
				return temp.substring(0,index2);
			} else {
				return temp;
			}
			
		}
		return null;
	}

	public static DBInfo getDBInfo(ConnectionSapDB connection) {

		if(infos.containsKey(connection)) {
			return infos.get(connection);
		}

		InetSocketAddress address = DatastoreInstanceDetection.getAddressForConnection(connection);

		Properties connectProps = connection.getConnectProperties();
		DatabaseMetaData metaData = null;
		try {
			metaData = connection.getMetaData();
		} catch(Exception e) {
			
		}
		String host = null;
		int port = -1;
		if(address != null) {
			host = address.getHostName();
			port = address.getPort();
		}

		String url = null;
		String dbName = null;
		String vendorName = null;

		if(connectProps != null) {

			if(connectProps.containsKey("dbname")) {
				dbName = connectProps.getProperty("dbname");
			}

			if(connectProps.containsKey("dburl")) {
				url = connectProps.getProperty("dburl");
			}
		}

		if(metaData != null) {
			if(url == null) {
				try {
					url = metaData.getURL();
				} catch (SQLException e) {
				}
			}
			try {
				String temp = metaData.getDatabaseProductName().toLowerCase();
				if(temp.equals(SAPVENDOR) || temp.contains("sap")) {
					vendorName = "SAP";
					if(url != null && !url.isEmpty()) {
						String hoststring = getSAPHost(url);
						if(hoststring != null) {
							int index = hoststring.indexOf(':');
							if(index > -1) {
								host = hoststring.substring(0, index);
								String portStr = hoststring.substring(index+1);
								try {
									port = Integer.parseInt(portStr);
								} catch (NumberFormatException e) {
								}
							} else {
								host = hoststring;
							}
						}
					}
				} else {
					if (url != null) {
						DatabaseVendor v = JdbcHelper.getVendor(null, url);
						if (v != null) {
							vendorName = v.getName();
						} else {
							vendorName = "JDBC";
						} 
					} else {
						vendorName = "JDBC";
					}
				}
			} catch (SQLException e) {
				vendorName = "JDBC";
			}

			if(url == null) {
				try {
					url = metaData.getURL();
				} catch (SQLException e) {
				}
			}
		}

		DBInfo info = new DBInfo();
		if(host != null) {
			info.setHost(host);
		}
		if(port > -1) {
			info.setPort(port);
		}
		if(dbName != null) {
			info.setDBName(dbName);
		}
		
		if(vendorName == null || vendorName.isEmpty()) {
			if(url != null && url.startsWith(SAPVENDOR_PREFIX)) {
				vendorName = "SAP";
			}
		}

		if(vendorName == null || vendorName.isEmpty()) {
			vendorName = "JDBC";
		}

		info.setVendor(vendorName);
		infos.put(connection, info);

		return info;
	}

	public static ParsedDatabaseStatement parseSQL(String sql) {
		if(!initialized) {
			init();
		}

		if(initialized) {
			
			if (vendor != null && sql != null) {
				ParsedDatabaseStatement statement = parser.getParsedDatabaseStatement(vendor, sql, null);
				return statement;
			}
		}

		return null;
	}
	public static DatastoreParameters getDBParameters(ParsedDatabaseStatement stmt, DBInfo info, String sql) {
		DatastoreParameters params = null;
		if(stmt != null) {
			String vendorName = info.getVendor();
			if(vendorName == null || vendorName.isEmpty()) {
				vendorName = "JDBC";
			}
			CollectionParameter cparam = DatastoreParameters.product(vendorName);
			String collection = stmt.getModel();
			String operation = stmt.getOperation();

			if(collection != null && operation != null) {
				InstanceParameter iparam = cparam.collection(collection).operation(operation);
				String host = info.getHost();
				int p = info.getPort();
				Integer port = null;
				if(p > 0) port = p;
				String dbname = info.getDBName();

				if(host != null && port != null && !host.isEmpty()) {
					DatabaseParameter dbParm = port > 0 ? iparam.instance(host, port) : iparam.instance(host, (Integer)null);
					if(dbname != null && !dbname.isEmpty()) {
						if(sql == null || sql.isEmpty()) {
							params = dbParm.databaseName(dbname).build();
						} else {
							SlowQueryParameter slowQuery = dbParm.databaseName(dbname);
							params = slowQuery.slowQuery(sql, new SAPSlowQueryConverter()).build();
						}


					} else {
						if(sql != null && !sql.isEmpty()) {
							SlowQueryParameter slowQuery = dbParm.noDatabaseName();
							params = slowQuery.slowQuery(sql, new SAPSlowQueryConverter()).build();
						} else {
							params = dbParm.build();
						}
					}
				} else {
					params = iparam.build();
				}
			}
		}
		return params;
	}

	public static class DBInfo {

		private String host = null;
		private int port;
		private String dbName = null;
		private String vendor = null;

		public void setDBName(String db) {
			dbName = db;
		}

		public String getDBName() {
			return dbName;
		}

		public String getHost() {
			return host;
		}

		public String getVendor() {
			return vendor;
		}

		public void setVendor(String vendor) {
			this.vendor = vendor;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public int getPort() {
			return port;
		}

		@Override
		public String toString() {
			return "host: " + host + ", port: " + port + ", DB Name: "+dbName + ", Vendor: "+vendor;
		}

	}



}
