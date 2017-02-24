package cz.doubeon.journalviewer.parser;

public interface IBufferedLineIterator {
	String getCurrent();

	boolean hasNext();

	void next();
}
