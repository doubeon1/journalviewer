package cz.doubeon.journalviewer.parser;

import cz.doubeon.journalviewer.punits.Receipt;
import cz.doubeon.journalviewer.punits.ReceiptItem;

public class BKPParser implements IItemParser {
	@Override
	public void tryToUpdate(Receipt receipt, IParserContext ctx) {
		final IBufferedLineIterator it = ctx.getIterator();
		final String raw = it.getCurrent();
		if (raw.startsWith("BKP:") && it.hasNext()) {
			it.next();
			final String raw2 = it.getCurrent();
			final ReceiptItem item = new ReceiptItem();
			item.setText((raw + raw2).trim());
			item.setReceipt(receipt);
			receipt.getReceiptItems().add(item);
			item.setItemOrder(ctx.getCounterAndIncrement());
			ctx.stopParsing();
		}
	}
}
