package cz.doubeon.journalviewer.parser;

import cz.doubeon.journalviewer.punits.Receipt;
import cz.doubeon.journalviewer.punits.ReceiptItem;

class UnidentifiedItemParser implements IItemParser {
	@Override
	public void tryToUpdate(Receipt receipt, String raw, IParserContext ctx) {
		final ReceiptItem item = new ReceiptItem();
		item.setText(raw);
		item.setReceipt(receipt);
		receipt.getReceiptItems().add(item);
		item.setItemOrder(ctx.getItemCounter().getAndIncrement());
		ctx.stopParsing();
	}
}
