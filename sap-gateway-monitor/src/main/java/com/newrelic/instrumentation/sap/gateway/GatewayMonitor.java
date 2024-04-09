package com.newrelic.instrumentation.sap.gateway;

import java.util.List;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.sap.igw.ejb.composite.MPLHeaderUI;
import com.sap.it.op.mpl.db.DatabaseMessageAccess;

public class GatewayMonitor {
	
//	private DatabaseMessageAccess dbAccess = null;
	
	private static GatewayMonitor instance = null;
	private ClassLoader loader = null;
	private Object dbAccess= null;
	
	
	public static void initialize(ClassLoader cl) {
		if(instance == null) {
			instance = new GatewayMonitor(cl);
		}
	}
	
	public static GatewayMonitor getInstance() {
		return instance;
	}

	private GatewayMonitor(ClassLoader cl) {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Call to GatewayMonitor.<init>({0})", cl);
		loader = cl;
		
	}
	
	private GatewayMonitor(Object instance) {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Call to GatewayMonitor.<init>({0})", instance);
		Class<?> clazz = instance.getClass();
		NewRelic.getAgent().getLogger().log(Level.FINE, "Instance class is {0}", clazz);
		try {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Attempting to load DatabaseMessageAccess");
			Class<?> c = Class.forName("com.sap.it.op.mpl.db.DatabaseMessageAccess", true, clazz.getClassLoader());
			NewRelic.getAgent().getLogger().log(Level.FINE, "loaded class {0}", c);
			dbAccess = (DatabaseMessageAccess) c.newInstance();
			NewRelic.getAgent().getLogger().log(Level.FINE, "created an instance of DatabaseMessageAccess {0}", dbAccess);
//			if(instance instanceof DatabaseMessageAccess) {
//				dbAccess = (DatabaseMessageAccess)instance;
//				NewRelic.getAgent().getLogger().log(Level.FINE, "created an instance of DatabaseMessageAccess {0}", dbAccess);
//			} else {
//				NewRelic.getAgent().getLogger().log(Level.FINE, "Object is not an instance of DatabaseMessageAccess");
//			}
		} catch (Exception e) {
			NewRelic.getAgent().getLogger().log(Level.FINE,e, "Could not load DatabaseMessageAccess class ");
		}
	}
	
	private void setDbAccess () {
		try {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Attempting to load DatabaseMessageAccess");
			Class<?> c = Class.forName("com.sap.it.op.mpl.db.DatabaseMessageAccess", true, loader);
			NewRelic.getAgent().getLogger().log(Level.FINE, "loaded class {0}", c);
			dbAccess =  c.newInstance();
			NewRelic.getAgent().getLogger().log(Level.FINE, "created an instance of DatabaseMessageAccess {0}", dbAccess);
		} catch (Exception e) {
			NewRelic.getAgent().getLogger().log(Level.FINE,e, "Failed to create an instance of DatabaseMessageAccess");
		}
		
	}
	
	public void report() {
		String message = "Unable to get message";
		try {
			setDbAccess();
			if(dbAccess != null) {
				
			}
			List<MPLHeaderUI> headers = dbAccess.retrieveAllHeaders(1000);
			if(headers != null) {
				message = "Retrieved " + headers.size() + " headers from DatabaseMessageAccess";
			} {
				message = "Call to DatabaseMessageAccess returned null list";
			}
		} catch (Exception e) {
			message = "Call to DatabaseMessageAccess caused an error";
			NewRelic.getAgent().getLogger().log(Level.FINER, e, message);
		}
		
		GatewayLogger.logMessage(message);
	}
}
