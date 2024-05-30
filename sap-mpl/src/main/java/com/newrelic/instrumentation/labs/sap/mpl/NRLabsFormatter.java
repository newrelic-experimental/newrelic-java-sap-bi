package com.newrelic.instrumentation.labs.sap.mpl;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class NRLabsFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		return record.getMessage() + "\n";
	}

}