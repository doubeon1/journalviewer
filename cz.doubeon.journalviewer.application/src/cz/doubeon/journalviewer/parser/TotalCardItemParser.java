package cz.doubeon.journalviewer.parser;

import java.math.BigDecimal;

import cz.doubeon.journalviewer.punits.PaymentType;
import cz.doubeon.journalviewer.punits.Receipt;

class TotalCardItemParser implements IItemParser {
	private static final String PATTERN = "^\\s{2}(Kart.*) (-?\\d+\\.?\\d*,?\\d*)$";

	@Override
	public void tryToUpdate(Receipt receipt, String raw, IParserContext ctx) {
		if (!raw.matches(PATTERN)) {
			return;
		}
		final BigDecimal cena = new BigDecimal(
				raw.replaceAll(PATTERN, "$2").replace(".", "").replace(',', '.'));
		receipt.setTotal(cena);
		receipt.setPaymentType(PaymentType.CARD);
		// radek musi zapsat i ItemWithPriceParser kvuli kontrole!
	}
}