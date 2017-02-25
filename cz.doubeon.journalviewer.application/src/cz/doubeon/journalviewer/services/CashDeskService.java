package cz.doubeon.journalviewer.services;

import java.io.File;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import cz.doubeon.journalviewer.parser.ILineIterator;
import cz.doubeon.journalviewer.parser.ReceiptParser;
import cz.doubeon.journalviewer.punits.CashDesk;
import cz.doubeon.journalviewer.punits.JournalFile;
import cz.doubeon.journalviewer.punits.Receipt;

@Singleton
@Creatable
public class CashDeskService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CashDeskService.class);
	private final PreferenceService prefs;
	private final EMFactoryService emfs;

	@Inject
	public CashDeskService(EMFactoryService emfs, PreferenceService prefs) {
		this.emfs = emfs;
		this.prefs = prefs;
	}

	private static List<File> getDirectoryContent(File folder) {
		File[] files = folder
				.listFiles(file -> !file.isDirectory() && file.getName().toLowerCase().endsWith(".csv"));
		if (files == null) {
			files = new File[0];
		}
		return Stream.of(files)
				.sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
				.collect(Collectors.toList());
	}

	private List<File> getJournalsToParse(CashDesk cashDesk, EntityManager em) {
		final Map<String, JournalFile> map = em.createNamedQuery("JournalFile.cashDeskJournals", JournalFile.class)
				.setParameter("id", cashDesk.getId())
				.getResultList()
				.stream()
				.collect(Collectors.toMap(JournalFile::getFileNameUpper, Function.identity()));
		return getDirectoryContent(new File(prefs.getRootFolder(), cashDesk.getCsvFolder()))
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
		final File file = new File(prefs.getRootFolder());
		final String[] directories = file.list((dir, name) -> new File(dir, name).isDirectory());
		if (directories == null) {
			throw new FileNotFoundException("Adresář " + file.getAbsolutePath() + " neexistuje.");
		}
		return Arrays.asList(directories);
	}

	private List<CashDesk> scanCashDesks(List<String> folders) throws IOException {
		final List<CashDesk> cashDesks = new ArrayList<>();
		for (final String folder : folders) {
			final File descFile = new File(prefs.getRootFolder() + File.separator + folder, prefs.getDescFileName());
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
			LOGGER.error("Error", e);
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
				for (final CashDesk cashDesk : cashDesks) {
					final List<File> toParse = getJournalsToParse(cashDesk, em);
					total = total + toParse.size();
					filesToParse.put(cashDesk, toParse);
				}
				monitor.beginTask("Probíhá import žurnálu", total);
				try {
					for (final CashDesk cashDesk : cashDesks) {
						for (final File file : filesToParse.get(cashDesk)) {
							LOGGER.info("Processing file: '" + file + "'");
							monitor.subTask("Zpracovávám žurnál '" + file.getName() + " z pokladny '"
									+ cashDesk.getDescription() + "'");
							em.getTransaction().begin();
							try {
								parseJournal(cashDesk, file, monitor, em);
							} catch (final Exception e) {
								em.getTransaction().rollback();
								if (e instanceof InterruptedException) {
									throw (InterruptedException) e;
								}
								throw new InvocationTargetException(e);
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
			LOGGER.error("Error updating journal database", e);
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
			LOGGER.error("Error", e);
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
					LOGGER.error("Error reading CSV file!", e);
				}
				readNext = true;
			}
			return nextRow != null;
		}

		@Override
		public String next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			readNext = false;
			final String val = nextRow[col];
			return val == null ? "" : val;
		}

		@Override
		public void close() {
			try {
				reader.close();
			} catch (final IOException e) {
				LOGGER.warn("Error closing CSV file", e);
			}
		}
	}

}
