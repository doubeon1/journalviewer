package cz.doubeon.journalviewer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class Activator implements BundleActivator {
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		configureLogbackInBundle(bundleContext.getBundle());
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		//
	}

	private void configureLogbackInBundle(Bundle bundle) throws JoranException, IOException {
		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		final JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(context);
		context.reset();

		// overriding the log directory property programmatically
		// in logback.xml use ${LOG_DIR}
		context.putProperty("LOG_DIR", System.getProperty("java.io.tmpdir"));

		// this assumes that the logback.xml file is in the root of the bundle.
		final URL logbackConfigFileUrl = FileLocator.find(bundle, new Path("/config/logback.xml"), null);
		try (InputStream is = logbackConfigFileUrl.openStream()) {
			jc.doConfigure(is);
		}
	}

}