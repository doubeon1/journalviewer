package cz.doubeon.journalviewer.parser;

import cz.doubeon.journalviewer.punits.Receipt;

class ReceiptNumberItemParser implements IItemParser {
	private static final String PATTERN = "^ #\\d{6} \\d{3} \\d{3} $";

	@Override
	public void tryToUpdate(Receipt receipt, String raw, IParserContext ctx) {
		if (!raw.matches(PATTERN)) {
			return;
		}
		receipt.setNumber(raw.substring(2, 8));
		ctx.lastItem();
		ctx.stopParsing();
	}
}
