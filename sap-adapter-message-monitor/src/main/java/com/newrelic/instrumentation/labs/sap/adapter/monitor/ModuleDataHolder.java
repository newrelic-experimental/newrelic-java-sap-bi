package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;

public class ModuleDataHolder implements DataHolder {

	protected ModuleData moduleData;
	protected ModuleContext moduleContext;
	
	public ModuleDataHolder(ModuleData moduleData, ModuleContext moduleContext) {
		super();
		this.moduleData = moduleData;
		this.moduleContext = moduleContext;
	}

	public ModuleData getModuleData() {
		return moduleData;
	}

	public ModuleContext getModuleContext() {
		return moduleContext;
	}
	
	
}
