package com.newrelic.instrumentation.labs.sap.idoc;

import java.util.Map;

import com.sap.conn.idoc.IDocDocument;
import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.jco.JCoDestination;

public class SAPIDocsUtils {

	public static void addAttribute(Map<String,Object> attributes, String key, Object value) {
		if(attributes != null && key != null && !key.isEmpty() && value != null) {
			attributes.put(key,value);
		}
	}

	public static void addJCODestination(Map<String,Object> attributes, JCoDestination dest) {
		if(dest != null) {
			addAttribute(attributes, "ApplicationServerHost", dest.getApplicationServerHost());
			addAttribute(attributes, "DestinationName", dest.getDestinationName());
			addAttribute(attributes, "Client", dest.getClient());
			addAttribute(attributes, "GatewayService", dest.getGatewayService());
			addAttribute(attributes, "GatewayHost", dest.getGatewayHost());
		}
	}

	public static void addIDocDocumentList(Map<String,Object> attributes, IDocDocumentList iDocList) {
		addAttribute(attributes, "IDocDocumentList-DocType", iDocList.getIDocType());	
		addAttribute(attributes, "IDocDocumentList-NumberOfDocs", iDocList.getNumDocuments());	
		addAttribute(attributes, "IDocDocumentList-DocTypeExtension", iDocList.getIDocTypeExtension());	
	}
	
	public static void addIDocDocument(Map<String,Object> attributes, IDocDocument doc) {
		addAttribute(attributes, "IDocDocument-Client", doc.getClient());
		addAttribute(attributes, "IDocDocument-CreationTime", doc.getCreationTimeAsString());
		addAttribute(attributes, "IDocDocument-Direction", doc.getDirection());
		addAttribute(attributes, "IDocDocument-DocNumber", doc.getIDocNumber());
		addAttribute(attributes, "IDocDocument-EDIMessage", doc.getEDIMessage());
		addAttribute(attributes, "IDocDocument-EDIMessageType", doc.getEDIMessageType());
		addAttribute(attributes, "IDocDocument-IDocNumber", doc.getIDocNumber());
		addAttribute(attributes, "IDocDocument-IDocType", doc.getIDocType());
		addAttribute(attributes, "IDocDocument-IDocTypeExtension", doc.getIDocTypeExtension());
		addAttribute(attributes, "IDocDocument-MessageCode", doc.getMessageCode());
		addAttribute(attributes, "IDocDocument-MessageFunction", doc.getMessageFunction());
		addAttribute(attributes, "IDocDocument-MessageType", doc.getMessageType());
		addAttribute(attributes, "IDocDocument-RecipientAddress", doc.getRecipientAddress());
		addAttribute(attributes, "IDocDocument-OutputMode", doc.getOutputMode());
		addAttribute(attributes, "IDocDocument-SenderAddress", doc.getSenderAddress());
		addAttribute(attributes, "IDocDocument-Status", doc.getStatus());
	}

}
