package com.sap.dbtech.jdbcext;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import com.newrelic.agent.bridge.datastore.DatabaseVendor;
import com.newrelic.agent.bridge.datastore.DatastoreInstanceDetection;
import com.newrelic.agent.bridge.datastore.JdbcDataSourceConnectionFactory;
import com.newrelic.agent.bridge.datastore.JdbcHelper;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.BaseClass)
public abstract class DataSourceSapDBBase {

	public abstract int getPort();
	public abstract String getServerName();
	public abstract String getDatabaseName();
	public abstract String getURL();
	
	public Connection openPhysicalConnection() {
		Connection connection = Weaver.callOriginal();
		
		
		InetSocketAddress socket = DatastoreInstanceDetection.getAddressForConnection(connection);
		if(socket == null) {
			socket = new InetSocketAddress(getServerName(), getPort());
			DatastoreInstanceDetection.detectConnectionAddress();
			DatastoreInstanceDetection.associateAddress(connection, socket);
			DatastoreInstanceDetection.stopDetectingConnectionAddress();
		}
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
		
		return connection;
	}
	
	public Connection openPhysicalConnection(String username, String password) {
		Connection connection = Weaver.callOriginal();
		InetSocketAddress socket = DatastoreInstanceDetection.getAddressForConnection(connection);
		if(socket == null) {
			socket = new InetSocketAddress(getServerName(), getPort());
			DatastoreInstanceDetection.detectConnectionAddress();
			DatastoreInstanceDetection.associateAddress(connection, socket);
			DatastoreInstanceDetection.stopDetectingConnectionAddress();
		}
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
		
		return connection;
	}
	
	public Connection openPhysicalConnection(Properties p) {
		Connection connection = Weaver.callOriginal();
		InetSocketAddress socket = DatastoreInstanceDetection.getAddressForConnection(connection);
		if(socket == null) {
			socket = new InetSocketAddress(getServerName(), getPort());
			DatastoreInstanceDetection.detectConnectionAddress();
			DatastoreInstanceDetection.associateAddress(connection, socket);
			DatastoreInstanceDetection.stopDetectingConnectionAddress();
		}
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
		
		return connection;
	}
}
