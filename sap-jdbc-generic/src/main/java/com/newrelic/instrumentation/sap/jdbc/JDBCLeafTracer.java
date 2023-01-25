package com.newrelic.instrumentation.sap.jdbc;

import com.newrelic.agent.Transaction;
import com.newrelic.agent.tracers.ClassMethodSignature;
import com.newrelic.agent.tracers.DefaultTracer;

public class JDBCLeafTracer extends DefaultTracer {

	public JDBCLeafTracer(Transaction transaction, ClassMethodSignature sig, Object object) {
		super(transaction, sig, object);
		// TODO Auto-generated constructor stub
	}

}
