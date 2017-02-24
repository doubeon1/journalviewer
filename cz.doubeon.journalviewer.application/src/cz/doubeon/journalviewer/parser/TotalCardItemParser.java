package cz.doubeon.journalviewer.parser;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.doubeon.journalviewer.punits.PaymentType;
import cz.doubeon.journalviewer.punits.Receipt;

class TotalCardItemParser implements IItemParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(TotalCardItemParser.class);
	private static final String PATTERN = "^\\s{2}(Kart.*) (-?\\d+\\.?\\d*,?\\d*)$";

	@Override
	public void tryToUpdate(Receipt receipt, IParserContext ctx) {
		final String raw = ctx.getIterator().getCurrent();
		if (!raw.matches(PATTERN)) {
			return;
		}
		try {
			final BigDecimal cena = new BigDecimal(
					raw.replaceAll(PATTERN, "$2").replace(".", "").replace(',', '.'));
			receipt.setTotal(cena);
			receipt.setPaymentType(PaymentType.CARD);
		} catch (final NumberFormatException ex) {
			LOGGER.warn("Error parsing card price item: '" + raw + "'", ex);
		}
		// pokracuj, radek musi zapsat i ItemWithPriceParser kvuli kontrole!
	}
}
