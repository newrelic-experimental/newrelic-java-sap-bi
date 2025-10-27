package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import com.sap.aii.af.lib.mp.module.ModuleData;

public class Utils {

	public static ThreadLocal<ModuleData> currentModuleData = new ThreadLocal<ModuleData>() {

		@Override
		protected ModuleData initialValue() {
			
			return null;
		}
		
	};

}
