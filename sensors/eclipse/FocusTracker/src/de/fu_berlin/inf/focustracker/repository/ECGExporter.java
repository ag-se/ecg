package de.fu_berlin.inf.focustracker.repository;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jdt.core.IJavaElement;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;

import de.fu_berlin.inf.focustracker.FocusTrackerPlugin;
import de.fu_berlin.inf.focustracker.interaction.JavaElementHelper;
import de.fu_berlin.inf.focustracker.interaction.JavaElementResourceAndName;
import de.fu_berlin.inf.focustracker.interaction.SystemInteraction;
import de.fu_berlin.inf.focustracker.ui.preferences.PreferenceConstants;

public class ECGExporter implements IPropertyChangeListener {

	private String username;
	private double minProbabilityForApperance;
	private double minProbabilityForDisapperance;
	private InteractionRepository interactionRepository;
	private Set<IJavaElement> currentlyExportedElements;
	private DecimalFormat decimalFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US)); 
	
	public ECGExporter() {
        this.username = System.getenv("username");

        if (this.username == null || this.username.equals("")) {
            this.username = "n.a.";
        }
        minProbabilityForApperance = getMinProbabilityForApperance();
        minProbabilityForDisapperance = getMinProbabilityForDisapperance();
        interactionRepository = InteractionRepository.getInstance();
        currentlyExportedElements = new HashSet<IJavaElement>();
		// listen to changes of the preferences
		FocusTrackerPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);
	}
	
	
	public void export(IJavaElement aJavaElement, double aRating, boolean aIsInFocus) {
		JavaElementResourceAndName resAndName = JavaElementHelper.getRepresentation(aJavaElement);
		
		String data = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
			+ this.username
			+ "</username><projectname>"
			+ aJavaElement.getJavaProject().getProject().getName()		            
			+ "</projectname></commonData><focus><resourcename>" 
//			+ JavaElementHelper.toString(aJavaElement)
			+ resAndName.getResource()
			+ "</resourcename><element>"
			+ resAndName.getName()
			+ "</element><elementtype>" 
			+ aJavaElement.getClass().getName()
			+ "</elementtype><hasfocus>" 
			+ aIsInFocus
			+ "</hasfocus>" 
			+ (aIsInFocus ? "<rating>" + decimalFormat.format(aRating) + "</rating>" : "") // add rating only if element is in focus
			+ "</focus></microActivity>";
		
//		System.err.println(data);
		
		ECGEclipseSensor.getInstance().processActivity(
				"msdt.focus.xsd",
				data);
	}
	
	public void exportSystemInteraction(SystemInteraction aSystemInteraction) {
		
		String data = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
			+ this.username
			+ "</username></commonData><user><activity>" 
			+ aSystemInteraction.getAction()
			+ "</activity></user></microActivity>";
		
//		System.err.println(data);
		aSystemInteraction.setExported(true);
		
		ECGEclipseSensor.getInstance().processActivity(
				"msdt.user.xsd",
				data);
	}
	
	
	
	public void exportCurrentInteractions() {

		// export java interactions :
		List<Element> focussedElementsToExport = new ArrayList<Element>();
		for (Element element : interactionRepository.getElements().values()) {
			if(element.getRating() >= minProbabilityForApperance || (element.getRating() > minProbabilityForDisapperance && currentlyExportedElements.contains(element.getJavaElement())) ) {
				// notify the ecg, that this element has (gained) focus
				currentlyExportedElements.add(element.getJavaElement());
				focussedElementsToExport.add(element);
			} else if(element.getRating() < minProbabilityForDisapperance && currentlyExportedElements.contains(element.getJavaElement())) {
				// notify the ecg, that this element isn't focussed anymore
				currentlyExportedElements.remove(element.getJavaElement());
				export(element.getJavaElement(), element.getRating(), false);
			}
		}
		
		for (Element element : focussedElementsToExport) {
			export(element.getJavaElement(), normalizeRating(element.getRating(), focussedElementsToExport.size()), true);
		}
		
		// export system interactions 
		for (SystemInteraction interaction : interactionRepository.getSystemInteractions()) {
			if(!interaction.isExported()) {
				exportSystemInteraction(interaction);
			}
		} 
		
	}

	private double normalizeRating(double aRating, int aNumberOfElements) {
//		return aRating / aNumberOfElements;
		return aRating;
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
