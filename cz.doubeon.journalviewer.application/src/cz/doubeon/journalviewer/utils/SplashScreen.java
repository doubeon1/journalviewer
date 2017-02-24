package cz.doubeon.journalviewer.utils;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.doubeon.journalviewer.Activator;
import cz.doubeon.journalviewer.services.CashDeskService;

public class SplashScreen {
	private static final Logger LOGGER = LoggerFactory.getLogger(CashDeskService.class);
	private static final String SPLASH_IMAGE = "/resource/kasa.jpg";

	private SplashScreen() {
	}

	public static Shell createSplash(Display display) {
		final Shell shell = new Shell(display, SWT.TOOL | SWT.NO_TRIM);
		final GridLayout gl = new GridLayout(1, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		shell.setLayout(gl);

		final Label label = new Label(shell, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.UP, false, false));
		Image img = null;
		try (InputStream is = Activator.getInstance().getResourceURL(SPLASH_IMAGE).openStream()) {
			img = new Image(display, is);
		} catch (final IOException e) {
			LOGGER.error("Error loading splash screen", e);
		}
		label.setImage(img);
		label.addDisposeListener(e -> {
			final Image image = ((Label) e.widget).getImage();
			if (image != null) {
				image.dispose();
			}
		});
		shell.pack();
		final Rectangle monitor = display.getPrimaryMonitor().getBounds();
		final Point location = new Point((monitor.width - shell.getSize().x) / 2,
				(monitor.height - shell.getSize().y) / 2);
		shell.setLocation(location);

		return shell;
	}
}
