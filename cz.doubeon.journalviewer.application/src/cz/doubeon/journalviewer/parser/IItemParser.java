package cz.doubeon.journalviewer.parser;

import cz.doubeon.journalviewer.punits.Receipt;

interface IItemParser {
	void tryToUpdate(Receipt receipt, String raw, IParserContext ctx);
}
