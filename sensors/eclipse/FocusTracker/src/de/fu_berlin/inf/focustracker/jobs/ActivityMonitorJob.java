package de.fu_berlin.inf.focustracker.jobs;

import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.Job;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.FocusTrackerPlugin;
import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.Origin;
import de.fu_berlin.inf.focustracker.interaction.SystemInteraction;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;
import de.fu_berlin.inf.focustracker.ui.preferences.PreferenceConstants;
import de.fu_berlin.inf.focustracker.util.Units;


public class ActivityMonitorJob extends Job implements IPropertyChangeListener {

	public long inactivityDetectionTimeout = getInactivityDetectionTimeout();
	private static final long DELAY = Units.SECOND;
	
	private long inactivityDetectedTimestamp = 0;
	private boolean currentlyInactive = false;
	
	int i = 0;
	
	public ActivityMonitorJob() {
		super("Activity Monitor");
		// listen to changes of the preferences
		FocusTrackerPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);
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
		if(tmpInactivityTimestamp < System.currentTimeMillis() - inactivityDetectionTimeout &&
				inactivityDetectedTimestamp != tmpInactivityTimestamp) {
			inactivityDetectedTimestamp = tmpInactivityTimestamp;
			System.err.println("Inactivity detected !");
			SystemInteraction interaction = new SystemInteraction(Action.USER_INACTIVE, 1d, new Date(), null, Origin.ACTIVITY_MANAGER);
			EventDispatcher.getInstance().notifyInteractionObserved(interaction);
			
			currentlyInactive = true;
		} else if (currentlyInactive == true && inactivityDetectedTimestamp != tmpInactivityTimestamp) {
				// TODO this is a imprecise value, since activity could have been recorded before 
				System.err.println("Activity after inactivity detected ! Active again since: " + tmpInactivityTimestamp);
				currentlyInactive = false;
				SystemInteraction interaction = new SystemInteraction(Action.USER_ACTIVE, 1d, new Date(), null, Origin.ACTIVITY_MANAGER);
				EventDispatcher.getInstance().notifyInteractionObserved(interaction);
		}
		
		InteractionRepository.getInstance().calculateInactivities();
		
		schedule(DELAY);
		return Status.OK_STATUS;
	}

	public void propertyChange(PropertyChangeEvent aEvent) {
		if(PreferenceConstants.P_USER_INACTIVITY_DETECTION_TIMEOUT.equals(aEvent.getProperty())) {
			inactivityDetectionTimeout = getInactivityDetectionTimeout();
		}
	}

	public static long getInactivityDetectionTimeout() {
		return Units.SECOND * FocusTrackerPlugin.getDefault().getPluginPreferences().getInt(PreferenceConstants.P_USER_INACTIVITY_DETECTION_TIMEOUT);
	}
	

	
}
