package com.newrelic.instrumentation.sap.httpserver;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.newrelic.api.agent.ExtendedRequest;
import com.newrelic.api.agent.HeaderType;
import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.services.httpserver.chain.HTTPRequest;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.client.Request;
import com.sap.engine.services.httpserver.interfaces.client.RequestLine;
import com.sap.engine.services.httpserver.lib.HttpCookie;
import com.sap.engine.services.httpserver.lib.headers.MimeHeaders;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.httpserver.server.Client;

public class SAPExtendedRequest extends ExtendedRequest {
	
	private HTTPRequest http_request = null;
	private HttpParameters http_params = null;
	private Request request = null;
	private Map<String, List<String>> params = new Hashtable<>();
	
	@SuppressWarnings("deprecation")
	public SAPExtendedRequest(HTTPRequest req) {
		http_request = req;
		if(http_request != null) {
			http_params = http_request.getHTTPParameters();
			if(http_params != null) {
				request = http_params.getRequest();
				if(request != null) {
					MessageBytes query = request.getRequestLine().getQuery();
					if (query != null) {
						String queryString = query.toString();
						if (queryString != null && !queryString.isEmpty()) {
							String[] splits = queryString.split("&");
							for (String split : splits) {
								int index = split.indexOf('=');
								if (index > -1) {
									String name = split.substring(0, index);
									String value = split.substring(index + 1);
									boolean keyExists = params.containsKey(name);
									List<String> list;
									if (keyExists) {
										list = params.get(name);
									} else {
										list = new ArrayList<>();
									}

									list.add(value);
									params.put(name, list);
								}
							}
						} 
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getRequestURI() {
		String requestURI = null;
		if(http_request != null) {
			Client client = http_request.getClient();
			if(client != null) {
				Request req = client.getRequest();
				if(req != null) {
					RequestLine requestLine = req.getRequestLine();
					if(requestLine != null) {
						MessageBytes urlDecoded = requestLine.getUrlDecoded();
						if(urlDecoded != null) {
							requestURI = urlDecoded.toString();
						}
					}
				}
			}
		}
		if(requestURI == null && request != null) {
			RequestLine requestLine = request.getRequestLine();
			if(requestLine != null) {
				MessageBytes url = requestLine.getFullUrl();
				if (url != null) {
					return url.toString();
				}
			}
		}
		return requestURI;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getParameterNames() {
		
		Set<String> keys = params.keySet();
		
		Vector<String> vector = new Vector<>();
		for(String key : keys) {
			vector.add(key);
		}
		
		return vector.elements();
	}

	@Override
	public String[] getParameterValues(String name) {
		List<String> list = params.get(name);
		String[] values = list == null ? new String[0] : new String[list.size()];
		if(list != null) {
			list.toArray(values);
		}
		return values;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAttribute(String name) {
		if(http_params != null) {
			Hashtable<String,Object> attrs = http_params.getRequestAttributes();
			if(attrs != null) {
				return attrs.get(name);
			}
		}
		return null;
	}

	@Override
	public String getCookieValue(String name) {
		if (request != null) {
			ArrayObject cookies = request.getCookies(true);
			if(cookies != null) {
				for (int i = 0; i < cookies.size(); ++i) {
					HttpCookie httpCookie = (HttpCookie) cookies.elementAt(i);
					if (name.equalsIgnoreCase(httpCookie.getName())) {
						return httpCookie.getValue();
					}
				}
				
			}
		}
		return null;
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.HTTP;
	}

	@Override
	public String getHeader(String name) {
		MimeHeaders reqHeaders = request.getHeaders();
		return reqHeaders != null ? reqHeaders.getHeader(name) : null;
	}

	@Override
	public String getMethod() {
		return http_request.getMethod();
	}

}
