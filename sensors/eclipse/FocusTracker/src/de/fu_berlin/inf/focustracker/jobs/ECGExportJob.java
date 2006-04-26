package de.fu_berlin.inf.focustracker.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class ECGExportJob extends Job {

	private static final long DELAY = 10000;

	public ECGExportJob() {
		super("ECG Export");
	}

	@Override
	protected IStatus run(IProgressMonitor aMonitor) {
		
		System.err.println("running: " + getName());
		schedule(DELAY);
		return Status.OK_STATUS;
	}

}
