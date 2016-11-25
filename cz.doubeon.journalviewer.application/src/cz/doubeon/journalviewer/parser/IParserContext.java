package cz.doubeon.journalviewer.parser;

public interface IParserContext {
	IItemCounter getItemCounter();

	void stopParsing();

	void lastItem();
}
