package de.fu_berlin.inf.focustracker.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.Job;

import de.fu_berlin.inf.focustracker.FocusTrackerPlugin;
import de.fu_berlin.inf.focustracker.repository.ECGExporter;
import de.fu_berlin.inf.focustracker.ui.preferences.PreferenceConstants;
import de.fu_berlin.inf.focustracker.util.Units;

public class ECGExportJob extends Job implements IPropertyChangeListener {

	private static long delay = getExportInterval();
	private static ECGExporter ecgExporter = new ECGExporter();
	
	public ECGExportJob() {
		super("ECG Export");
		// listen to changes of the preferences
		FocusTrackerPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);
	}

	@Override
	protected IStatus run(IProgressMonitor aMonitor) {
		
		try {
			ecgExporter.exportCurrentInteractions();
			schedule(delay);
			return Status.OK_STATUS;
		} catch (NoClassDefFoundError e) {
			System.err.println("ECG Sensor not found! Cancelling " + getName());
			return Status.CANCEL_STATUS;
		}
	}

	public void propertyChange(PropertyChangeEvent aEvent) {
		if(PreferenceConstants.P_ECG_EXPORT_INTERVAL.equals(aEvent.getProperty())) {
			delay = getExportInterval();
			this.cancel();
			schedule(delay);
		}
	}

	public static long getExportInterval() {
		return Units.SECOND * FocusTrackerPlugin.getDefault().getPluginPreferences().getInt(PreferenceConstants.P_ECG_EXPORT_INTERVAL);
	}
	
}
