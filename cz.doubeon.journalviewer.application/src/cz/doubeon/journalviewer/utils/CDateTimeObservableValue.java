package cz.doubeon.journalviewer.utils;
import java.util.Date;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * The Class CDateTimeObservableValue.
 */
public class CDateTimeObservableValue extends AbstractObservableValue<Date> {

	/** The Control being observed here. */
	protected final CDateTime cDateTime;

	/** Flag to prevent infinite recursion in {@link #doSetValue(Object)}. */
	protected boolean updating = false;

	/** The "old" selection before a selection event is fired. */
	protected Date currentSelection;

	/** Old value. */
	private Date oldValue;

	/**
	 * Instantiates a new c date time observable value.
	 * 
	 * @param cDateTime
	 *            the c date time
	 */
	public CDateTimeObservableValue(CDateTime cDateTime) {
		this.cDateTime = cDateTime;
		this.currentSelection = cDateTime.getSelection();

		cDateTime.addDisposeListener(disposeListener);
		cDateTime.addListener(SWT.Modify, updateListener);
	}

	/** The dispose listener. */
	private final DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			CDateTimeObservableValue.this.dispose();
		}
	};

	/** The update listener. */
	private final Listener updateListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			if (!updating) {
				final Date newValue = cDateTime.getSelection();
				if (newValue == null)
					return;
				if (!newValue.equals(oldValue)) {
					fireValueChange(Diffs.createValueDiff(oldValue, newValue));
					oldValue = newValue;
				}
			}
		}
	};

	@Override
	protected Date doGetValue() {
		return cDateTime.getSelection();
	}

	@Override
	public Object getValueType() {
		return Date.class;
	}

	@Override
	protected void doSetValue(Date newValue) {
		try {
			updating = true;
			oldValue = cDateTime.getSelection();
			cDateTime.setSelection(newValue);
			currentSelection = newValue;
			fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		} finally {
			updating = false;
		}
	}

}