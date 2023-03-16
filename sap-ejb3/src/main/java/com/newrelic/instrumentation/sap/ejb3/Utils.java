package com.newrelic.instrumentation.sap.ejb3;

import java.util.Map;

import com.sap.engine.services.ejb3.model.BeanIdentity;
import com.sap.engine.services.ejb3.runtime.InstanceIdentity;

public class Utils {

	
	public static void addAttribute(Map<String, Object> attributes, String key, Object value) {
		String tmp;
		if(key != null && !key.startsWith("SAP")) {
			tmp = "SAP-"+key;
		} else {
			tmp = key;
		}
		if(attributes != null && key != null && !key.isEmpty() && value != null) {
			attributes.put(tmp, value);
		}
	}
	
	public static void addInstanceIdentity(Map<String, Object> attributes, InstanceIdentity id) {
		BeanIdentity beanId = id.getBeanIdentity();
		addAttribute(attributes, "ApplicationName", beanId.getApplicationName());
		addAttribute(attributes, "ModuleName", beanId.getModuleName());
		addAttribute(attributes, "BeanName", beanId.getBeanName());
		addAttribute(attributes, "ElementUniqueName", beanId.getElementUniqueName());
	}
}
