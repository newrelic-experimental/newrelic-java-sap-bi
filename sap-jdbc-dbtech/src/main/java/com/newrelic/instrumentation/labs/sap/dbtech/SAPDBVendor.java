package com.newrelic.instrumentation.labs.sap.dbtech;

import com.newrelic.agent.bridge.datastore.DatastoreVendor;
import com.newrelic.agent.bridge.datastore.JdbcDatabaseVendor;

public class SAPDBVendor extends JdbcDatabaseVendor {

	public SAPDBVendor() {
		super("SAP", DatastoreVendor.JDBC.name(), false);
	}

	@Override
	public DatastoreVendor getDatastoreVendor() {
		return DatastoreVendor.JDBC;
	}

	

}
