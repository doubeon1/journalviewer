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
			new BKPParser(),
			new FIKParser(),
			new UnidentifiedItemParser() };

	private ReceiptParser() {
		// nop
	}

	public static void parseReceipt(Receipt receipt, ILineIterator iter) {
		final ParserContext ctx = new ParserContext(iter);
		final IBufferedLineIterator bit = ctx.getIterator();
		while (!ctx.lastItem && bit.hasNext()) {
			bit.next();
			ctx.enabledParsing = true;
			for (final IItemParser parser : PARSERS) {
				parser.tryToUpdate(receipt, ctx);
				if (ctx.lastItem || !ctx.enabledParsing) {
					break;
				}
			}
		}
	}

	private static class ParserContext implements IParserContext {
		private boolean enabledParsing;
		private boolean lastItem;
		private int counter = 1;
		private final IBufferedLineIterator bit;

		ParserContext(ILineIterator it) {
			this.bit = new IBufferedLineIterator() {
				private String curr;

				@Override
				public void next() {
					curr = it.next();
				}

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public String getCurrent() {
					return curr;
				}
			};
		}

		@Override
		public int getCounterAndIncrement() {
			return counter++;
		}

		@Override
		public void lastItem() {
			this.lastItem = true;
		}

		@Override
		public void stopParsing() {
			this.enabledParsing = false;
		}

		@Override
		public IBufferedLineIterator getIterator() {
			return bit;
		}

	};
}
