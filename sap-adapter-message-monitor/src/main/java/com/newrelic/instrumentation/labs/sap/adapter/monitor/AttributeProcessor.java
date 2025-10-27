package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import static com.newrelic.instrumentation.labs.sap.adapter.monitor.AdapterMonitorLogger.logErrorWithMessage;
import static com.newrelic.instrumentation.labs.sap.adapter.monitor.AdapterMonitorLogger.logMessage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.newrelic.api.agent.Logger;
import com.newrelic.api.agent.NewRelic;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.search.api.IndexDataAttribute;
import com.sap.aii.af.search.core.DataAccess;
import com.sap.aii.af.search.core.IndexDataAttributeImpl;
import com.sap.aii.af.service.db.ConnectionManager;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.XMLPayload;

public class AttributeProcessor {

	protected static Map<String, Map<String,String>> message_Attributes = new HashMap<String, Map<String,String>>(10);
	protected static final String TABLE_NAME = "NR_ADAPTER_ATTRIBUTES";
	private static final String DROP_TABLE_SQL = "DROP TABLE " + TABLE_NAME;
	private static boolean loggedDBType = false;

	private static boolean initialized = false;
	private static final String CommonConnectionImpl_Class = "com.sap.sql.jdbc.common.CommonConnectionImpl";
	private static final String CommonConnectionHandle_Class = "com.sap.engine.services.dbpool.cci.CommonConnectionHandle";
	public static final String MESSAGEID = "MessageId";
	public static final String INPUT = "Input";
	public static final String OUTPUT = "Output";
	public static final String MESSAGE = "Message";

	public static final String MAP_TYPE = "MapType";

	private static synchronized void initialize() {
		if(initialized) return;
		Logger logger = NewRelic.getAgent().getLogger();
		Connection connection = getConnectionToUse();
		if(connection != null) {
			try {
				DatabaseMetaData dbMetaData = connection.getMetaData();
				boolean found = false;
				if(dbMetaData != null) {
					ResultSet tables;
					try {
						tables = dbMetaData.getTables(null, null, TABLE_NAME, null);
						found = false;
						while(tables.next()) {
							String tableName = tables.getString("TABLE_NAME");
							if (tableName.equalsIgnoreCase(TABLE_NAME)) { // Important for case-insensitive databases
								found = true;
								logMessage("Found table " + tableName);
								break;
							}
						}
						tables.close();
					} catch (SQLException e) {
						logger.log(Level.FINER, e, "Failed while trying to find if table {0} exists",TABLE_NAME);
						logErrorWithMessage("Failed to find table " + TABLE_NAME,e);
					}
					if(found) {
						dropTable(connection);
						logger.log(Level.FINE, "{0} dropped", TABLE_NAME);
						initialized = true;
					}
				}
				connection.close();
			} catch (SQLException e) {
				if(connection != null) {
					try {
						connection.close();
					} catch (SQLException e1) {
						logger.log(Level.FINER, e, "Failed to close connection after error while trying find or create table");
					}
				}
				logger.log(Level.FINER, e, "Failed while trying find or create table {0}",TABLE_NAME);
				logErrorWithMessage("Failed while trying to find or create table " + TABLE_NAME,e);
			}
		}
	}

	private static void dropTable(Connection connection) throws SQLException {
		Statement stmt = connection.createStatement();
		stmt.executeUpdate(DROP_TABLE_SQL);
		NewRelic.getAgent().getLogger().log(Level.FINE, "Dropped table {0}",TABLE_NAME);
	}

	protected static void setAttributes(MessageKey messageKey, Map<String,String> attributes) {
		long startTime = System.nanoTime();

		if(!initialized) {
			initialize();
		}

		if(attributes == null || attributes.isEmpty()) return;
		
		Map<String,String> attributesToReport = processAttributes(attributes);
		if(attributesToReport.isEmpty()) {
			logMessage("Skipping insert/update, no configured attributes found");
			return;
		}
		
		NewRelic.recordMetric("SAP/AttributeProcessor/AttributesToProcess", attributes.size());
		logMessage("Checking to insert or update attributes for message id "+ messageKey.getMessageId() + ": " + attributes);

			try {
				IndexDataAttribute[] dataAttributes = DataAccess.readAttributesByMessageKey(messageKey); //searchFacade.getDataAttributes(messageKey);
				logMessage("Read " + dataAttributes.length + " data attributes from DataAccess: " + Arrays.toString(dataAttributes));
				if(dataAttributes == null || dataAttributes.length == 0) {
					dataAttributes = new IndexDataAttribute[attributesToReport.size()];
					int index = 0;
					for(String key  : attributesToReport.keySet()) {
						IndexDataAttributeImpl dataAttribute = new IndexDataAttributeImpl();
						dataAttribute.setMessageKey(messageKey);
						dataAttribute.setName(key);
						String value = attributesToReport.get(key);
						dataAttribute.setValue(value);
						dataAttribute.setPosition(index);
						dataAttributes[index] = dataAttribute;
						index++;
					}
					DataAccess.upsert(dataAttributes);

					
				} else {
					Map<String, IndexDataAttribute> attributeMap = new Hashtable<String, IndexDataAttribute>();
					int index = dataAttributes.length;
					
					for(IndexDataAttribute existing : dataAttributes) {
						String name = existing.getName();
						attributeMap.put(name, existing);
					}
					boolean modified = false;
					for(String key  : attributesToReport.keySet()) {
						IndexDataAttribute indexData = attributeMap.get(key);
						if(indexData != null) {
							String existingValue = indexData.getValue();
							String currentValue = attributesToReport.get(key);
							if(existingValue == null || !existingValue.equalsIgnoreCase(currentValue)) {
								indexData.setValue(currentValue);
								modified = true;
							}
						} else {
							IndexDataAttributeImpl dataAttribute = new IndexDataAttributeImpl();
							dataAttribute.setMessageKey(messageKey);
							dataAttribute.setName(key);
							String value = attributesToReport.get(key);
							dataAttribute.setValue(value);
							dataAttribute.setPosition(index);
							index++;
							attributeMap.put(key, dataAttribute);
							modified = true;
						}
					}
					if(modified) {
						dataAttributes = new IndexDataAttribute[attributeMap.size()];
						int i = 0;
						for(IndexDataAttribute dataAttribute : attributeMap.values()) {
							dataAttributes[i] = dataAttribute;
							i++;
						}
						DataAccess.upsert(dataAttributes);					
					}
					
				}
				
				AdapterMonitorLogger.logMessage("Inserted " + dataAttributes.length + " using SearchFacade");
			} catch (Exception e) {
				AdapterMonitorLogger.logErrorWithMessage("Error while trying to set attributes", e);
			}

		long endTime = System.nanoTime();
		NewRelic.recordMetric("SAP/AttributeProcessor/SetAttributesTimer(ms)", (endTime-startTime)/1000000.0f);
		MessageMonitor.messageKeyToProcessDelayed(new MessageToProcess(messageKey));
	}
	
	private static Set<IndexDataAttribute> getCurrentAttributes(MessageKey messageKey) {
		Set<IndexDataAttribute> currentAttributes = new HashSet<IndexDataAttribute>();
		try {
			IndexDataAttribute[] dataAttributes = DataAccess.readAttributesByMessageKey(messageKey);
			AdapterMonitorLogger.logMessage("Retrieved attributes for messagekey " + messageKey + ": " + Arrays.toString(dataAttributes));
			if(dataAttributes.length > 0) {
				AdapterMonitorLogger.logMessage("Using data attributes array");
				currentAttributes.addAll(Arrays.asList(dataAttributes));
			} else {
				AdapterMonitorLogger.logMessage("Did not find attributes in array, trying map");

				List<MessageKey> msgKeys = new ArrayList<MessageKey>();
				msgKeys.add(messageKey);
				HashMap<MessageKey, ArrayList<IndexDataAttribute>> attributesMap = DataAccess.readAttributesByMessageKeys(msgKeys);
				AdapterMonitorLogger.logMessage("Map has " + attributesMap.size() + " elements");
				ArrayList<IndexDataAttribute> attributeList = attributesMap.get(messageKey);
				AdapterMonitorLogger.logMessage("Map returned " + attributeList.size() + " attributes for message key " + messageKey);
				if(!attributeList.isEmpty()) {
					currentAttributes.addAll(attributeList);
				}
			}
		} catch (Exception e) {
			AdapterMonitorLogger.logErrorWithMessage("Error while getting data attributes from DataAccess", e);
		}

		return currentAttributes;
	}

	public static Map<String,String> recordObject(Object requestMessage) {

		if(requestMessage instanceof Message) {
			Message xiMessage = (Message)requestMessage;

			XMLPayload document = xiMessage.getDocument();
			try {
				Document doc = loadXMLFromString(document.getText());
				Map<String,String> attributes = getAttributesFromXML(doc);
				return attributes;
			} catch (Exception e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to parse XML");
			}

		}
		return null;
	}

	public static Map<String, String> getAttributesFromXML(Document document) {
		Map<String,String> result = new HashMap<String, String>();

		Element root = document.getDocumentElement();
		processNode(root, result);

		return result;
	}

	private static void processNode(Node node, Map<String, String> attributes) {
		String name = node.getNodeName();
		node.getNodeType();
		if(node.hasChildNodes()) {
			NodeList children = node.getChildNodes();
			int length = children.getLength();
			for(int i=0;i<length;i++) {
				Node child = children.item(i);
				short type = child.getNodeType();
				if(type == Node.TEXT_NODE) {
					String value = child.getTextContent();
					String modifiedValue = value.replace("\\n", "").replace("\\t", "").trim();
					if(!modifiedValue.isEmpty()) {
						attributes.put(name.trim(), modifiedValue);
					}
				} else {
					processNode(child, attributes);
				}
			}
		}
	}


	public static Document loadXMLFromString(String xml) throws Exception {
		InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(stream);
	}

	public static void record(ModuleContext moduleContext, ModuleData moduleData) {
		if(!AttributeChecker.initialized) {
			AttributeChecker.startChecker();
		}
		AdapterMonitorLogger.logMessage("Added ModuleContext " + moduleContext + " and ModuleData " + moduleData + " to processing queue");
		AttributeChecker.addDataToQueue(new ModuleDataHolder(moduleData, moduleContext));
	}

	public static Map<String,String> getMessageAttributes(MessageKey messageKey) {
		AdapterMonitorLogger.logMessage("Getting attributes for message key " + messageKey);
		
		Map<String,String> attributes = new LinkedHashMap<String, String>();
		Set<IndexDataAttribute> current = getCurrentAttributes(messageKey);
		for(IndexDataAttribute dataAttribute : current) {
			attributes.put(dataAttribute.getName(), dataAttribute.getValue());
		}
		AdapterMonitorLogger.logMessage("Found " + attributes.size() + " from DataAccess");
		AdapterMonitorLogger.logMessage("Attributes reported " + attributes);
		NewRelic.recordMetric("SAP/AttributeProcessor/AttributesRetrieved", attributes.size());
		return attributes;
	}

	private static Connection getConnectionToUse() {
		try {
			ConnectionManager connectionMgr = ConnectionManager.getInstance();
			logMessage("ConnectionManager type: " + connectionMgr.getClass().getName());
			Connection connectionFromMgr = connectionMgr.getNoTXDBConnection();
			if (connectionFromMgr != null) {
				DatabaseMetaData metaData = connectionFromMgr.getMetaData();
				Map<String, Object> metaDataMap = new HashMap<String, Object>();
				if (!loggedDBType) {
					metaDataMap.put("Parent-Database-Product", metaData.getDatabaseProductName());
					metaDataMap.put("Parent-Database-Product-Version", metaData.getDatabaseProductVersion());
					metaDataMap.put("Parent-Database-Driver", metaData.getDriverName());
					metaDataMap.put("Parent-Database-Driver-Version", metaData.getDriverVersion());
				}
				Class<?> connectionClass = connectionFromMgr.getClass();
				String classname = connectionClass.getName();
				logMessage("Connection from ConnectionManager: " + classname);

				if (classname.equalsIgnoreCase("com.sap.engine.services.dbpool.cci.ConnectionHandle")
						|| classname.equalsIgnoreCase(CommonConnectionHandle_Class)
						|| classname.equalsIgnoreCase(CommonConnectionImpl_Class)) {
					try {
						Method getPhysicalConnection = connectionClass.getMethod("getPhysicalConnection",new Class<?>[0]);
						Object physicalConn = getPhysicalConnection.invoke(connectionFromMgr, new Object[0]);
						logMessage("Got PhysicalConnection: " + physicalConn);
						if (physicalConn != null) {
							Class<?> physicalConnClass = physicalConn.getClass();
							String physicalConnectionType = physicalConnClass.getName();
							if (physicalConnectionType.equalsIgnoreCase(CommonConnectionImpl_Class)) {
								try {
									Method getExtendedPooledConnectionMethod = physicalConnClass.getMethod("getExtendedPooledConnection", new Class[0]);
									Object extendedConn = getExtendedPooledConnectionMethod.invoke(physicalConn,new Object[0]);
									logMessage("Got ExtendedConnection: " + extendedConn);
									if (extendedConn != null) {
										Class<?> extendedConnectionClasss = extendedConn.getClass();
										Method getWrappedConnectionMethod = extendedConnectionClasss.getMethod("getWrappedConnection", new Class<?>[0]);
										Object wrappedCon = getWrappedConnectionMethod.invoke(extendedConn,new Object[0]);
										if (wrappedCon != null && wrappedCon instanceof Connection) {
											Connection wrappedConnection = (Connection)wrappedCon;
											if (!loggedDBType) {
												metaData = wrappedConnection.getMetaData();
												metaDataMap.put("Wrapped-Database-Product", metaData.getDatabaseProductName());
												metaDataMap.put("Wrapped-Database-Product-Version", metaData.getDatabaseProductVersion());
												metaDataMap.put("Wrapped-Database-Driver", metaData.getDriverName());
												metaDataMap.put("Wrapped-Database-Driver-Version", metaData.getDriverVersion());
												NewRelic.getAgent().getInsights().recordCustomEvent("SAP_Adapter_DBConnection", metaDataMap);
												loggedDBType = true;
											}
											logMessage("Connection to use set to " + wrappedConnection);
											return wrappedConnection;
										}
									}
								} catch (Exception e) {
									NewRelic.getAgent().getLogger().log(Level.FINE, e,"Failed to get connection to use from CommonConnectionImpl");
									logErrorWithMessage("Failed to get connection to use from CommonConnectionImpl", e);
								}
							} else {

							}
						}
					} catch (Exception e) {
						NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to get connection");
						logErrorWithMessage("Failed to get connection", e);
					}
				} else {
					return connectionFromMgr;
				} 
			}


		} catch (SQLException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE,e, "AttributeProcessor failed to aquire connection");
			logErrorWithMessage("AttributeProcessor failed to aquire connection", e);
		}		
		return null;
	}
	
	private static Map<String,String> processAttributes(Map<String,String> currentAttributes) {
		AttributeConfig config = AttributeConfig.getInstance();
		Map<String,String> attributesToReport = new HashMap<String, String>();
		if (config.collectingUserAttributes()) {
			Set<String> toCollect = config.attributesToCollect();
			Set<String> currentKeys = currentAttributes != null ? currentAttributes.keySet() : new HashSet<String>();
			Set<String> modifedKeys = new HashSet<String>();
			HashMap<String, String> keyMapping = new HashMap<String, String>();

			for(String key : currentKeys) {
				String mKey = key.toLowerCase().trim();
				String keyToUse = key;

				String tmp = "modulecontext-";
				if(mKey.startsWith(tmp)) {
					keyToUse = key.substring(tmp.length());
				}
				tmp = "SupplementalData-".toLowerCase();
				if(mKey.startsWith(tmp)) {
					keyToUse = key.substring(tmp.length());
				}
				modifedKeys.add(keyToUse.toLowerCase());
				keyMapping.put(keyToUse.toLowerCase(), key);

			}
					
			for(String attribute : toCollect) {
				String mKey = attribute.toLowerCase().trim();
				String keyToUse = attribute.trim();
				String tmp = "modulecontext-";
				if(mKey.startsWith(tmp)) {
					keyToUse = attribute.substring(tmp.length());
				}
				tmp = "SupplementalData-".toLowerCase();
				if(mKey.startsWith(tmp)) {
					keyToUse = attribute.substring(tmp.length());
				}
				keyToUse = keyToUse.toLowerCase();
				if(modifedKeys.contains(keyToUse)) {
					String mappedKey = keyMapping.get(keyToUse);
					if(mappedKey == null) mappedKey = keyToUse;
					String value = currentAttributes.get(mappedKey);

					attributesToReport.put(attribute, value);
				}

			}

		}


		return attributesToReport;
	}
}
