package com.newrelic.instrumentation.labs.sap.payload;

import java.util.Iterator;
import java.util.Set;

import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.Payload;
import com.sap.engine.interfaces.messaging.api.XMLPayload;
import com.sap.engine.interfaces.messaging.spi.AbstractMessage;
import com.sap.engine.interfaces.messaging.spi.TransportableMessage;

public class PayloadProcessor {
	
	public static void processMessage(TransportableMessage message, String source) {
		if(message instanceof AbstractMessage) {
			AbstractMessage msg = (AbstractMessage)message;
			MessageKey msgKey = msg.getMessageKey();
			PayloadLogger.logMessage("Processing Message " + msgKey.toString() + " " + source);
			XMLPayload document = msg.getDocument();
			StringBuffer sb = new StringBuffer();
			if(document != null) {
				sb.append("Document: name= ");
				sb.append(document.getName());
				sb.append(", Attributes: [");
				Set<String> names = document.getAttributeNames();
				for(String name : names) {
					String value = document.getAttribute(name);
					sb.append("key=" + name + ", value=" + value + ", ");
				}
				sb.append(']');
				String text = document.getText();
				sb.append("Contents: " + text);
			} else {
				sb.append("No Document");
			}
			PayloadLogger.logMessage(sb.toString());
			sb = new StringBuffer();
			Payload payload = msg.getMainPayload();
			if(payload != null) {
				sb.append("Main Payload: name= ");
				sb.append(payload.getName());
				sb.append(", Attributes: [");
				Set<String> names = payload.getAttributeNames();
				for(String name : names) {
					String value = payload.getAttribute(name);
					sb.append("key=" + name + ", value=" + value + ", ");
				}
				sb.append(']');
				byte[] content = payload.getContent();
				sb.append("Content: " + new String(content));
			} else {
				sb.append("No Main Payload");
			}
			PayloadLogger.logMessage(sb.toString());
			int attachmentCount =msg.countAttachments();
			if (attachmentCount > 0) {
				@SuppressWarnings("rawtypes")
				Iterator attachmentIterator = msg.getAttachmentIterator();
				int count = 1;
				while (attachmentIterator.hasNext()) {
					sb = new StringBuffer();
					Payload attachement = (Payload) attachmentIterator.next();
					PayloadLogger.logMessage("Processing Attachement " + count);
					count++;
					sb.append("Attachment: name= ");
					sb.append(attachement.getName());
					sb.append(", Attributes: [");
					Set<String> names = attachement.getAttributeNames();
					for (String name : names) {
						String value = attachement.getAttribute(name);
						sb.append("key=" + name + ", value=" + value + ", ");
					}
					sb.append(']');
					byte[] content = payload.getContent();
					sb.append("Content: " + new String(content));
					PayloadLogger.logMessage(sb.toString());
				} 
			} else {
				PayloadLogger.logMessage("No attachments");
			}
			
		}
	}

}
