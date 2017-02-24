package cz.doubeon.journalviewer.parser;

import cz.doubeon.journalviewer.punits.Receipt;

interface IItemParser {
	void tryToUpdate(Receipt receipt, IParserContext ctx);
}
