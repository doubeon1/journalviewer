package cz.doubeon.journalviewer.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SplashScreen {
	private static final String URL_STRING = "platform:/plugin/cz.doubeon.journalviewer.application/resource/kasa.jpg";

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
		try (InputStream is = new URL(URL_STRING).openStream()) {
			img = new Image(display, is);
		} catch (final IOException e) {
			label.setText("Here should be a splash image.");
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
