package de.fu_berlin.inf.focustracker.jobs;

import java.sql.Timestamp;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.SeverityHelper;
import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.interaction.Origin;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;


public class ActivityMonitorJob extends Job {

	private static int DELAY = 1000;
	private static int DELAY_NOP = 5000;
	private static int DELAY_LOSING_FOCUS = 5000;
	private TimeOutEvent lastTimeOut = null;
	
	int i = 0;
	
	public ActivityMonitorJob() {
		super("Activity Monitor");
	}
	
	@Override
	protected IStatus run(IProgressMonitor aMonitor) {
		
//		System.out.println("checking for a timeout... ");
		long currentTime = System.currentTimeMillis();
//		if(poiList.size() > 0 &&  
//				poiList.get(poiList.size()-1).getTimeStamp().getTime() < (currentTime - NOP_DELAY) &&
//				(lastTimeOut == null || lastTimeOut.getNumberOfElements() != poiList.size()) 
//			) {
//			System.err.println("timeout detected ! Last activity : " + poiList.get(poiList.size() - 1).toString());
////			Activator.getDefault().getLog().log(new Status())
//			lastTimeOut = new TimeOutEvent(poiList.size(), new Timestamp(currentTime));
//		}
//		System.err.println(i++ + " - " + System.currentTimeMillis());
		InteractionRepository repository = InteractionRepository.getInstance();
//		IJavaElement lastVisitedJavaElement = repository.getLastVisitedJavaElement();
		
		// iterate through all java elements
		for (IJavaElement lastVisitedJavaElement : repository.getJavaElements()) {
			if (lastVisitedJavaElement != null) {
				JavaInteraction lastInteraction = repository.getLastInteraction(lastVisitedJavaElement);
				if (lastInteraction.getDate().getTime() < currentTime - DELAY_LOSING_FOCUS && lastInteraction.getSeverity() > 0) {
					// losing focus
					double newSeverity = SeverityHelper.calculateSeverity(Action.LOSING_FOCUS, lastInteraction.getSeverity());
					JavaInteraction javaInteraction = new JavaInteraction(Action.LOSING_FOCUS, lastVisitedJavaElement, newSeverity, new Date(System.currentTimeMillis()), null, Origin.MONITOR_ACTIVIY);
					EventDispatcher.getInstance().notifyInteractionObserved(javaInteraction);
				}
			}
		}
		
		schedule(DELAY);
		return Status.OK_STATUS;
	}

	class TimeOutEvent {
		
		private int numberOfElements;
		private Timestamp timeStamp;
		
		public TimeOutEvent(int aNumberOfElements, Timestamp aTimeStamp) {
			super();
			numberOfElements = aNumberOfElements;
			timeStamp = aTimeStamp;
		}
		
		public int getNumberOfElements() {
			return numberOfElements;
		}
		public Timestamp getTimeStamp() {
			return timeStamp;
		}
		
		@Override
		public int hashCode() {
			final int PRIME = 31;
			int result = 1;
			result = PRIME * result + numberOfElements;
			result = PRIME * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final TimeOutEvent other = (TimeOutEvent) obj;
			if (numberOfElements != other.numberOfElements)
				return false;
			if (timeStamp == null) {
				if (other.timeStamp != null)
					return false;
			} else if (!timeStamp.equals(other.timeStamp))
				return false;
			return true;
		}
		
		
	}
}
