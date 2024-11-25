package com.newrelic.instrumentation.labs.sap.adapters.ejb.attrributeservice;

import java.util.HashSet;
import java.util.Set;

public class AttributeConfiguration {

	private static boolean collectContextChannelId = true;
	private static Set<String> contextAttributes = new HashSet<String>();
	private static boolean supplementalEnabled = false;
	private static Set<String> supplementalAttributes = new HashSet<String>();
	private static boolean principalEnabled = false;
	private static Set<String> principalAttributes = new HashSet<String>();
	
	public static boolean isCollectContextChannelId() {
		return collectContextChannelId;
	}
	
	/*
	 * called when reseting the attributes to collect
	 */
	public static void clearAttributes() {
		contextAttributes.clear();
		supplementalAttributes.clear();
		principalAttributes.clear();
	}
	
	public static void addContextAttribute(String attribute) {
		contextAttributes.add(attribute);
	}
	
	public static void addSupplementalAttribute(String attribute) {
		supplementalAttributes.add(attribute);
	}
	
	public static void addPrincipalAttribute(String attribute) {
		principalAttributes.add(attribute);
	}
	
	public static void setCollectContextChannelId(boolean collectContextChannelId) {
		AttributeConfiguration.collectContextChannelId = collectContextChannelId;
	}


	public static void setSupplementalEnabled(boolean supplementalEnabled) {
		AttributeConfiguration.supplementalEnabled = supplementalEnabled;
	}


	public static void setPrincipalEnabled(boolean principalEnabled) {
		AttributeConfiguration.principalEnabled = principalEnabled;
	}


	public static Set<String> getContextAttributes() {
		return contextAttributes;
	}
	public static boolean isSupplementalEnabled() {
		return supplementalEnabled;
	}
	public static Set<String> getSupplementalAttributes() {
		return supplementalAttributes;
	}
	public static boolean isPrincipalEnabled() {
		return principalEnabled;
	}
	public static Set<String> getPrincipalAttributes() {
		return principalAttributes;
	}
	
	
	
}
