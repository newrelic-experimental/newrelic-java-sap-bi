package com.newrelic.instrumentation.labs.sap.mpl;

import java.util.TimerTask;

public class NRLabsTimerTask extends TimerTask {

	@Override
	public void run() {
		GatewayLogger.checkConfig();
	}

}
