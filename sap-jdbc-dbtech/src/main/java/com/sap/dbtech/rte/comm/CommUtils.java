package com.sap.dbtech.rte.comm;

public class CommUtils {
	
	public static DBInfo getSocketInfo(BasicSocketComm comm) {
		DBInfo info = new DBInfo();
		info.setDbName(comm.dbname);
		info.setHost(comm.host);
		info.setPort(comm.port);
		return info;
	}

	public static class DBInfo {
		private String host = null;
		private int port;
		private String dbName = null;
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
		public String getDbName() {
			return dbName;
		}
		public void setDbName(String dbName) {
			this.dbName = dbName;
		}
		
		
	}
}
