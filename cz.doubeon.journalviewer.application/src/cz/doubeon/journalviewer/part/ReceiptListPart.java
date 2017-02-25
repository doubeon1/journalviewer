package cz.doubeon.journalviewer.part;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import cz.doubeon.journalviewer.punits.CashDesk;
import cz.doubeon.journalviewer.punits.PaymentType;
import cz.doubeon.journalviewer.punits.Receipt;
import cz.doubeon.journalviewer.services.CashDeskService;
import cz.doubeon.journalviewer.utils.AppConstants;
import cz.doubeon.journalviewer.utils.Formatters;

public class ReceiptListPart {
	private static final String NOT_AVAILABLE = "n/a";

	private IObservableValue<CashDesk> oCashDesk;
	private IObservableValue<Date> oDate;
	private Runnable reloader = () -> {
		//
	};
	private Runnable onFocus = () -> {
		//
	};

	@Inject
	private CashDeskService cashDeskService;

	@Inject
	private IEclipseContext context;

	@Inject
	public void bindCashDesk(@Named(AppConstants.CTX_CASHDESK_OBSERVABLE) IObservableValue<CashDesk> cashDesk) {
		this.oCashDesk = cashDesk;
		cashDesk.addValueChangeListener(event -> reloader.run());
	}

	@Inject
	public void bindDate(@Named(AppConstants.CTX_DATE_OBSERVABLE) IObservableValue<Date> date) {
		this.oDate = date;
		date.addValueChangeListener(event -> reloader.run());
	}

	private List<Receipt> getReceipts() {
		if (oCashDesk == null || oDate == null) {
			return Collections.emptyList();
		}
		final CashDesk cashDesk = oCashDesk.getValue();
		final Date date = oDate.getValue();
		if (cashDesk == null || date == null) {
			return Collections.emptyList();
		}
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		// from
		final Date from = cal.getTime();
		cal.add(Calendar.MILLISECOND, (int) TimeUnit.DAYS.toMillis(1) - 1);

		// to
		final Date to = cal.getTime();
		return cashDeskService.getReceipts(cashDesk, from, to);
	}

	@PostConstruct
	public void createUI(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TableViewer receiptsViewer = new TableViewer(container, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		final Table table = receiptsViewer.getTable();
		final GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		receiptsViewer.setContentProvider(ArrayContentProvider.getInstance());

		// Datum
		TableViewerColumn col = new TableViewerColumn(receiptsViewer, SWT.NONE);
		col.getColumn().setText("Datum");
		col.getColumn().setWidth(120);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final Receipt receipt = (Receipt) element;
				return receipt.getDate() != null ? Formatters.DATE.get().format(receipt.getDate()) : "";
			}
		});

		// Cislo
		col = new TableViewerColumn(receiptsViewer, SWT.NONE);
		col.getColumn().setText("Číslo");
		col.getColumn().setWidth(60);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final Receipt receipt = (Receipt) element;
				return receipt.getNumber() != null ? receipt.getNumber() : "";
			}
		});

		// Total
		col = new TableViewerColumn(receiptsViewer, SWT.NONE);
		col.getColumn().setText("Total");
		col.getColumn().setWidth(140);
		col.getColumn().setAlignment(SWT.RIGHT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final Receipt receipt = (Receipt) element;
				return receipt.getTotal() != null ? Formatters.CURRENCY.get().format(receipt.getTotal())
						: NOT_AVAILABLE;
			}

			@Override
			public Color getBackground(Object element) {
				final Receipt rcpt = (Receipt) element;
				final Color color;
				if (rcpt.getTotal() == null) {
					color = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
				} else if (rcpt.getPaymentType() == PaymentType.CARD) {
					color = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
				} else if (rcpt.getTotal() != null && rcpt.getTotal().intValue() < 0) {
					color = Display.getDefault().getSystemColor(SWT.COLOR_RED);
				} else {
					color = super.getBackground(element);
				}
				return color;
			}
		});
		final Composite comp = new Composite(container, SWT.BORDER);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		final Label lbl = new Label(comp, SWT.NONE);
		lbl.setText("Celkem za den: ");
		final Label lblTotal = new Label(comp, SWT.NONE);
		lblTotal.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		//
		context.modify(AppConstants.CTX_RECEIPT_OBSERVABLE,
				ViewersObservables.observeSingleSelection(receiptsViewer));
		//
		reloader = () -> {
			final List<Receipt> rcpts = getReceipts();
			receiptsViewer.setInput(rcpts);
				BigDecimal total = BigDecimal.ZERO;
				for (final Receipt rcpt : rcpts) {
					total = total.add(rcpt.getTotal() != null ? rcpt.getTotal() : BigDecimal.ZERO);
				}
				lblTotal.setText(Formatters.CURRENCY.get().format(total) + " Kč");
		};
		onFocus = () -> receiptsViewer.getControl().setFocus();
	}

	@Focus
	public void setFocus() {
		onFocus.run();
	}

}
