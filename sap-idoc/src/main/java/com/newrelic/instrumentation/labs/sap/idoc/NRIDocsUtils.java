package com.newrelic.instrumentation.labs.sap.idoc;

import java.util.Map;

import com.sap.conn.idoc.IDocDocument;
import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.idoc.jco.JCoIDocServerContext;
import com.sap.conn.jco.JCoAttributes;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContext;

public class NRIDocsUtils {
	
	public static void addAttribute(Map<String, Object> attributes, String key, Object value) {
		if(attributes != null && key != null && !key.isEmpty() && value != null) {
			attributes.put(key, value);
		}
	}

	public static void addJcoServerContext(Map<String, Object> attributes, JCoServerContext ctx) {
		addAttribute(attributes, "SAP Call Type", ctx.getCallType());
		 addJcoAttributes(attributes,ctx.getConnectionAttributes());
		 addAttribute(attributes, "Connection ID", ctx.getConnectionID());
		 addJcoServer(attributes, ctx.getServer());
		 addJcoRepository(attributes, ctx.getRepository());
	}
	
	public static void addJcoAttributes(Map<String, Object> attributes, JCoAttributes jco_attributes) {
		if (jco_attributes != null) {
			addAttribute(attributes, "JcoAttributes - Client", jco_attributes.getClient());
			addAttribute(attributes, "JcoAttributes - Destination", jco_attributes.getDestination());
			addAttribute(attributes, "JcoAttributes - Host", jco_attributes.getHost());
		}
	}
	
	public static void addJcoServer(Map<String, Object> attributes, JCoServer server) {
		if (server != null) {
			addAttribute(attributes, "JCoServer - GatewayHost", server.getGatewayHost());
			addAttribute(attributes, "JCoServer - GatewayService", server.getGatewayService());
		}
	}
	
	public static void addJcoRepository(Map<String, Object> attributes, JCoRepository repo) {
		if(repo != null) {
			addAttribute(attributes, "JCoRepository - Name", repo.getName());
		}
	}
	
	public static void addJcoDestination(Map<String, Object> attributes, JCoDestination dest) {
		if(dest != null) {
			addAttribute(attributes, "JCoDestination - DestinationName", dest.getDestinationName());
			addAttribute(attributes, "JCoDestination - GatewayHost", dest.getGatewayHost());
			addAttribute(attributes, "JCoDestination - GatewayService", dest.getGatewayService());
		}
	}
	
	public static void addIDocDoument(Map<String, Object> attributes, IDocDocument doc) {
		if(doc != null) {
			addAttribute(attributes, "IDocDocument - DocType", doc.getIDocType());
			addAttribute(attributes, "IDocDocument - Direction", doc.getDirection());
			addAttribute(attributes, "IDocDocument - RecipientAddress", doc.getRecipientAddress());
			addAttribute(attributes, "IDocDocument - SenderAddress", doc.getSenderAddress());
		}
	}
	
	public static void addIDocDocumentList(Map<String, Object> attributes, IDocDocumentList docList) {
		if(docList != null) {
			addAttribute(attributes, "IDocDocumentList - DocType", docList.getIDocType());
			addAttribute(attributes, "IDocDocumentList - NumberDocs", docList.getNumDocuments());
		}
	}
	
	public static void addJcoIDocServerContext(Map<String, Object> attributes, JCoIDocServerContext ctx) {
		if(ctx != null) {
			addAttribute(attributes,"JCoIDocServerContext - FunctionName",ctx.getFunctionName());
			addAttribute(attributes,"JCoIDocServerContext - IDocQueueName",ctx.getIDocQueueName());
			addJcoServerContext(attributes, ctx.getJCoServerContext());
		}
	}
}
