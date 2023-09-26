package com.nr.instrumentation.sap.auditlogging;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;

public class SimulateTimerTask extends TimerTask {
	
	private Future<?> future;
	private Timer timer;
	
	public SimulateTimerTask( Future<?> t, Timer tim) {
		future = t;
		timer = tim;
	}

	@Override
	public void run() {
		if(future != null && !future.isCancelled() && !future.isDone()) {
			future.cancel(true);
			timer.cancel();
			NewRelic.getAgent().getLogger().log(Level.FINE, "Forced simulate task to stop after 10 seconds");
		}
	}

}
