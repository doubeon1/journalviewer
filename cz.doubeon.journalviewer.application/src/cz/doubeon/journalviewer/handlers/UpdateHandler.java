package cz.doubeon.journalviewer.handlers;

import java.io.IOException;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import cz.doubeon.journalviewer.AppConstants;
import cz.doubeon.journalviewer.punits.CashDesk;
import cz.doubeon.journalviewer.services.CashDeskService;

public class UpdateHandler {
	@Execute
	public void execute(IEventBroker eventBroker, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell, Logger logger,
			final CashDeskService updater) {
		// update folders with journals
		try {
			updater.updateCashDeskList();
		} catch (final IOException e) {
			logger.error("Error loading journal folders", e);
			MessageDialog.openError(shell, "Chyba", "Chyba při načítání adresářů se žurnály.");
			return;
		}

		// update journals
		final List<CashDesk> cds = updater.getCashDeskList();
		updater.updateCashDesks(cds);

		eventBroker.post(AppConstants.TOPIC_CASHDESKS_UPDATED, cds);
	}
}