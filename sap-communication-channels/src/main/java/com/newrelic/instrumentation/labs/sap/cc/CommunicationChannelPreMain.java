package com.newrelic.instrumentation.labs.sap.cc;

import java.lang.instrument.Instrumentation;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;

public class CommunicationChannelPreMain {
	
	private static final String SAP_SYSTEM_NAME = "SAPSYSTEMNAME";
	private static final String SAP_MYNAME = "SAPMYNAME";
	private static final String SAPVERSION = "SAPJStartVersion";
	private static final String SAPPRODUCT = "SAP";
	private static ClassLoader LOGGING_CLASSLOADER = null;


	public static void premain(String s, Instrumentation inst) {
		init();
	}

	public static void init() {
		if(!isSAP()) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Skipping Communication Channel logging, application does not appear to be SAP");
			return;
		}
		NewRelic.getAgent().getLogger().log(Level.FINE, "Call to initialize Communcation Channel Logging");
		
		if(CommunicationChannelLogger.initialized) return;
		
//		try {
//			File nrDir = ConfigFileHelper.getNewRelicDirectory();
//			File extensions = new File(nrDir,"extensions");
//			Class<?> clazz = null;
//			if(extensions != null && extensions.exists()) {
//				File nrLabsLogging = new File(extensions,"LabsCustomLog4j-1.0.0.jar");
//				if(nrLabsLogging != null && nrLabsLogging.exists()) {
//					URLClassLoader cl = new URLClassLoader(new URL[] {nrLabsLogging.toURI().toURL()},CommunicationChannelPreMain.class.getClassLoader());
//					clazz = Class.forName("com.newrelic.labs.log4j.LogManager",true,cl);
//					Class.forName("com.newrelic.labs.log4j.Level",true,cl);
//					Class.forName("com.newrelic.labs.log4j.core.LoggerContext",true,cl);
//					Class.forName("com.newrelic.labs.log4j.core.config.Configurator",true,cl);
//					Class.forName("com.newrelic.labs.log4j.core.config.builder.api.AppenderComponentBuilder",true,cl);
//					Class.forName("com.newrelic.labs.log4j.core.config.builder.api.ComponentBuilder",true,cl);
//					Class.forName("com.newrelic.labs.log4j.core.config.builder.api.ConfigurationBuilder",true,cl);
//					Class.forName("com.newrelic.labs.log4j.core.config.builder.api.ConfigurationBuilderFactory",true,cl);
//					Class.forName("com.newrelic.labs.log4j.core.config.builder.api.LayoutComponentBuilder",true,cl);
//					Class.forName("com.newrelic.labs.log4j.core.config.builder.impl.BuiltConfiguration",true,cl);
//					Class.forName("com.newrelic.labs.log4j.spi.ExtendedLogger",true,cl);
//					Class.forName("com.newrelic.labs.log4j.core.config.Configuration",true,cl);
//
//				}
//			}
//			
//			if(clazz == null) {
//				NewRelic.getAgent().getLogger().log(Level.FINE, "Could not initialize Communcation Channel Logger because did not find Labs logging classes");
//				return;
//			}
			
			CommunicationChannelLogger.init();
//		} catch (Exception e) {
//			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to initialize Communication Channel logging due to {0}", e.getClass().getSimpleName());
//		}
	}

	private static boolean isSAP() {
		String p = System.getProperty(SAPPRODUCT);
		if(p != null) return true;
		
		p = System.getProperty(SAP_MYNAME);
		if(p != null) return true;

		p = System.getProperty(SAPVERSION);
		if(p != null) return true;

		p = System.getProperty(SAP_SYSTEM_NAME);
		if(p != null) return true;

		return false;
	}

	public static ClassLoader getLOGGING_CLASSLOADER() {
		return LOGGING_CLASSLOADER;
	}
}
