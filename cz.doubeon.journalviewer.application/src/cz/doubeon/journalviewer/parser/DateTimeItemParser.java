package cz.doubeon.journalviewer.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.doubeon.journalviewer.punits.Receipt;

class DateTimeItemParser implements IItemParser {
	private static final SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yy HH:mm");
	private static final String PATTERN = "^\\s?\\d{1,2}- ?\\d{1,2}- ?\\d{1,2} \\d  ?\\d{1,2}:\\d{2}\\s+$";

	private static Date parseDate(String text) {
		final String dateString = text.substring(0, 8).replace(' ', '0') +
				" " + text.substring(11, 16).replace(' ', '0');
		Date date;
		try {
			date = ft.parse(dateString);
		} catch (final ParseException e) {
			date = null;
		}
		return date;
	}

	@Override
	public void tryToUpdate(Receipt receipt, String raw, IParserContext ctx) {
		if (!raw.matches(PATTERN)) {
			return;
		}
		final Date date = parseDate(raw);
		// TODO if null -> log it!!!add logger to context?!
		if (date != null) {
			receipt.setDate(date);
			ctx.stopParsing();
		}
	}
}
