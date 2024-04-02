package com.newrelic.instrumentation.sap.gateway;

//import java.util.Random;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;

public class Simulate implements Runnable {

	//	private static int messageId = 10000;
	//	private static Random random = new Random();
	protected boolean simulate = false;
	static private Object dbAccess;
	//private static DatabaseMessageAccess dbAccess = null;
	private static ClassLoader clLoader = null;
	String dbAccessName = "com.sap.it.op.mpl.db.DatabaseMessageAccess";

	public static void initialize(ClassLoader loader) {
		clLoader = loader;//new DatabaseMessageAccess();
		GatewayMonitor.initialize(loader);
	}

	//	private static long randomDuration() {
	//		long r = Math.abs(random.nextLong() % 25000) + 5000;
	//		return r;
	//	}
	//	
	//	private static String getCurrentMessage() {
	//		StringBuffer sb = new StringBuffer();
	//		int currentId = messageId < 1000000 ? messageId++ : 1000000;
	//		
	//		sb.append("Message Id: " + Integer.toString(currentId) + ", ");
	//		sb.append("Processed At: ");
	//		long now = System.currentTimeMillis();
	//		long duration = randomDuration();
	//		Date startDate = new Date(now - duration);
	//		sb.append(startDate);
	//		sb.append(", ");
	//		sb.append("Processing Finished At: ");
	//		Date endDate = new Date(now);
	//		sb.append(endDate);
	//		sb.append(", ");
	//		int i = random.nextInt(20);
	//		String flow = "MyIntegrationFlow-" + i;
	//		sb.append("Integration Flow: " + flow + ", ");
	//		
	//		sb.append("Duration: ");
	//		sb.append(duration);
	//		
	//		String message = sb.toString();
	//		return message;
	//	}

	@Override
	public void run() {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Call to Gateway Simulate");
		if(simulate) {
			try {
//				Class<?> dbAccessClass = Class.forName(dbAccessName, true, clLoader);
//				dbAccess = dbAccessClass.newInstance();
//				if(dbAccessClass != null && dbAccess != null) {
//					NewRelic.getAgent().getLogger().log(Level.FINE, "Got DatabaseMessageAccess instance and calling GatewayMonitor");
					GatewayMonitor monitor = GatewayMonitor.getInstance();
					if(monitor != null) {
						
					monitor.report();
					}
//				} else {
//					GatewayLogger.logMessage("DatabaseMessageAccess is not available, dbAccessClass = "+dbAccessClass+", dbAccess = "+dbAccess);
//				}
			} catch (Exception e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Error invoking DatabaseMessageAccess");
				GatewayLogger.logMessage("Failed to get headers from DatabaseMessageAccess due to error: " + e.getMessage());
			}
		}
	}

}
