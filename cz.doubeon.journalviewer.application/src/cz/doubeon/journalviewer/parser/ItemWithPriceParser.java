package cz.doubeon.journalviewer.parser;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.doubeon.journalviewer.punits.Receipt;
import cz.doubeon.journalviewer.punits.ReceiptItem;

class ItemWithPriceParser implements IItemParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(ItemWithPriceParser.class);
	private static final String PATTERN = "^\\s{2}(.*) (-?\\d+\\.?\\d*,?\\d*)$";

	@Override
	public void tryToUpdate(Receipt receipt, IParserContext ctx) {
		final String raw = ctx.getIterator().getCurrent();
		if (!raw.matches(PATTERN)) {
			return;
		}
		try {
			final BigDecimal cena = new BigDecimal(
					raw.replaceAll(PATTERN, "$2").replace(".", "").replace(',', '.'));
			final String text = raw.replaceAll(PATTERN, "$1").trim();
			final ReceiptItem item = new ReceiptItem();
			item.setText(text);
			item.setTotal(cena);
			item.setReceipt(receipt);
			receipt.getReceiptItems().add(item);
			item.setItemOrder(ctx.getCounterAndIncrement());
			ctx.stopParsing();
		} catch (final NumberFormatException ex) {
			LOGGER.warn("Error parsing price item: '" + raw + "'", ex);
		}
	}
}
