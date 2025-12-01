package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.lib.mp.module.ModuleData;

import java.io.File;
import java.util.logging.Level;

public class Utils {

	public static ThreadLocal<ModuleData> currentModuleData = new ThreadLocal<ModuleData>() {

		@Override
		protected ModuleData initialValue() {
			
			return null;
		}
		
	};

	public static long getSize(String rolloverSize) {
		if (rolloverSize == null || rolloverSize.isEmpty()) {
			return 10 * 1024L *  1024L;
		}
		StringBuilder sb = new StringBuilder();
		int length = rolloverSize.length();
		for(int i = 0; i < length; i++) {
			char c = rolloverSize.charAt(i);
			if(Character.isDigit(c)) {
				sb.append(c);
			}
		}

		long size = Long.parseLong(sb.toString());
		char end = rolloverSize.charAt(length-1);
		switch (end) {
			case 'K':
				size *= 1024L;
				break;
			case 'M':
				size *= 1024L*1024L;
				break;
			case 'G':
				size *= 1024L*1024L*1024L;
				break;
		}

		// disallow less than 10K
		if(size < 10 * 1024L) {
			size =  10 * 1024L;
		}

		return size;
	}

	public static void createDirectoriesIfNotExists(File file) {
		File parent = file.getParentFile();
		if(parent != null && !parent.exists()) {
			boolean result = parent.mkdirs();
			if(result) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Created parent directory of {0}", file.getAbsolutePath());
			}
		}
	}

}
