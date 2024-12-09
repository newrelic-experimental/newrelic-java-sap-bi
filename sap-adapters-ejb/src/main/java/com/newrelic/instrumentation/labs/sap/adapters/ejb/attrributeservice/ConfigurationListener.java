package com.newrelic.instrumentation.labs.sap.adapters.ejb.attrributeservice;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.TimerTask;
import java.util.logging.Level;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.instrumentation.labs.sap.adapters.ejb.DataUtils;

public class ConfigurationListener extends TimerTask {

	private static ConfigurationListener INSTANCE = null;
	private long lastModified;
	private File configFile;
	
	public static ConfigurationListener getListener() {
		if(INSTANCE == null) {
			INSTANCE = new ConfigurationListener();
		}
		return INSTANCE;
	}

	private ConfigurationListener() {
		File newRelicDir = ConfigFileHelper.getNewRelicDirectory();
		configFile = new File(newRelicDir, "attribute-config.json");
		lastModified = configFile.lastModified();

	}

	@Override
	public void run() {
		try {
			if(configFile.lastModified() > lastModified) {
				// config file has changed so process it
				loadConfig();
				lastModified = configFile.lastModified();
			}
		} catch (FileNotFoundException e) {
			NewRelic.getAgent().getLogger().log(Level.FINEST, e, "Configuration file {0} was not found",configFile);
		} catch (JsonException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Error was generated while trying to parse JSON contents from file {0}",configFile);
		}
	}
	
	protected void loadConfig() throws FileNotFoundException, JsonException {
		NewRelic.getAgent().getLogger().log(Level.FINE, "processing SAP attribute configuration");
		AttributeConfiguration.clearAttributes();
		FileReader reader = new FileReader(configFile);
		Object obj = Jsoner.deserialize(reader);
		if(obj != null) {
			if(obj instanceof JsonObject) {
				JsonObject json = (JsonObject)obj;
				Object ctxObj = json.get("ModuleContext");
				if(ctxObj != null && ctxObj instanceof JsonObject) {
					JsonObject contextConfig = (JsonObject)ctxObj;
					Boolean collId = contextConfig.getBooleanOrDefault(new BooleanJsonKey("collectChannelId", Boolean.TRUE));
					AttributeConfiguration.setCollectContextChannelId(collId);
					Object obj1 = contextConfig.get("attributesToCollect");
					if(obj1 != null) {
						if(obj1 instanceof JsonArray) {
							JsonArray jArray = (JsonArray)obj1;
							int size = jArray.size();
							NewRelic.getAgent().getLogger().log(Level.FINE,"Will collect {0} ModuleContext attributes", size);
							for(int index = 0;index<size;index++) {
								AttributeConfiguration.addContextAttribute(jArray.getString(index));
							}

						} else {
							NewRelic.getAgent().getLogger().log(Level.FINE,"value of attributesToCollect is {0} " , contextConfig.get("attributesToCollect"));
						}
					} else {
						NewRelic.getAgent().getLogger().log(Level.FINE,"value of attributesToCollect for ModuleContext is null");
					}
				} else {
					NewRelic.getAgent().getLogger().log(Level.FINE,"Did not find ModuleContext configuration, value was {0}",obj);
				}
				Object dataObject = json.get("ModuleData");
				if(dataObject != null && dataObject instanceof JsonObject) {
					JsonObject dataJson = (JsonObject)dataObject;
					Object sDataObj = dataJson.get("supplementalData");
					if(sDataObj != null && sDataObj instanceof JsonObject) {
						JsonObject suppData = (JsonObject)sDataObj;
						Boolean enabled = suppData.getBoolean(new BooleanJsonKey("enabled"));
						NewRelic.getAgent().getLogger().log(Level.FINE,"Supplemental Data enabled: {0} ", enabled);
						AttributeConfiguration.setSupplementalEnabled(enabled);
						Object arrayObj = suppData.get("attributesToCollect");
						if(arrayObj != null && arrayObj instanceof JsonArray) {
							JsonArray arrayJson = (JsonArray)arrayObj;
							int size = arrayJson.size();
							NewRelic.getAgent().getLogger().log(Level.FINE,"Will collect {0} SupplementalData attributes", size);
							for(int index = 0;index<size;index++) {
								String attr = arrayJson.getString(index);
								NewRelic.getAgent().getLogger().log(Level.FINE,"Will collect supplemental attributes {0}", attr);
								AttributeConfiguration.addSupplementalAttribute(attr);
							}

						} else {
							NewRelic.getAgent().getLogger().log(Level.FINE,"Did not find supplemental data attributes to collect, found {0}",sDataObj);
						}
					} else {
						NewRelic.getAgent().getLogger().log(Level.FINE,"No supplemental data configuration was found", sDataObj);
					}
					Object prinObj = dataJson.get("principalData");
					if(prinObj != null && prinObj instanceof JsonObject) {
						JsonObject prinJson = (JsonObject)prinObj;
						Boolean enabled = prinJson.getBoolean(new BooleanJsonKey("enabled"));
						AttributeConfiguration.setPrincipalEnabled(enabled);
						NewRelic.getAgent().getLogger().log(Level.FINE,"Principal Data enabled: {0} ", enabled);
						Boolean collectDefault = prinJson.getBoolean(new BooleanJsonKey("collectDefault"));
						AttributeConfiguration.setPrincipalDefaultsEnabled(collectDefault);
						NewRelic.getAgent().getLogger().log(Level.FINE,"Principal Data collect default: {0} ", collectDefault);
						Object arrayObj = prinJson.get("attributesToCollect");
						if(arrayObj != null && arrayObj instanceof JsonArray) {
							JsonArray arrayJson = (JsonArray)arrayObj;
							int size = arrayJson.size();
							NewRelic.getAgent().getLogger().log(Level.FINE,"Will collect {0} PrincipalData attributes", size);
							for(int index = 0;index<size;index++) {
								String attribute =arrayJson.getString(index);
								int propertyIndex = attribute.indexOf("MessagePropertyKey [");
								if(propertyIndex > -1) {
									String tmp = attribute.substring(propertyIndex + "MessagePropertyKey [".length(), attribute.length()-1);
									String[] parts = tmp.split("[= ]");
									if(parts.length == 4) {
										String propertyName = null;
										String propertyNamespace = null;
										for(int i=0;i<4;i=i+2) {
											String s = parts[i];
											if(s.equals("propertyName")) {
												propertyName = parts[i+1].replace(',', ' ').trim();
											} else if(s.equals("propertyNamespace")) {
												propertyNamespace = parts[i+1];
											}
										}
										if(propertyName != null) {
											AttributeConfiguration.addMessagePropertyKey(propertyNamespace, propertyName);
										}

									}
								} else {
									int attachmentIndex = attribute.indexOf("Attachment-Attribute: ");
									if(attachmentIndex > -1) {
										String tmp = attribute.substring(attachmentIndex + "Attachment-Attribute: ".length());
										String[] parts = tmp.split("[=,]");
										if(parts.length == 4) {
											String payloadName = null;
											String attributeName = null;
											for(int i=0;i<parts.length;i=i+2) {
												String s = parts[i];
												if(s.equals("PayloadName")) {
													payloadName = parts[i+1].replace(',', ' ').trim();
												} else if(s.equals("AttributeName")) {
													attributeName = parts[i+1];
												}
												
											}
											if(payloadName != null && attributeName != null) {
												AttributeConfiguration.addAttributeKey(payloadName, attributeName);
											}
										}
									} else {
										AttributeConfiguration.addPrincipalAttribute(attribute);
									}
								}
							}

						} else {
							NewRelic.getAgent().getLogger().log(Level.FINE,"Did not find principal data attributes to collect, found {0}", sDataObj);
						}
					} else {
						NewRelic.getAgent().getLogger().log(Level.FINE,"No principal data configuration was found", sDataObj);
					}

				} else {
					NewRelic.getAgent().getLogger().log(Level.FINE,"No Module data configuration was found");
				}
			}
		}
		DataUtils.reset();
		
	}

	private class BooleanJsonKey implements JsonKey {

		private String key = null;
		private Boolean value = null;

		public BooleanJsonKey(String k, Boolean def) {
			key = k;
			value = def;
		}

		public BooleanJsonKey(String k) {
			this(k,Boolean.FALSE);
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public Boolean getValue() {
			return value;
		}

	}


}

