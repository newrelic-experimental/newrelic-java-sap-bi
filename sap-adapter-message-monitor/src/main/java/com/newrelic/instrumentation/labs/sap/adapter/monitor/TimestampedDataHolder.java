package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper for DataHolder that adds delay enforcement for DelayQueue.
 * Messages cannot be retrieved from DelayQueue until the configured delay expires.
 *
 * This ensures every message waits at least the configured polling interval
 * before being processed, allowing SAP to complete message processing
 * and return final status (DLVD) instead of intermediate status (DLNG).
 */
public class TimestampedDataHolder implements Delayed {

	private final DataHolder delegate;
	private final long captureTimeMillis;
	private final long delayMillis;
	private final long readyTimeMillis;

	/**
	 * Create a timestamped holder with specified delay
	 *
	 * @param delegate The actual DataHolder to wrap
	 * @param delayMillis Minimum delay in milliseconds before this can be retrieved
	 */
	public TimestampedDataHolder(DataHolder delegate, long delayMillis) {
		this.delegate = delegate;
		this.delayMillis = delayMillis;
		this.captureTimeMillis = System.currentTimeMillis();
		this.readyTimeMillis = captureTimeMillis + delayMillis;
	}

	/**
	 * Returns the remaining delay in the given time unit.
	 * Negative value means delay has expired and message is ready.
	 */
	@Override
	public long getDelay(TimeUnit unit) {
		long remainingMillis = readyTimeMillis - System.currentTimeMillis();
		return unit.convert(remainingMillis, TimeUnit.MILLISECONDS);
	}

	/**
	 * Compare delays for priority queue ordering.
	 * Messages with shorter remaining delay have higher priority.
	 */
	@Override
	public int compareTo(Delayed other) {
		return Long.compare(
			this.getDelay(TimeUnit.MILLISECONDS),
			other.getDelay(TimeUnit.MILLISECONDS)
		);
	}

	/**
	 * Get the wrapped DataHolder
	 */
	public DataHolder getDelegate() {
		return delegate;
	}

	/**
	 * Get capture timestamp
	 */
	public long getCaptureTimeMillis() {
		return captureTimeMillis;
	}

	/**
	 * Get configured delay
	 */
	public long getDelayMillis() {
		return delayMillis;
	}

	/**
	 * Get time when message becomes ready
	 */
	public long getReadyTimeMillis() {
		return readyTimeMillis;
	}

	/**
	 * Check if delay has expired
	 */
	public boolean isReady() {
		return System.currentTimeMillis() >= readyTimeMillis;
	}

	/**
	 * Get elapsed time since capture
	 */
	public long getElapsedMillis() {
		return System.currentTimeMillis() - captureTimeMillis;
	}

	@Override
	public String toString() {
		long remaining = getDelay(TimeUnit.MILLISECONDS);
		return "TimestampedDataHolder[delegate=" + delegate +
			", elapsed=" + getElapsedMillis() + "ms" +
			", remaining=" + (remaining > 0 ? remaining + "ms" : "READY") + "]";
	}
}
