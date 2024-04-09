package com.newrelic.instrumentation.sap.gateway;

import java.util.Date;
import java.util.Random;

public class Simulate implements Runnable {
	
	private static int messageId = 10000;
	private static Random random = new Random();
	protected boolean simulate = false;
	
	private static long randomDuration() {
		long r = Math.abs(random.nextLong() % 25000) + 5000;
		return r;
	}
	
	private static String getCurrentMessage() {
		StringBuffer sb = new StringBuffer();
		int currentId = messageId < 1000000 ? messageId++ : 1000000;
		
		sb.append("Message Id: " + Integer.toString(currentId) + ", ");
		sb.append("Processed At: ");
		long now = System.currentTimeMillis();
		long duration = randomDuration();
		Date startDate = new Date(now - duration);
		sb.append(startDate);
		sb.append(", ");
		sb.append("Processing Finished At: ");
		Date endDate = new Date(now);
		sb.append(endDate);
		sb.append(", ");
		int i = random.nextInt(20);
		String flow = "MyIntegrationFlow-" + i;
		sb.append("Integration Flow: " + flow + ", ");
		
		sb.append("Duration: ");
		sb.append(duration);
		
		String message = sb.toString();
		return message;
	}

	@Override
	public void run() {
		if(simulate) {
			GatewayLogger.logMessage(getCurrentMessage());
		}
	}

}
