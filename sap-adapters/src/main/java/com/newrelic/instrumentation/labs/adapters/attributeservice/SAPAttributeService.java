package com.newrelic.instrumentation.labs.adapters.attributeservice;

import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.logging.Level;

import com.github.cliftonlabs.json_simple.JsonException;
import com.newrelic.api.agent.NewRelic;

public class SAPAttributeService {
	
	public static final SAPAttributeService INSTANCE = new SAPAttributeService();
	private static final long ONE_MINUTE = 60L * 1000L;

	private SAPAttributeService() {
		
	}
	
	Timer timer = null;
	
	public void start() {
		timer = new Timer("Adapters-Config", true);
		ConfigurationListener task = ConfigurationListener.getListener();
		try {
			task.loadConfig();
		} catch (FileNotFoundException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to find attribute config file");
		} catch (JsonException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to parse JSON from attribute config file");
		}
		timer.scheduleAtFixedRate(task, ONE_MINUTE, ONE_MINUTE);
		
	}
	
	public void stop() {
		if(timer != null) {
			timer.cancel();
		}
	}
}
