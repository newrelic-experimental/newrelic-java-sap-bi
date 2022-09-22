package com.nr.instrumentation.sap.adaptermonitoring;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;

/**
 * Used to wrap a Runnable so we can link what it does back to the original transaction
 * 
 * @author dhilpipre
 *
 */
public class NRRunnable implements Runnable {
	
	private Runnable delegate = null;
	private Token token = null;
	private static boolean isTransformed = false;
	
	public NRRunnable(Runnable r, Token t) {
		delegate = r;
		token = t;
		if(!isTransformed) {
			AgentBridge.instrumentation.retransformUninstrumentedClass(getClass());
			isTransformed = true;
		}
	}

	@Override
	@Trace(async=true)
	public void run() {
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		if(delegate != null) {
			delegate.run();
		}

	}

}
