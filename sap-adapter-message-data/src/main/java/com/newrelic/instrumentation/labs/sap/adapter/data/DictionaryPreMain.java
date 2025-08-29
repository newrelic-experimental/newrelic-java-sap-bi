package com.newrelic.instrumentation.labs.sap.adapter.data;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;

import com.newrelic.api.agent.Logger;
import com.newrelic.api.agent.NewRelic;

public class DictionaryPreMain {

	public static void premain(String s, Instrumentation inst) {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Call to DictionaryPreMain.premain");
		load();
	}
	
	private static void load() {
		Logger logger = NewRelic.getAgent().getLogger();
		DictionaryUtils.initialize();
		try {
			File jar1 = new File("/usr/sap/PD1/SUM/sdt/lib/sdt_db.jar");
			File jar2 = new File("/usr/sap/PD1/SUM/sdt/lib/sdt_dmt.jar");
			File jar3 = new File("/usr/sap/PD1/SUM/sdt/lib/sdt_dmt_ddic.jar");
			File jar4 = new File("/usr/sap/PD1/SUM/sdt/lib/sdt_trace.jar");
			File jar5 = new File("/usr/sap/PD1/SUM/sdt/lib/sdt_dmt.jar");
			File jar6 = new File("/usr/sap/PD1/SUM/sdt/lib/sdt_dmt710.jar");
			File jar7 = new File("/usr/sap/PD1/SUM/sdt/lib/sap.com~tc~dd~db~dictionarydatabase~implDictionaryDatabase.jar");
			File jar8 = new File("/sapdb/usrsapPD1/J00/j2ee/cluster/bin/services/com.sap.aii.af.svc/lib/com.sap.aii.af.svc_api.jar");
			File jar9 = new File("/usr/sap/PD1/J00/j2ee/cluster/bin/ext/com.sap.aii.af.lib/lib/com.sap.aii.af.lib.mod.jar");
			File jar10 = new File("/usr/sap/PD1/SUM/sdt/lib/645/logging.jar");
			
			URL[] urls = new URL[10];
			urls[0] = jar1.toURI().toURL();
			urls[1] = jar2.toURI().toURL();
			urls[2] = jar3.toURI().toURL();
			urls[3] = jar4.toURI().toURL();
			urls[4] = jar5.toURI().toURL();
			urls[5] = jar6.toURI().toURL();
			urls[6] = jar7.toURI().toURL();
			urls[7] = jar8.toURI().toURL();
			urls[8] = jar9.toURI().toURL();
			urls[9] = jar10.toURI().toURL();
			
			URLClassLoader cl = new URLClassLoader(urls);
			
			Class<?> clazz = Class.forName("com.sap.dictionary.database.friendtools.RuntimeTableFriendTools", true, cl);
			logger.log(Level.FINE, "Loaded FriendTools class: {0}", clazz);
//			DictionaryUtils.setDBToolsClass(clazz);
			
			Class<?> clazz2 = Class.forName("com.sap.aii.af.service.db.ConnectionManager",true,cl);
			logger.log(Level.FINE, "Loaded ConnectionManager class: {0}", clazz2);
//			DictionaryUtils.setConnMgrClass(clazz2);

//			Connection connection = null;
//			
//			Method getInstanceMethod = clazz2.getMethod("getInstance", new Class<?>[0]);
//			logger.log(Level.FINE, "Loaded getInstance method: {0}", getInstanceMethod);
//			
//			Object connMgrObj = getInstanceMethod.invoke(null, new Object[0]);
//			logger.log(Level.FINE, "getInstance result: {0}", connMgrObj);
//
//			Method getDBConnectionMethod = clazz2.getDeclaredMethod("getDBConnection", new Class<?>[0]);
//			logger.log(Level.FINE, "Loaded getDBConnection method: {0}", getDBConnectionMethod);
//			
//			Object connObj = getDBConnectionMethod.invoke(connMgrObj, new Object[0]);
//			
//			if(connObj != null && connMgrObj instanceof Connection) {
//				connection = (Connection)connObj;
//			}
//			
//			
//			logger.log(Level.FINE, "Got connection  {0}", connection);
//			File nrDir = ConfigFileHelper.getNewRelicDirectory();
//			File input = new File(nrDir, "attributes_table.xml");
//			logger.log(Level.FINE, "Got file input  {0}", input);
//			
//			RuntimeTableFriendTools dbTools = new RuntimeTableFriendTools(connection);
//			logger.log(Level.FINE, "Got file dbTools  {0}", dbTools);
//			boolean create = dbTools.createTable("NR_NewrelicAttributes", input);
//			logger.log(Level.FINE, "Result of createTable is {0}", create);
//
			
 			
		} catch (IOException e) {
			logger.log(Level.FINE,e, "Failed in DictionaryPreMain due to IOException");
		} catch (ClassNotFoundException e) {
			logger.log(Level.FINE,e, "Failed in DictionaryPreMain due to ClassNotFoundException");
		} catch (Exception e) {
			logger.log(Level.FINE,e, "Failed in DictionaryPreMain due to Exception");
		}
	}
}
