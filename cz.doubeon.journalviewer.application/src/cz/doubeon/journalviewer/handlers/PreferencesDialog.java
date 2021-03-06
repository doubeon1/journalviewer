package cz.doubeon.journalviewer.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cz.doubeon.journalviewer.services.PreferenceService;

public class PreferencesDialog extends TitleAreaDialog {
	private Text txtDbPath;
	private Text txtDbName;
	private Text txtJournalPath;
	private Text txtDescFile;

	private final PreferenceService prefs;

	@Inject
	public PreferencesDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, PreferenceService prefs) {
		super(shell);
		this.prefs = prefs;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Nastavení");
		setMessage("Nastavení aplikace", IMessageProvider.NONE);
	}

	public String chooseFolder() {
		final DirectoryDialog dirDialog = new DirectoryDialog(getShell());
		final String value = dirDialog.open();
		return value;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = (Composite) super.createDialogArea(parent);

		final Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		final GridLayout layout = new GridLayout(3, false);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);

		final Label lblDbPath = new Label(container, SWT.NONE);
		lblDbPath.setText("Cesta k databázi");

		txtDbPath = new Text(container, SWT.BORDER);
		txtDbPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDbPath.setText(prefs.getDbPath());

		final Button buttDbPath = new Button(container, SWT.NONE);
		buttDbPath.setText("...");
		buttDbPath.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String newFolder = chooseFolder();
				if (newFolder != null) {
					txtDbPath.setText(newFolder);
				}
			}
		});

		final Label lblDbName = new Label(container, SWT.NONE);
		lblDbName.setText("Název databáze");

		txtDbName = new Text(container, SWT.BORDER);
		txtDbName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDbName.setText(prefs.getDbName());
		new Label(container, SWT.NONE);

		final Label lblJournalPath = new Label(container, SWT.NONE);
		lblJournalPath.setText("Cesta k žurnálům");

		txtJournalPath = new Text(container, SWT.BORDER);
		txtJournalPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtJournalPath.setText(prefs.getRootFolder());

		final Button buttJournalPath = new Button(container, SWT.NONE);
		buttJournalPath.setText("...");
		buttJournalPath.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String newFolder = chooseFolder();
				if (newFolder != null) {
					txtJournalPath.setText(newFolder);
				}
			}
		});

		final Label lblDescFile = new Label(container, SWT.NONE);
		lblDescFile.setText("Soubor s popisem pokladny");

		txtDescFile = new Text(container, SWT.BORDER);
		txtDescFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDescFile.setText(prefs.getDescFileName());
		new Label(container, SWT.NONE);

		return area;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private static String appendSlash(String path) {
		final String modifiedPath = path.replace('/', '\\');
		return modifiedPath.endsWith("\\") ? modifiedPath : modifiedPath + "\\";
	}

	// zatim bez validace
	private void saveInput() {
		prefs.setRootFolder(appendSlash(txtJournalPath.getText()));
		prefs.setDbPath(appendSlash(txtDbPath.getText()));
		prefs.setDbName(txtDbName.getText());
		prefs.setDescFileName(txtDescFile.getText());
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

}
