package cz.doubeon.journalviewer.part;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import cz.doubeon.journalviewer.punits.Receipt;
import cz.doubeon.journalviewer.punits.ReceiptItem;
import cz.doubeon.journalviewer.utils.AppConstants;
import cz.doubeon.journalviewer.utils.Formatters;

public class ReceiptDetailPart {
	private Consumer<List<ReceiptItem>> itemSetter = (items) -> {
		//
	};
	private Runnable onFocus = () -> {
		//
	};

	@Inject
	public void bindReceipt(@Named(AppConstants.CTX_RECEIPT_OBSERVABLE) IObservableValue<Receipt> receipt) {
		receipt.addValueChangeListener(event -> {
			final Receipt rcp = event.diff.getNewValue();
			itemSetter.accept(rcp == null ? Collections.emptyList() : rcp.getReceiptItems());
		});
	}

	@PostConstruct
	public void createUI(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TableViewer viewer = new TableViewer(container,
				SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		final Table table = viewer.getTable();
		final GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// text
		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText("Text");
		col.getColumn().setWidth(100);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final ReceiptItem item = (ReceiptItem) element;
				return item.getText() != null ? item.getText() : "";
			}
		});

		// mnozstvi
		col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText("Množství");
		col.getColumn().setWidth(60);
		col.getColumn().setAlignment(SWT.RIGHT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final ReceiptItem item = (ReceiptItem) element;
				return item.getQuantity() != null ? Formatters.QUANTITY.get().format(item.getQuantity()) : "";
			}
		});

		// cena
		col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText("Cena");
		col.getColumn().setWidth(70);
		col.getColumn().setAlignment(SWT.RIGHT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final ReceiptItem item = (ReceiptItem) element;
				return item.getTotal() != null ? Formatters.CURRENCY.get().format(item.getTotal()) : "";
			}

			@Override
			public Color getBackground(Object element) {
				final ReceiptItem item = (ReceiptItem) element;
				if (item.getTotal() != null && item.getTotal().intValue() < 0) {
					return parent.getDisplay().getSystemColor(SWT.COLOR_RED);
				}
				return super.getBackground(element);
			}
		});
		//
		itemSetter = items -> viewer.setInput(items);
		onFocus = () -> viewer.getControl().setFocus();
	}

	public void focus() {
		onFocus.run();
	}
}
