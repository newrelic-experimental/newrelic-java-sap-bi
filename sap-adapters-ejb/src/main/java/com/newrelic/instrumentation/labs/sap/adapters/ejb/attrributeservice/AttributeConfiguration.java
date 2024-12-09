package com.newrelic.instrumentation.labs.sap.adapters.ejb.attrributeservice;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;
import com.sap.engine.interfaces.messaging.api.MessagePropertyKey;

public class AttributeConfiguration {

	private static boolean collectContextChannelId = true;
	private static Set<String> contextAttributes = new HashSet<String>();
	private static boolean supplementalEnabled = false;
	private static Set<String> supplementalAttributes = new HashSet<String>();
	private static boolean principalEnabled = false;
	private static Set<String> principalAttributes = new HashSet<String>();
	private static boolean principalDefaultsEnabled = true;
	private static Map<String, List<String>> payloadAttributesToCapture = new HashMap<String, List<String>>();
	private static Set<MessagePropertyKey> messagePropsToCapture = new HashSet<MessagePropertyKey>();
	
	
	public static boolean isPrincipalDefaultsEnabled() {
		return principalDefaultsEnabled;
	}
	
	public static void setPrincipalDefaultsEnabled(boolean b) {
		principalDefaultsEnabled = b;
	}
	
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
		messagePropsToCapture.clear();
		payloadAttributesToCapture.clear();
	}
	
	public static void addMessagePropertyKey(String ns, String name) {
		MessagePropertyKey key = new MessagePropertyKey(name, ns);
		NewRelic.getAgent().getLogger().log(Level.FINE, "Will capture MessageKey {0}", key);
		
		messagePropsToCapture.add(key);
	}
	
	public static Set<MessagePropertyKey> getMessagePropertiesToCapture() {
		return messagePropsToCapture;
	}
	
	public static void addAttributeKey(String name, String key) {
		List<String> current = payloadAttributesToCapture.get(name);
		if(current == null) {
			current = new ArrayList<String>();
		}
		NewRelic.getAgent().getLogger().log(Level.FINE, "Current list of attributes for payload {0} is {1}", name, current);
		current.add(key);
		payloadAttributesToCapture.put(name, current);
		NewRelic.getAgent().getLogger().log(Level.FINE, "Will capture attribute {0} from payloads with the name {1}", key,name);
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

	public static List<String> getAttachmentAttributes(String name) {
		return payloadAttributesToCapture.get(name);
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