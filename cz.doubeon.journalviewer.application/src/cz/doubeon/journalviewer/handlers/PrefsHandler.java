package cz.doubeon.journalviewer.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

public class PrefsHandler {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, IEclipseContext context) {
		final PreferencesDialog dialog = ContextInjectionFactory.make(PreferencesDialog.class, context);
		dialog.create();
		if (dialog.open() == Window.OK) {
			MessageDialog.openInformation(shell, "Info", "Teď je potřeba provést aktualizaci!");
		}
	}
}
