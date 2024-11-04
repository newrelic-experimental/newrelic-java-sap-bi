package com.newrelic.instrumentation.labs.sap.jco;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.conn.jco.JCoAttributes;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoFunctionUnit;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContext;

public class NRJcoUtils {

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
		addAttribute(attributes, "JCoServerCallType", ctx.getCallType());
		addAttribute(attributes, "TID", ctx.getTID());
		String[] queueNames = ctx.getQueueNames();
		if(queueNames !=  null && queueNames.length > 0) {
			addAttribute(attributes, "QueueNames", getQueueNames(queueNames));
		}
		addAttribute(attributes, "QueueName", ctx.getQueueName());

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

	public static void addJCoFunctionUnit(Map<String,Object> attributes, JCoFunctionUnit unit) {
		if(unit != null) {
			List<JCoFunction> functions = unit.getFunctions();
			StringBuffer functionNames = new StringBuffer();
			int size = functions.size();
			int i = 1;
			for(JCoFunction function : functions) {
				functionNames.append(function.getName());
				if(i < size) {
					functionNames.append(',');
				}
				i++;
			}
			addAttribute(attributes, "Functions", functionNames.toString());

			Set<String> queueNames = unit.getQueueNames();
			size = queueNames.size();
			i = 1;
			StringBuffer sb = new StringBuffer();
			for(String queueName : queueNames) {
				sb.append(queueName);
				if(i < size) {
					sb.append(',');
				}
				i++;
			}
			addAttribute(attributes, "QueueNames", sb.toString());

		}
	}

	private static String getQueueNames(String[] names) {
		int len = names.length;
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<len;i++) {
			String queueName = names[i];
			sb.append(queueName);
			if(i < len -1) sb.append(',');
		}
		return sb.toString();
	}

}
