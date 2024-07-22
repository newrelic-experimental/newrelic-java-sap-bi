package com.newrelic.instrumentation.labs.sap.channel.monitoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.newrelic.api.agent.NewRelic;

public class NRLabsHandler extends Handler {

	private int maxFiles;
	private long maxSize;
	private long rolloverTime;
	private int index = 0;
	private File currentLog;
	private PrintWriter logWriter;
	private String logFileName;
	private long logCreated;

	public NRLabsHandler(String filename, int rolltime, long size, int max) throws IOException {
		logFileName = filename;
		if(max < 1) max = 1;
		maxFiles = max;
		if(size <= 0) {
			maxSize = 1024L*1024L;
		} else {
			maxSize = size;
		}
		if(rolltime <= 0) {
			rolloverTime = 0;
		} else {
			rolloverTime = rolltime * 60000L;
		}
		currentLog = new File(logFileName);
		logWriter = new PrintWriter(currentLog);
		logCreated = System.currentTimeMillis();
	}

	@Override
	public void publish(LogRecord record) {
		if(!isLoggable(record)) return;

		if(logWriter == null) return;

		boolean rolled = false;
		long logSize = currentLog.length();
		if(logSize > maxSize) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Rolling log {0} because current size {1} exceeds max file {2}", logFileName, logSize, maxSize);
			rollLog();
			rolled = true;
		}

		long timeExisted = System.currentTimeMillis() - logCreated;

		if(!rolled && rolloverTime != 0 && (timeExisted > rolloverTime)) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Rolling log {0} because file existence {1} exceeds max time {2}", logFileName, timeExisted, rolloverTime);
			rollLog();
		}

		String msg = record.getMessage();
		logWriter.println(msg);
		logWriter.flush();
	}

	@Override
	public void flush() {
		logWriter.flush();
	}

	@Override
	public void close() throws SecurityException {
		logWriter.close();
	}

	private void rollLog() {
		logWriter.close();

		int fileEnding = index + 1;
		if(fileEnding >= maxFiles) {
			fileEnding = 1;
		}
		if (maxFiles > 1) {
			File oldLog = new File(logFileName + "." + fileEnding);
			if (oldLog.exists()) {
				oldLog.delete();
			}
			currentLog.renameTo(oldLog);
			currentLog = new File(logFileName);
			try {
				logWriter = new PrintWriter(currentLog);
			} catch (FileNotFoundException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to open log file");
			} 
			index++;
			if(index == maxFiles) index = 0;
		} else {
			currentLog.delete();
			currentLog = new File(logFileName);
			try {
				logWriter = new PrintWriter(currentLog);
			} catch (FileNotFoundException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to open log file");
			} 
		}
		logCreated = System.currentTimeMillis();
	}
}
