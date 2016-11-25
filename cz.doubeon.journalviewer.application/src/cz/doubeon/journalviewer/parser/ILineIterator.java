package cz.doubeon.journalviewer.parser;

public interface ILineIterator extends AutoCloseable {
	boolean hasNext();

	String next();
}
