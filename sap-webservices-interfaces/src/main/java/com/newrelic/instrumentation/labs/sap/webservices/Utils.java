package com.newrelic.instrumentation.labs.sap.webservices;

import java.util.HashMap;
import java.util.Map;

import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;

public class Utils {

	public static void addValue(Map<String, Object> attributes, String key, Object value) {
		if(key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}
	
	public static void populateWithProviderContextHelper(HashMap<String, Object> attributes, ProviderContextHelper ctxHelper) {
		addValue(attributes,"Path", ctxHelper.getPath());
		addValue(attributes,"SessionId", ctxHelper.getSessionID());
		addValue(attributes,"Name", ctxHelper.getName());
		try {
			OperationMapping operation = ctxHelper.getOperation();
			if(operation != null) {
				addValue(attributes,"Operation-HTTPLocation", operation.getHTTPLocation());
				addValue(attributes,"Operation-JavaMethodName", operation.getJavaMethodName());
				addValue(attributes,"Operation-WSDLOperationName", operation.getWSDLOperationName());
			}
		} catch (RuntimeProcessException e) {
		}

	}
}
