package cz.doubeon.journalviewer.parser;

import cz.doubeon.journalviewer.punits.Receipt;

public class ReceiptParser {
	private static final IItemParser[] PARSERS = new IItemParser[] {
			new GoodsItemParser(),
			new DateTimeItemParser(),
			new ReceiptNumberItemParser(),
			new TotalCashItemParser(),
			new TotalCardItemParser(),
			new ItemWithPriceParser(),
			new UnidentifiedItemParser() };

	private ReceiptParser() {
		// nop
	}

	public static void parseReceipt(Receipt receipt, ILineIterator iter) {
		final ParserContext ctx = new ParserContext();
		while (!ctx.lastItem && iter.hasNext()) {
			final String raw = iter.next();
			ctx.enabledParsing = true;
			for (final IItemParser parser : PARSERS) {
				parser.tryToUpdate(receipt, raw, ctx);
				if (ctx.lastItem || !ctx.enabledParsing) {
					break;
				}
			}
		}
	}

	private static class ParserContext implements IParserContext {
		private boolean enabledParsing;
		private boolean lastItem;

		private final IItemCounter counter = new IItemCounter() {
			int i = 1;

			@Override
			public int getAndIncrement() {
				return i++;
			}
		};

		@Override
		public IItemCounter getItemCounter() {
			return counter;
		}

		@Override
		public void lastItem() {
			this.lastItem = true;
		}

		@Override
		public void stopParsing() {
			this.enabledParsing = false;
		}
	};
}
