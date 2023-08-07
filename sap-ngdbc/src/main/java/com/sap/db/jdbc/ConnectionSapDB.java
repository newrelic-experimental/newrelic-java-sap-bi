package com.sap.db.jdbc;

import java.net.InetSocketAddress;
import java.util.Properties;

import com.newrelic.agent.bridge.datastore.DatastoreInstanceDetection;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.sap.db.jdbc.topology.Location;
import com.sap.db.jdbc.trace.TraceControl;
import com.sap.db.rte.comm.JdbcCommunication;

@Weave(type = MatchType.BaseClass)
public abstract class ConnectionSapDB {

	
	public ConnectionSapDB(JdbcCommunication session, Properties info, TraceControl mytrc)  {
		if(session != null) {
			Location location = session.getLocation();
			if(location != null) {
				String host = location.getHostName();
				int port = location.getPortNumber();
				InetSocketAddress iAddress = new InetSocketAddress(host, port);
				DatastoreInstanceDetection.associateAddress(this, iAddress);
			}
		}
		
	}
}
