package cz.doubeon.journalviewer.services;

import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;

import cz.doubeon.journalviewer.AppConstants;

@Singleton
@Creatable
public class EMFactoryService {
	private static final String PERSISTENCE_UNIT_NAME = "parser";
	private EntityManagerFactory factory;
	private final HashMap<Object, Object> properties = new HashMap<Object, Object>();

	public EMFactoryService() {
		//
	}

	@PostConstruct
	public void loadSettings(Logger logger) {
		final IPreferencesService service = Platform.getPreferencesService();
		final String dbPath = service.getString(AppConstants.PREF_PLUGIN_NAME, AppConstants.PREF_DB_PATH, null, null);
		final String dbName = service.getString(AppConstants.PREF_PLUGIN_NAME, AppConstants.PREF_DB_NAME, null, null);
		properties.put(PersistenceUnitProperties.JDBC_URL, "jdbc:derby:" + dbPath + dbName + ";create=true");
		properties.put(PersistenceUnitProperties.CLASSLOADER, getClass().getClassLoader());
		if (logger.isInfoEnabled()) {
			logger.info("using JDBC URL: " + properties.get(PersistenceUnitProperties.JDBC_URL));
		}
	}

	private EntityManagerFactory createEmFactory() {
		return new PersistenceProvider().createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
	}

	public EntityManagerFactory getEmFactory() {
		if (factory == null) {
			factory = createEmFactory();
		}
		return factory;
	}

	@PreDestroy
	void close() {
		if (factory != null && factory.isOpen()) {
			factory.close();
		}
	}

}
