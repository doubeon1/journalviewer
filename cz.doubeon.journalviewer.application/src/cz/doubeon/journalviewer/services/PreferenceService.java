package cz.doubeon.journalviewer.services;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Creatable
public class PreferenceService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PreferenceService.class);
	private static final String PREF_JOURNAL_PATH = "JOURNAL_PATH";
	private static final String PREF_DB_PATH = "DB_PATH";
	private static final String PREF_DB_NAME = "DB_NAME";
	private static final String PREF_DESC_FILE = "DESC_FILE";

	private final IEclipsePreferences prefs;
	private String dbPath;
	private String dbName;
	private String descFileName;
	private String rootFolder;

	@Inject
	public PreferenceService(@Preference(nodePath = "cz.doubeon.journalviewer.application") IEclipsePreferences prefs) {
		this.prefs = prefs;
		this.dbPath = prefs.get(PREF_DB_PATH, "./");
		this.dbName = prefs.get(PREF_DB_NAME, "testdb");
		this.descFileName = prefs.get(PREF_DESC_FILE, "desc");
		this.rootFolder = prefs.get(PREF_JOURNAL_PATH, "./");
	}

	public void store() {
		try {
			prefs.put(PREF_DB_PATH, dbPath);
			prefs.put(PREF_DB_NAME, dbName);
			prefs.put(PREF_DESC_FILE, descFileName);
			prefs.put(PREF_JOURNAL_PATH, rootFolder);
			prefs.flush();
		} catch (final BackingStoreException e) {
			LOGGER.error("Error saving settings", e);
		}
	}

	public String getDbPath() {
		return dbPath;
	}

	public void setDbPath(String dbPath) {
		this.dbPath = dbPath;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDescFileName() {
		return descFileName;
	}

	public void setDescFileName(String descFileName) {
		this.descFileName = descFileName;
	}

	public String getRootFolder() {
		return rootFolder;
	}

	public void setRootFolder(String rootFolder) {
		this.rootFolder = rootFolder;
	}

}
