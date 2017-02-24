package cz.doubeon.journalviewer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class Activator implements BundleActivator {
	private BundleContext bundleContext;
	private static Activator instance;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		this.bundleContext = bundleContext;
		configureLogbackInBundle();
		synchronized (Activator.class) {
			Activator.instance = this;
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		synchronized (Activator.class) {
			Activator.instance = null;
		}
		this.bundleContext = null;
	}

	public URL getResourceURL(String resPath) {
		return FileLocator.find(bundleContext.getBundle(), new Path(resPath), null);
	}

	private void configureLogbackInBundle() throws JoranException, IOException {
		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		final JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(context);
		context.reset();

		// overriding the log directory property programmatically
		// in logback.xml use ${LOG_DIR}
		context.putProperty("LOG_DIR", System.getProperty("java.io.tmpdir"));
		try (InputStream is = getResourceURL("config/logback.xml").openStream()) {
			jc.doConfigure(is);
		}
	}

	public static Activator getInstance() {
		return instance;
	}

}