package com.sap.dbtech.jdbc;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

import com.newrelic.agent.bridge.datastore.DatastoreInstanceDetection;
import com.newrelic.agent.bridge.datastore.JdbcDriverConnectionFactory;
import com.newrelic.agent.bridge.datastore.JdbcHelper;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.sap.dbtech.SAPDBVendor;
import com.sap.dbtech.jdbc.trace.TraceControl;
import com.sap.dbtech.rte.comm.BasicSocketComm;
import com.sap.dbtech.rte.comm.CommUtils;
import com.sap.dbtech.rte.comm.CommUtils.DBInfo;
import com.sap.dbtech.rte.comm.JdbcCommunication;

@Weave
public abstract class DriverSapDB {
	
	@NewField
	private InetSocketAddress address = null;
	
	@NewField
	private String dbName = null;

    public Connection connect(String url, Properties props) {
            Connection connection = Weaver.callOriginal(); 
            InetSocketAddress socket = DatastoreInstanceDetection.getAddressForConnection(connection);
            
            if(socket == null && address != null) {
            	DatastoreInstanceDetection.detectConnectionAddress();
            	DatastoreInstanceDetection.associateAddress(connection, address);
            	DatastoreInstanceDetection.stopDetectingConnectionAddress();
            	address = null;
            }
            if(dbName != null) {
            	JdbcHelper.putDatabaseName(url, dbName);
            	dbName = null;
            }
            if (!JdbcHelper.connectionFactoryExists(connection)) {
            	String detectedUrl = JdbcHelper.getConnectionURL(connection);
                if (detectedUrl == null) {
                    return connection;
                }
            	JdbcHelper.putConnectionFactory(detectedUrl, new JdbcDriverConnectionFactory( new SAPDBVendor(), (Driver) this,
                        url, props));
            }
            return connection;
    }
    
    @SuppressWarnings("unused")
	private final JdbcCommunication openByURL(String url, Properties info, TraceControl mytrc) {
    	JdbcCommunication comm = Weaver.callOriginal();
    	if(comm instanceof BasicSocketComm) {
    		BasicSocketComm bComm = (BasicSocketComm)comm;
    		DBInfo dbInfo = CommUtils.getSocketInfo(bComm);
    		String host = dbInfo.getHost();
    		dbName = dbInfo.getDbName();
    		int port = dbInfo.getPort();
    		address = new InetSocketAddress(host,port);
    		
    	}
    	
    	
    	return comm;
    }

}
