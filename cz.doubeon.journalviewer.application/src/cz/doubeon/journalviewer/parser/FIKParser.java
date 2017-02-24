package cz.doubeon.journalviewer.parser;

import cz.doubeon.journalviewer.punits.Receipt;
import cz.doubeon.journalviewer.punits.ReceiptItem;

public class FIKParser implements IItemParser {
	@Override
	public void tryToUpdate(Receipt receipt, IParserContext ctx) {
		final String raw = ctx.getIterator().getCurrent();
		if (raw.startsWith("FIK:")) {
			final ReceiptItem item = new ReceiptItem();
			item.setText(raw.trim());
			item.setReceipt(receipt);
			receipt.getReceiptItems().add(item);
			item.setItemOrder(ctx.getCounterAndIncrement());
			ctx.stopParsing();
		}
	}
}