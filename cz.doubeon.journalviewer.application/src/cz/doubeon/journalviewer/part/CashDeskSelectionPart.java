package cz.doubeon.journalviewer.part;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import cz.doubeon.journalviewer.AppConstants;
import cz.doubeon.journalviewer.punits.CashDesk;
import cz.doubeon.journalviewer.utils.CDateTimeObservableValue;

public class CashDeskSelectionPart {
	@Inject
	private IEclipseContext context;

	private Consumer<List<CashDesk>> updater = (cds) -> {
		//
	};
	private Runnable onFocus = () -> {
		//
	};

	@Inject
	@Optional
	public void setCashDesks(@UIEventTopic(AppConstants.TOPIC_CASHDESKS_UPDATED) List<CashDesk> cashDesks) {
		updater.accept(cashDesks);
	}

	@PostConstruct
	public void createUI(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		//
		final Label lblCds = new Label(container, SWT.NONE);
		lblCds.setText("Pokladna");
		final Label lblDate = new Label(container, SWT.NONE);
		lblDate.setText("Žurnál ze dne");
		// cash desks
		final Combo combo = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		final ComboViewer cashViewer = new ComboViewer(combo);
		cashViewer.setContentProvider(ArrayContentProvider.getInstance());
		cashViewer.setLabelProvider(new CdLblProvider());
		// date
		final CDateTime dateWidget = new CDateTime(container, CDT.DROP_DOWN | CDT.BORDER);
		dateWidget.setPattern("EEEE, d. MMMM YYYY");
		final GridData gdDateWidget = new GridData(SWT.FILL, SWT.FILL, false, false);
		gdDateWidget.widthHint = 200;
		dateWidget.setLayoutData(gdDateWidget);
		//
		context.modify(AppConstants.CTX_CASHDESK_OBSERVABLE,
				ViewersObservables.observeSingleSelection(cashViewer));
		//
		final IObservableValue<Date> dateTimeObservable = new CDateTimeObservableValue(dateWidget);
		dateTimeObservable.setValue(new Date());
		context.modify(AppConstants.CTX_DATE_OBSERVABLE, dateTimeObservable);
		//
		updater = cds -> cashViewer.setInput(cds);
		onFocus = () -> cashViewer.getControl().setFocus();
	}

	@Focus
	public void setFocus() {
		onFocus.run();
	}

	private static class CdLblProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof CashDesk) {
				final CashDesk cd = (CashDesk) element;
				return cd.getDescription();
			}
			return super.getText(element);
		}
	}
}
