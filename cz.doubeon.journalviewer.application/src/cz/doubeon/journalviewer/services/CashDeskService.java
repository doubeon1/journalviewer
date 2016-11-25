package cz.doubeon.journalviewer.services;

import static cz.doubeon.journalviewer.AppConstants.PREF_DESC_FILE;
import static cz.doubeon.journalviewer.AppConstants.PREF_JOURNAL_PATH;
import static cz.doubeon.journalviewer.AppConstants.PREF_PLUGIN_NAME;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import au.com.bytecode.opencsv.CSVReader;
import cz.doubeon.journalviewer.parser.ILineIterator;
import cz.doubeon.journalviewer.parser.ReceiptParser;
import cz.doubeon.journalviewer.punits.CashDesk;
import cz.doubeon.journalviewer.punits.JournalFile;
import cz.doubeon.journalviewer.punits.Receipt;

@Singleton
@Creatable
public class CashDeskService {
	private final EMFactoryService emfs;
	private final Logger logger;

	@Inject
	public CashDeskService(EMFactoryService emfs, Logger logger) {
		this.emfs = emfs;
		this.logger = logger;
	}

	private static String getDescFileName() {
		return Platform.getPreferencesService().getString(PREF_PLUGIN_NAME, PREF_DESC_FILE, null, null);
	}

	private static String getRootFolder() {
		return Platform.getPreferencesService().getString(PREF_PLUGIN_NAME, PREF_JOURNAL_PATH, null, null);
	}

	private static List<File> getDirectoryContent(File folder) {
		final File[] files = folder.listFiles((FileFilter) file -> {
			if (!file.isDirectory() && file.getName().toLowerCase().endsWith(".csv")) {
				return true;
			}
			return false;
		});
		if (files == null) {
			return Collections.emptyList();
		}
		Arrays.sort(files, (o1, o2) -> o1.getName().compareTo(o2.getName()));
		return Arrays.asList(files);
	}

	private List<File> getJournalsToParse(CashDesk cashDesk, EntityManager em) {
		final Map<String, JournalFile> map = em.createNamedQuery("JournalFile.cashDeskJournals", JournalFile.class)
				.setParameter("id", cashDesk.getId())
				.getResultList()
				.stream()
				.collect(Collectors.toMap(JournalFile::getFileNameUpper, Function.identity()));
		return getDirectoryContent(new File(getRootFolder(), cashDesk.getCsvFolder()))
				.stream()
				.filter(file -> !map.containsKey(file.getName().toUpperCase()))
				.collect(Collectors.toList());
	}

	private void parseJournal(CashDesk cashDesk, File file, IProgressMonitor monitor, EntityManager em)
			throws FileNotFoundException, InterruptedException {
		try (RawItemsReader li = new RawItemsReader(file, 5)) {
			final JournalFile journal = new JournalFile(file.getName(), cashDesk);
			em.persist(journal);
			while (li.hasNext()) {
				final Receipt receipt = new Receipt(cashDesk, journal);
				ReceiptParser.parseReceipt(receipt, li);
				if (receipt.getNumber() != null) {
					em.persist(receipt);
				}
				if (monitor.isCanceled()) {
					throw new InterruptedException();
				}
			}
		}
	}

	private List<String> getFolders() throws FileNotFoundException {
		final File file = new File(getRootFolder());
		final String[] directories = file.list((dir, name) -> new File(dir, name).isDirectory());
		if (directories == null) {
			throw new FileNotFoundException("Adresář " + file.getAbsolutePath() + " neexistuje.");
		}
		return Arrays.asList(directories);
	}

	private List<CashDesk> scanCashDesks(List<String> folders) throws IOException {
		final List<CashDesk> cashDesks = new ArrayList<>();
		for (final String folder : folders) {
			final File descFile = new File(getRootFolder() + File.separator + folder, getDescFileName());
			if (!descFile.exists()) {
				continue;
			}
			final String cashDescription = new String(Files.readAllBytes(Paths.get(descFile.getAbsolutePath())),
					StandardCharsets.UTF_8);
			final CashDesk cd = new CashDesk();
			cd.setCsvFolder(folder); // just the journal folder!!!
			cd.setDescription(cashDescription);
			cd.setId(folder);
			cashDesks.add(cd);
		}
		return cashDesks;
	}

	public void updateCashDeskList() throws IOException {
		final EntityManagerFactory emf = emfs.getEmFactory();
		final List<CashDesk> newCashDesks = scanCashDesks(getFolders());
		final EntityManager em = emf.createEntityManager();
		try {
			for (final CashDesk newCd : newCashDesks) {
				em.getTransaction().begin();
				final CashDesk cashDesk = em.find(CashDesk.class, newCd.getId());
				if (cashDesk == null) {
					em.persist(newCd);
				} else {
					cashDesk.setDescription(newCd.getDescription());
					// zmena popisu pokladny
				}
				em.getTransaction().commit();
			}
		} finally {
			em.close();
		}
	}

	public List<CashDesk> getCashDeskList() {
		final EntityManagerFactory emf = emfs.getEmFactory();
		final List<CashDesk> cashDesks = new ArrayList<>();
		try {
			new ProgressMonitorDialog(null).run(true, false, monitor -> {
				monitor.beginTask("Zjišťuji seznam pokladen...", IProgressMonitor.UNKNOWN);
				final EntityManager em = emf.createEntityManager();
				cashDesks.addAll(em.createNamedQuery("CashDesk.findAll", CashDesk.class).getResultList());
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e);
		}
		return cashDesks;
	}

	public void updateCashDesks(final List<CashDesk> cashDesks) {
		final EntityManagerFactory emf = emfs.getEmFactory();
		final EntityManager em = emf.createEntityManager();
		try {
			new ProgressMonitorDialog(null).run(true, true, monitor -> {
				final Map<CashDesk, List<File>> filesToParse = new HashMap<>();
				int total = 0;
				for (final CashDesk cashDesk1 : cashDesks) {
					final List<File> toParse = getJournalsToParse(cashDesk1, em);
					total = total + toParse.size();
					filesToParse.put(cashDesk1, toParse);
				}
				monitor.beginTask("Probíhá import žurnálu", total);
				try {
					for (final CashDesk cashDesk2 : cashDesks) {
						for (final File file : filesToParse.get(cashDesk2)) {
							monitor.subTask("Zpracovávám žurnál '" + file.getName() + " z pokladny '"
									+ cashDesk2.getDescription() + "'");
							em.getTransaction().begin();
							try {
								parseJournal(cashDesk2, file, monitor, em);
							} catch (final IOException e1) {
								em.getTransaction().rollback();
								throw new InvocationTargetException(e1);
							} catch (final InterruptedException e2) {
								em.getTransaction().rollback();
								throw e2;
							}
							em.getTransaction().commit();
							monitor.worked(1);
							em.clear();
						}
					}
				} finally {
					em.close();
					monitor.done();
				}
			});
		} catch (final InvocationTargetException e) {
			logger.error("Error updating journal database", e);
			MessageDialog.openError(null, "Chyba", "Chyba při aktualizaci databáze žurnálu.");
		} catch (final InterruptedException e) {
			MessageDialog.openInformation(null, "Info", "Operace přerušena uživatelem");
		}
	}

	public List<Receipt> getReceipts(CashDesk cashDesk, Date from, Date to) {
		final EntityManagerFactory emf = emfs.getEmFactory();
		final List<Receipt> receipts = new ArrayList<>();
		try {
			new ProgressMonitorDialog(null).run(true, false, monitor -> {
				monitor.beginTask("Načítám položky účtenky", IProgressMonitor.UNKNOWN);
				final EntityManager em = emf.createEntityManager();
				final TypedQuery<Receipt> query = em
						.createNamedQuery("Receipt.fromCashDeskPeriod", Receipt.class)
						.setParameter("id", cashDesk.getId())
						.setParameter("from", from)
						.setParameter("to", to);
				receipts.addAll(query.getResultList());
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e);
		}
		return receipts;
	}

	private class RawItemsReader implements ILineIterator {
		private final CSVReader reader;
		private boolean readNext;
		private String[] nextRow;
		private final int col;

		public RawItemsReader(File file, int col) throws FileNotFoundException {
			this.reader = new CSVReader(new InputStreamReader(new FileInputStream(file),
					Charset.forName("CP1250")), ';');
			this.col = col;
			this.readNext = false;
		}

		@Override
		public boolean hasNext() {
			if (!readNext) {
				try {
					nextRow = reader.readNext();
				} catch (final IOException e) {
					nextRow = null;
					logger.error("Error reading CSV", e);
				}
				readNext = true;
			}
			return nextRow != null;
		}

		@Override
		public String next() {
			if (!hasNext()) {
				return null;
			}
			readNext = false;
			return nextRow[col];
		}

		@Override
		public void close() {
			try {
				reader.close();
			} catch (final IOException e) {
				logger.error("Error closing file", e);
			}
		}
	}

}
