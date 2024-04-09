package com.newrelic.instrumentation.sap.gateway;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;

public class LoadSAPMPLClasses {

	public static Simulate loadClassesForSimulate() {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Call to LoadSAPMPLClasses.loadClassesForSimulate()");
		Class<?> clazz = null;
		String dbAccessName = "com.sap.it.op.mpl.db.DatabaseMessageAccess";
		URLClassLoader urlLoader = null;
//		try {
//			clazz = Class.forName(dbAccessName);
//		} catch (ClassNotFoundException e) {
//			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Unable to load DatabaseMessageAccess using current classloader");
//		}

		if(clazz == null) {
			try {
				File jarFile = new File("/sapdb/usrsapPD1/J00/j2ee/cluster/apps/sap.com/com.sap.aii.igw.mpl.app/EJBContainer/applicationjars/com.sap.aii.igw.mpl.jpa.ejb.jar");
				File jarFile2 = new File("/sapdb/usrsapPD1/J00/j2ee/cluster/apps/sap.com/com.sap.aii.igw.monitoring.ui.app/app_libraries_container/com.sap.aii.igw.ui.interface.jar");
				File jarFile3 = new File("/sapdb/usrsapPD1/J00/j2ee/cluster/bin/ext/com.sap.aii.igw.esb.base.lib/lib/com.sap.esb.application.services-message.storage.api-1.63.0.jar");
				File jarFile4 = new File("/sapdb/usrsapPD1/J00/j2ee/cluster/bin/ext/com.sap.aii.igw.slf4j.lib/lib/org.slf4j-slf4j-api-1.7.5.jar");
				File jarFile5 = new File("/sapdb/usrsapPD1/SUM/sdt/data/install/standalone_public.jar");
				URL jarURL = jarFile.toURI().toURL();
				URL jar2URL = jarFile2.toURI().toURL();
				URL jar3URL = jarFile3.toURI().toURL();
				URL jar4URL = jarFile4.toURI().toURL();
				URL jar5URL = jarFile5.toURI().toURL();
				urlLoader = new URLClassLoader(new URL[] {jarURL,jar2URL,jar3URL, jar4URL, jar5URL});
				clazz = Class.forName(dbAccessName, true, urlLoader);
			} catch (Exception e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Unable to load DatabaseMessageAccess using jar classloader");
			}
		}
		
		if(clazz != null && urlLoader != null) {
			Simulate.initialize(urlLoader);
			return new Simulate();
		}
		return null;
	}
}
