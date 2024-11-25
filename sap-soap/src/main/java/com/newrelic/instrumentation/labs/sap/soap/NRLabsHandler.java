package com.newrelic.instrumentation.labs.sap.soap;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class NRLabsHandler extends Handler {

	private File currentLog;
	private PrintWriter logWriter;
	private String logFileName;

	public NRLabsHandler(String filename) throws IOException {
		logFileName = filename;
		currentLog = new File(logFileName);
		logWriter = new PrintWriter(currentLog);
	}

	@Override
	public void publish(LogRecord record) {
		if(!isLoggable(record)) return;

		if(logWriter == null) return;

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

}
