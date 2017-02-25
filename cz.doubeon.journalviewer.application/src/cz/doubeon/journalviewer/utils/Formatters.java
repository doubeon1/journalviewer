package cz.doubeon.journalviewer.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class Formatters {
	private Formatters() {
		// util
	}

	public static final ThreadLocal<SimpleDateFormat> DATE = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("EEE dd.MM.yyyy HH:mm");
		}
	};

	public static final ThreadLocal<NumberFormat> CURRENCY = new ThreadLocal<NumberFormat>() {
		@Override
		protected NumberFormat initialValue() {
			return new DecimalFormat("#,###,##0.00");
		}
	};

	public static final ThreadLocal<NumberFormat> QUANTITY = new ThreadLocal<NumberFormat>() {
		@Override
		protected NumberFormat initialValue() {
			return new DecimalFormat("0.00");
		}
	};

}
