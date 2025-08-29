package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.util.logging.Level;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.api.agent.Logger;
import com.newrelic.api.agent.NewRelic;

public class CreateTable {

	
	public static boolean createTable(Connection connection) {
		
		Logger logger = NewRelic.getAgent().getLogger();
		try {
			File jar1 = new File("/usr/sap/PD1/SUM/sdt/lib/sdt_db.jar");
			File jar2 = new File("/usr/sap/PD1/SUM/sdt/lib/sdt_dmt.jar");
			File jar3 = new File("/usr/sap/PD1/SUM/sdt/lib/sdt_dmt_ddic.jar");
			File jar4 = new File("/usr/sap/PD1/SUM/sdt/lib/sdt_trace.jar");
			File jar5 = new File("/usr/sap/PD1/SUM/sdt/lib/sdt_dmt.jar");
			File jar6 = new File("/usr/sap/PD1/SUM/sdt/lib/sdt_dmt710.jar");
			File jar7 = new File("/usr/sap/PD1/SUM/sdt/lib/sap.com~tc~dd~db~dictionarydatabase~implDictionaryDatabase.jar");
			
			URL[] urls = new URL[7];
			urls[0] = jar1.toURI().toURL();
			urls[1] = jar2.toURI().toURL();
			urls[2] = jar3.toURI().toURL();
			urls[3] = jar4.toURI().toURL();
			urls[4] = jar5.toURI().toURL();
			urls[5] = jar6.toURI().toURL();
			urls[6] = jar7.toURI().toURL();
			
			URLClassLoader cl = new URLClassLoader(urls);
			
			Class<?> clazz = Class.forName("com.sap.dictionary.database.friendtools.RuntimeTableFriendTools", false, cl);
			logger.log(Level.FINE, "Loaded FriendTools class: {0}", clazz);
			
			Constructor<?> constructor = clazz.getConstructor(Connection.class);
			logger.log(Level.FINE, "Found FriendTools constructor: {0}", constructor);
			Object toolsObj = constructor.newInstance(connection);
			logger.log(Level.FINE, "Found FriendTools object: {0}", toolsObj);
			
			Method createTableMethod = clazz.getDeclaredMethod("createTable", String.class, File.class);
			logger.log(Level.FINE, "Found FriendTools createTable method: {0}", createTableMethod);
			File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
			File input = new File(newRelicDir,"attributes_table.xml");
			logger.log(Level.FINE, "Found file: {0}, exists {1}", input, input != null ? input.exists() : false);
			logger.log(Level.FINE, "Calling createTable using {0} and inputs {1}, {2}", toolsObj, AttributeProcessor.TABLE_NAME, input);
			boolean result = (boolean)createTableMethod.invoke(toolsObj, AttributeProcessor.TABLE_NAME,input);
			logger.log(Level.FINE, "createTable returned {0}", result);
			return result;
 			
		} catch (IOException e) {
			logger.log(Level.FINE,e, "Failed in CreateTable.createTable due to IOException");
		} catch (ClassNotFoundException e) {
			logger.log(Level.FINE,e, "Failed in CreateTable.createTable due due to ClassNotFoundException");
		} catch (Exception e) {
			logger.log(Level.FINE,e, "Failed in CreateTable.createTable due due to Exception");
		}
		
		
		return false;
	}
}
