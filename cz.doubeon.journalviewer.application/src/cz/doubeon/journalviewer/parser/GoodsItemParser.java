package cz.doubeon.journalviewer.parser;

import java.math.BigDecimal;

import cz.doubeon.journalviewer.punits.Receipt;
import cz.doubeon.journalviewer.punits.ReceiptItem;

class GoodsItemParser implements IItemParser {
	private static final String PATTERN = "^(-?\\d+\\.?\\d*,?\\d*) (.+) (-?\\d+\\.?\\d*,?\\d*)$";

	@Override
	public void tryToUpdate(Receipt receipt, String raw, IParserContext ctx) {
		if (!raw.matches(PATTERN)) {
			return;
		}
		final BigDecimal cena = new BigDecimal(raw.replaceAll(PATTERN, "$3").replace(".", "").replace(',', '.'));
		final BigDecimal mnoz = new BigDecimal(raw.replaceAll(PATTERN, "$1"));
		final String text = raw.replaceAll(PATTERN, "$2");
		final ReceiptItem item = new ReceiptItem();
		item.setQuantity(mnoz);
		item.setText(text);
		item.setTotal(cena);
		item.setReceipt(receipt);
		receipt.getReceiptItems().add(item);
		item.setItemOrder(ctx.getItemCounter().getAndIncrement());
		ctx.stopParsing();
	}
}
