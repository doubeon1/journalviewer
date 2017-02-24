package cz.doubeon.journalviewer.parser;

public interface IParserContext {
	int getCounterAndIncrement();

	void stopParsing();

	void lastItem();

	IBufferedLineIterator getIterator();
}
