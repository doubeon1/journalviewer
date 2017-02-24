package cz.doubeon.journalviewer.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.doubeon.journalviewer.punits.Receipt;

class DateTimeItemParser implements IItemParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeItemParser.class);
	private static final SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yy HH:mm");
	private static final String PATTERN = "^\\s?\\d{1,2}- ?\\d{1,2}- ?\\d{1,2} \\d  ?\\d{1,2}:\\d{2}\\s+$";

	private static Date parseDate(String text) throws ParseException {
		final String dateString = text.substring(0, 8).replace(' ', '0') +
				" " + text.substring(11, 16).replace(' ', '0');
		return ft.parse(dateString);
	}

	@Override
	public void tryToUpdate(Receipt receipt, IParserContext ctx) {
		final String raw = ctx.getIterator().getCurrent();
		if (!raw.matches(PATTERN)) {
			return;
		}
		try {
			final Date date = parseDate(raw);
			receipt.setDate(date);
			ctx.stopParsing();
		} catch (final ParseException ex) {
			LOGGER.warn("Error parsing date item: '" + raw + "'", ex);
		}
	}
}
