package cz.doubeon.journalviewer.services;

import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Creatable
public class EMFactoryService {
	private static final Logger LOGGER = LoggerFactory.getLogger(EMFactoryService.class);
	private static final String PERSISTENCE_UNIT_NAME = "parser";
	private final HashMap<Object, Object> properties = new HashMap<>();
	private final PreferenceService pref;
	private EntityManagerFactory factory;

	@Inject
	public EMFactoryService(PreferenceService pref) {
		this.pref = pref;
	}

	@PostConstruct
	public void loadSettings() {
		properties.put(PersistenceUnitProperties.JDBC_URL,
				"jdbc:derby:" + pref.getDbPath() + pref.getDbName() + ";create=true");
		properties.put(PersistenceUnitProperties.CLASSLOADER, getClass().getClassLoader());
		properties.put("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
		properties.put("eclipselink.logging.level.sql", "OFF");
		properties.put("eclipselink.logging.level", "INFO");
		properties.put("eclipselink.logging.logger", "cz.doubeon.journalviewer.logging.Slf4jSessionLogger");
		// properties.put("javax.persistence.jdbc.user", "test");
		// properties.put("javax.persistence.jdbc.password", "test");
		properties.put("eclipselink.ddl-generation", "create-tables");
		properties.put("eclipselink.ddl-generation.output-mode", "database");
		// redirect derby log to slf4j
		System.getProperties().put("derby.stream.error.method",
				"cz.doubeon.journalviewer.logging.DerbySlf4jBridge.bridge");
		LOGGER.info("Initializing database using JDBC URL: " + properties.get(PersistenceUnitProperties.JDBC_URL));
	}

	private EntityManagerFactory createEmFactory() {
		return new PersistenceProvider().createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
	}

	public EntityManagerFactory getEmFactory() {
		if (factory == null) {
			factory = createEmFactory();
			LOGGER.info("EM Factory created.");
		}
		return factory;
	}

	@PreDestroy
	void close() {
		if (factory != null && factory.isOpen()) {
			factory.close();
			LOGGER.info("EM Factory closed.");
		}
	}

}
