package com.newrelic.instrumentation.labs.sap.tracemessage.processing;

import java.util.TimerTask;

public class NRLabsTimerTask extends TimerTask {

	@Override
	public void run() {
		TraceMessageLogger.checkConfig();
	}

}
