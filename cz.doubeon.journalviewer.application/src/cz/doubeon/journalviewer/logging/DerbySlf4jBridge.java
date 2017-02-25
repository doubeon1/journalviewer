package cz.doubeon.journalviewer.logging;

import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DerbySlf4jBridge extends Writer {
	private static final Logger logger = LoggerFactory.getLogger(DerbySlf4jBridge.class);

	@Override
	public void write(final char[] cbuf, final int off, final int len) {
		// Don't bother with empty lines.
		if (len > 1) {
			logger.info(new String(cbuf, off, len));
		}
	}

	@Override
	public void flush() {
		// nop
	}

	@Override
	public void close() {
		// nop
	}

	public static Writer bridge() {
		return new DerbySlf4jBridge();
	}
}