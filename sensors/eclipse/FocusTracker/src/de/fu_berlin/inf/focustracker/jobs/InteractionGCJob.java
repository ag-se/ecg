package de.fu_berlin.inf.focustracker.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.fu_berlin.inf.focustracker.repository.InteractionRepository;

public class InteractionGCJob extends Job {

	public static long DELAY = 5000;
	private static long DELETE_OLDER_THAN = 1000 * 60;
	private static InteractionRepository interactionRepository = InteractionRepository.getInstance();
	
	public InteractionGCJob() {
		super("Interaction GC");
	}

	@Override
	protected IStatus run(IProgressMonitor aMonitor) {
		
		System.err.println("running: " + getName());
		int removed = interactionRepository.removeInteractions(System.currentTimeMillis() - DELETE_OLDER_THAN);
		if(removed > 0) {
			System.err.println("Removed " + removed + " interactions!");
		}
		schedule(DELAY);
		return Status.OK_STATUS;
	}

}
