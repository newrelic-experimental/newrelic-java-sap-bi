package com.newrelic.instrumentation.sap.af;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;

public class SAPAFUtils {

	
	public static URI convert(com.sap.aii.af.sdk.xi.util.URI sapURI) {
		
		URI uri = null;
		
		try {
			uri = sapURI.toURI();
		} catch (URISyntaxException e) {
			NewRelic.getAgent().getLogger().log(Level.FINEST, e, "Failed to convert com.sap.aii.af.sdk.xi.util.URI: {0} to java.net.URI using toURI",sapURI);
		}
		
		if(uri == null) {
			try {
				uri = new URI(sapURI.toString());
			} catch (URISyntaxException e) {
				NewRelic.getAgent().getLogger().log(Level.FINEST, e, "Failed to convert com.sap.aii.af.sdk.xi.util.URI: {0} to java.net.URI using toString",sapURI);
			}
		}
		
		if(uri == null) {
			try {
				uri = new URI(sapURI.getScheme(),sapURI.getUserInfo(),sapURI.getHost(),sapURI.getPort(),sapURI.getPath(),sapURI.getQueryString(),sapURI.getFragment());
			} catch (URISyntaxException e) {
				NewRelic.getAgent().getLogger().log(Level.FINEST, e, "Failed to convert com.sap.aii.af.sdk.xi.util.URI: {0} to java.net.URI using get of items",sapURI);
			}
		}

		return uri;
	}
}
