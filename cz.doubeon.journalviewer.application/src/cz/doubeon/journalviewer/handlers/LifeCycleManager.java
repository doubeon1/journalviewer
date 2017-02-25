package cz.doubeon.journalviewer.handlers;

import static cz.doubeon.journalviewer.utils.AppConstants.TOPIC_CASHDESKS_UPDATED;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.doubeon.journalviewer.services.CashDeskService;
import cz.doubeon.journalviewer.services.PreferenceService;
import cz.doubeon.journalviewer.utils.AppConstants;
import cz.doubeon.journalviewer.utils.SplashScreen;

public class LifeCycleManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(LifeCycleManager.class);

	@PreSave
	public void done(PreferenceService pref) {
		LOGGER.info("Trying to save settings");
		pref.store();
	}

	@PostContextCreate
	public void postContextCreate(final IEventBroker eventBroker,
			IApplicationContext context, IEclipseContext ec, final CashDeskService updater, final Display display) {

		ec.declareModifiable(AppConstants.CTX_CASHDESK_OBSERVABLE);
		ec.declareModifiable(AppConstants.CTX_DATE_OBSERVABLE);
		ec.declareModifiable(AppConstants.CTX_RECEIPT_OBSERVABLE);
		//
		final Shell splash = SplashScreen.createSplash(display);
		eventBroker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				eventBroker.send(TOPIC_CASHDESKS_UPDATED, updater.getCashDeskList());
				splash.close();
				splash.dispose();
				eventBroker.unsubscribe(this);
			}
		});
		// close static splash screen
		context.applicationRunning();
		splash.open();
		LOGGER.info("Context created.");
	}

	@ProcessAdditions
	public void processAdditions(MApplication app, EModelService modelService, Display display) {
		final MWindow window = (MWindow) modelService.find("cz.doubeon.journalviewer.application.mainwindow", app);
		final Rectangle monitor = display.getPrimaryMonitor().getBounds();
		window.setWidth(750);
		window.setHeight(800);
		window.setX((monitor.width - window.getWidth()) / 2);
		window.setY((monitor.height - window.getHeight()) / 2);
	}
}
