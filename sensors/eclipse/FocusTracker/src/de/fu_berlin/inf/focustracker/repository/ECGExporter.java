package de.fu_berlin.inf.focustracker.repository;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jdt.core.IJavaElement;

import de.fu_berlin.inf.focustracker.FocusTrackerPlugin;
import de.fu_berlin.inf.focustracker.ui.preferences.PreferenceConstants;

public class ECGExporter implements IPropertyChangeListener {

	private String username;
	private double minProbabilityForApperance;
	private double minProbabilityForDisapperance;
	private InteractionRepository interactionRepository;
	private Set<IJavaElement> currentlyExportedElements;
	
	public ECGExporter() {
        this.username = System.getenv("username");

        if (this.username == null || this.username.equals("")) {
            this.username = "n.a.";
        }
        minProbabilityForApperance = getMinProbabilityForApperance();
        interactionRepository = InteractionRepository.getInstance();
        currentlyExportedElements = new HashSet<IJavaElement>();
	}
	
	
	public void export(IJavaElement aJavaElement, double aRating, boolean aIsInFocus) {
		
		String data = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
			+ this.username
			+ "</username><projectname>"
			+ aJavaElement.getJavaProject().getProject().getName()		            
			+ "</projectname></commonData><focustracker><element>" 
			+ aJavaElement.getElementName()
			+ "</element><focus>" 
			+ aIsInFocus
			+ "</focus><rating>"
			+ aRating
			+ "</rating></focustracker></microActivity>";
		
		System.err.println(data);
		
//		ECGEclipseSensor.getInstance().processActivity(
//        "msdt.focustracker.xsd",
//        data);
	}
	
	public void exportCurrentInteractions() {

		System.err.println("ECG Export: -----------------------");
		for (Element element : interactionRepository.getElements().values()) {
			if(element.getRating() >= minProbabilityForApperance) {
				// notify the ecg, that this element has (gained) focus
				currentlyExportedElements.add(element.getJavaElement());
				export(element.getJavaElement(), element.getRating(), true);
			} else if(element.getRating() <= minProbabilityForDisapperance && currentlyExportedElements.contains(element.getJavaElement())) {
				// notify the ecg, that this element isn't focussed anymore
				currentlyExportedElements.remove(element.getJavaElement());
				export(element.getJavaElement(), element.getRating(), false);
			}
		}
		
	}

	public void propertyChange(PropertyChangeEvent aEvent) {
		if(PreferenceConstants.P_ECG_EXPORT_MIN_PROBABILITY_FOR_APPEARANCE.equals(aEvent.getProperty())) {
			minProbabilityForApperance = getMinProbabilityForApperance();
		} else if (PreferenceConstants.P_ECG_EXPORT_MIN_PROBABILITY_FOR_DISAPPEARANCE.equals(aEvent.getProperty())) {
			minProbabilityForDisapperance = getMinProbabilityForDisapperance();
		}
	}
	
	private double getMinProbabilityForApperance() {
		return FocusTrackerPlugin.getDefault().getPluginPreferences().getDouble(PreferenceConstants.P_ECG_EXPORT_MIN_PROBABILITY_FOR_APPEARANCE);
	}
	private double getMinProbabilityForDisapperance() {
		return FocusTrackerPlugin.getDefault().getPluginPreferences().getDouble(PreferenceConstants.P_ECG_EXPORT_MIN_PROBABILITY_FOR_DISAPPEARANCE);
	}

}
