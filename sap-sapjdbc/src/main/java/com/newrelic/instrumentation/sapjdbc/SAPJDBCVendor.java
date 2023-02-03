package com.newrelic.instrumentation.sapjdbc;

import com.newrelic.agent.bridge.datastore.DatastoreVendor;
import com.newrelic.agent.bridge.datastore.JdbcDatabaseVendor;

public class SAPJDBCVendor extends JdbcDatabaseVendor {
	
	public static SAPJDBCVendor INSTANCE = new SAPJDBCVendor();
	
	private SAPJDBCVendor() {
		super("SAP","sap",false);
	}

	@Override
	public DatastoreVendor getDatastoreVendor() {
		return DatastoreVendor.JDBC;
	}

}
