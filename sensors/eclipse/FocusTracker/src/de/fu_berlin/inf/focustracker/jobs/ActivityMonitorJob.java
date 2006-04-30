package de.fu_berlin.inf.focustracker.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.fu_berlin.inf.focustracker.EventDispatcher;


public class ActivityMonitorJob extends Job {

	public static final long INACTIVITY_DELAY = 5000;
	private static final int DELAY = 1000;
//	private static final int DELAY_NOP = 5000;
//	private static final int DELAY_LOSING_FOCUS = 5000;
	
	private long inactivityDetectedTimestamp = 0;
	private boolean currentlyInactive = false;
	
	int i = 0;
	
	public ActivityMonitorJob() {
		super("Activity Monitor");
	}
	
	@Override
	protected IStatus run(IProgressMonitor aMonitor) {
		
//		System.out.println("checking for a timeout... ");
//		long currentTime = System.currentTimeMillis();
//		if(poiList.size() > 0 &&  
//				poiList.get(poiList.size()-1).getTimeStamp().getTime() < (currentTime - NOP_DELAY) &&
//				(lastTimeOut == null || lastTimeOut.getNumberOfElements() != poiList.size()) 
//			) {
//			System.err.println("timeout detected ! Last activity : " + poiList.get(poiList.size() - 1).toString());
////			Activator.getDefault().getLog().log(new Status())
//			lastTimeOut = new TimeOutEvent(poiList.size(), new Timestamp(currentTime));
//		}
//		System.err.println(i++ + " - " + System.currentTimeMillis());
		
		
		
//		InteractionRepository repository = InteractionRepository.getInstance();
//		
//		// iterate through all java elements
//		for (Element lastVisitedJavaElement : repository.getElements().values()) {
//			if (lastVisitedJavaElement != null) {
//				JavaInteraction lastInteraction = lastVisitedJavaElement.getLastInteraction();
//				if (lastInteraction.getDate().getTime() < currentTime - DELAY_LOSING_FOCUS && lastInteraction.getSeverity() > 0) {
//					// losing focus
//					double newSeverity = SeverityHelper.calculateSeverity(Action.LOSING_FOCUS, lastInteraction.getSeverity());
//					JavaInteraction javaInteraction = new JavaInteraction(Action.LOSING_FOCUS, lastVisitedJavaElement.getJavaElement(), newSeverity, new Date(System.currentTimeMillis()), null, Origin.MONITOR_ACTIVIY);
//					EventDispatcher.getInstance().notifyInteractionObserved(javaInteraction);
//				}
//			}
//		}
		
		long tmpInactivityTimestamp = EventDispatcher.getInstance().getSystemMonitor().getLastActivityTimestamp();
		if(tmpInactivityTimestamp < System.currentTimeMillis() - INACTIVITY_DELAY &&
				inactivityDetectedTimestamp != tmpInactivityTimestamp) {
			inactivityDetectedTimestamp = tmpInactivityTimestamp;
			System.err.println("Inactivity detected !");
			currentlyInactive = true;
		} else if (currentlyInactive == true && inactivityDetectedTimestamp != tmpInactivityTimestamp) {
				// TODO this is a imprecise value, since activity could have been recorded before 
				System.err.println("Activity after inactivity detected ! Active again since: " + tmpInactivityTimestamp);
				currentlyInactive = false;
		}
		
		schedule(DELAY);
		return Status.OK_STATUS;
	}

}
