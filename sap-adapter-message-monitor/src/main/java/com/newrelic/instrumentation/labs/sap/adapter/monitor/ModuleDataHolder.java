package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.concurrent.atomic.AtomicInteger;

import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;

public class ModuleDataHolder implements DataHolder {

	protected ModuleData moduleData;
	protected ModuleContext moduleContext;
	protected AtomicInteger invocations = new AtomicInteger(0);
	private static final int MAX_RETRIES = 5;
	
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

	@Override
	public boolean retry() {
		int count = invocations.incrementAndGet();
		return count < MAX_RETRIES;
	}

	
}
