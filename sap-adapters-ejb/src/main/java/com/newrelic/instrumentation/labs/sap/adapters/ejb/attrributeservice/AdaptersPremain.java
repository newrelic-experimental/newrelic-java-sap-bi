package com.newrelic.instrumentation.labs.sap.adapters.ejb.attrributeservice;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.instrument.Instrumentation;

import com.newrelic.agent.config.ConfigFileHelper;

public class AdaptersPremain {

	
	public static void premain(String s, Instrumentation inst) {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		File logDirectory = new File(newRelicDir, "attributeLogging");
		if(logDirectory.exists() && logDirectory.isDirectory()) {
			// Clear the attributes logs since this is a fresh start
			File[] logs = logDirectory.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".log");
				}
			});
			for(File log : logs) {
				log.delete();
			}
		}
	}
}
