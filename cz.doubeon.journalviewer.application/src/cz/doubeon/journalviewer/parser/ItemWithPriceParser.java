package cz.doubeon.journalviewer.parser;

import java.math.BigDecimal;

import cz.doubeon.journalviewer.punits.Receipt;
import cz.doubeon.journalviewer.punits.ReceiptItem;

class ItemWithPriceParser implements IItemParser {
	private static final String PATTERN = "^\\s{2}(.*) (-?\\d+\\.?\\d*,?\\d*)$";

	@Override
	public void tryToUpdate(Receipt receipt, String raw, IParserContext ctx) {
		if (!raw.matches(PATTERN)) {
			return;
		}
		final BigDecimal cena = new BigDecimal(
				raw.replaceAll(PATTERN, "$2").replace(".", "").replace(',', '.'));
		final String text = raw.replaceAll(PATTERN, "$1").trim();
		final ReceiptItem item = new ReceiptItem();
		item.setText(text);
		item.setTotal(cena);
		item.setReceipt(receipt);
		receipt.getReceiptItems().add(item);
		item.setItemOrder(ctx.getItemCounter().getAndIncrement());
		ctx.stopParsing();
	}
}
