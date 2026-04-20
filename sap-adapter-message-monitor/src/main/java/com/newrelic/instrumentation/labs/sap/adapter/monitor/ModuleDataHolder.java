package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.concurrent.atomic.AtomicInteger;

import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;

public class ModuleDataHolder implements DataHolder {

	protected ModuleData moduleData;
	protected ModuleContext moduleContext;
	protected AtomicInteger invocations = new AtomicInteger(0);
	private static final int MAX_RETRIES = 5;

	// Timestamp when message was captured (for enforcing minimum delay)
	private final long captureTimestamp;

	public ModuleDataHolder(ModuleData moduleData, ModuleContext moduleContext) {
		super();
		this.moduleData = moduleData;
		this.moduleContext = moduleContext;
		this.captureTimestamp = System.currentTimeMillis();
	}

	public ModuleData getModuleData() {
		return moduleData;
	}

	public ModuleContext getModuleContext() {
		return moduleContext;
	}

	public boolean retry() {
		int count = invocations.incrementAndGet();
		return count < MAX_RETRIES;
	}

	/**
	 * Returns timestamp (in milliseconds) when this message was captured
	 */
	public long getCaptureTimestamp() {
		return captureTimestamp;
	}

	/**
	 * Returns elapsed time (in milliseconds) since message was captured
	 */
	public long getElapsedMillis() {
		return System.currentTimeMillis() - captureTimestamp;
	}

	/**
	 * Returns true if message has been in queue for at least the specified delay
	 */
	public boolean hasWaitedAtLeast(long delayMillis) {
		return getElapsedMillis() >= delayMillis;
	}

}
