package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class NRLabsLogFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		Throwable t = record.getThrown();
		if(t != null) {
			StringWriter sw = new StringWriter();
			PrintWriter writer = new PrintWriter(sw);
			t.printStackTrace(writer);
			String exceptionMsg = sw.toString();
			return record.getMessage() + "\n" + exceptionMsg;			
			
		}
		return record.getMessage() + "\n";
	}

}
