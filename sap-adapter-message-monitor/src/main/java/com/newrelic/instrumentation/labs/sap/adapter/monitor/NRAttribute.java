package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import com.sap.aii.af.search.api.Attribute;

public class NRAttribute implements Attribute {
	
	private static final long serialVersionUID = 1L;
	private String name = null;
	private String value = null;
	
	public NRAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

}
