package de.fu_berlin.inf.focustracker.repository;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jdt.core.IJavaElement;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;

import de.fu_berlin.inf.focustracker.FocusTrackerPlugin;
import de.fu_berlin.inf.focustracker.interaction.JavaElementHelper;
import de.fu_berlin.inf.focustracker.interaction.JavaElementResourceAndName;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.interaction.SystemInteraction;
import de.fu_berlin.inf.focustracker.ui.preferences.PreferenceConstants;

public class ECGExporter implements IPropertyChangeListener {

	private String username;

	private double minProbabilityForApperance;

	private double minProbabilityForDisapperance;

	private InteractionRepository interactionRepository;

	private Set<IJavaElement> currentlyExportedElements;

	private DecimalFormat decimalFormat = new DecimalFormat("0.00",
			new DecimalFormatSymbols(Locale.US));

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
		FocusTrackerPlugin.getDefault().getPluginPreferences()
				.addPropertyChangeListener(this);
	}

	public void export(JavaInteraction aJavaInteraction, boolean aIsInFocus) {
		JavaElementResourceAndName resAndName = JavaElementHelper
				.getRepresentation(aJavaInteraction.getJavaElement());

		String data = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
				+ this.username
				+ "</username><projectname>"
				+ aJavaInteraction.getJavaElement().getJavaProject().getProject().getName()
				+ "</projectname></commonData><focus><resourcename>"
				// + JavaElementHelper.toString(aJavaElement)
				+ resAndName.getResource()
				+ "</resourcename><element>"
				+ resAndName.getName()
				+ "</element><elementtype>"
				+ aJavaInteraction.getJavaElement().getClass().getName()
				+ "</elementtype><hasfocus>"
				+ aIsInFocus
				+ "</hasfocus>"
				+ (aIsInFocus ? "<rating>" + decimalFormat.format(aJavaInteraction.getSeverity())
						+ "</rating>" : "") // add rating only if element is in
											// focus
				+ "<detectedtimestamp>"
				+ timestampToXMLString(aJavaInteraction.getDate())
				+ "</detectedtimestamp>" + "</focus></microActivity>";

//		System.out.println(data);
		ECGEclipseSensor.getInstance().processActivity("msdt.focus.xsd", data);
	}

	public void exportSystemInteraction(SystemInteraction aSystemInteraction) {

		String data = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
				+ this.username
				+ "</username></commonData><user><activity>"
				+ aSystemInteraction.getAction().toString().toLowerCase()
				+ "</activity></user></microActivity>";

		aSystemInteraction.setExported(true);

		ECGEclipseSensor.getInstance().processActivity("msdt.user.xsd", data);
	}

	public void exportCurrentInteractions() {

		HashMap<IJavaElement, Element> elements = interactionRepository.getElements(); 
		// export java interactions :
		try {
			synchronized (elements) {
			for (Element element : elements.values()) {
					for (JavaInteraction interaction : element.getInteractions()) {
						if (!interaction.isExported()) {
							if(interaction.getLastInteraction() == null || 
									interaction.getSeverity() != interaction.getLastInteraction().getSeverity()) {
								boolean isInFocus = interaction.getSeverity() >= minProbabilityForApperance
									|| (interaction.getSeverity() > minProbabilityForDisapperance && currentlyExportedElements
									.contains(interaction.getJavaElement()));
								
								if(isInFocus) {
									currentlyExportedElements.add(element.getJavaElement());
									export(interaction, isInFocus);
								} else {
									if(currentlyExportedElements.remove(element.getJavaElement())) {
										export(interaction, isInFocus);
									}
								}
							}
							
							interaction.setExported(true);
						}
					}
				}
			}
		} catch (ConcurrentModificationException e) {
			// ignore this rare case, the interactions will be exported next run.
		}
		
		
//		List<Element> focussedElementsToExport = new ArrayList<Element>();
//		for (Element element : interactionRepository.getElements().values()) {
//			if (element.getRating() >= minProbabilityForApperance
//					|| (element.getRating() > minProbabilityForDisapperance && currentlyExportedElements
//							.contains(element.getJavaElement()))) {
//				// notify the ecg, that this element has (gained) focus
//				currentlyExportedElements.add(element.getJavaElement());
//				focussedElementsToExport.add(element);
//			} else if (element.getRating() < minProbabilityForDisapperance
//					&& currentlyExportedElements.contains(element
//							.getJavaElement())) {
//				// notify the ecg, that this element isn't focussed anymore
//				currentlyExportedElements.remove(element.getJavaElement());
//				export(element.getJavaElement(), element.getRating(), false);
//			}
//		}
//
//		for (Element element : focussedElementsToExport) {
//			export(element.getJavaElement(), normalizeRating(element
//					.getRating(), focussedElementsToExport.size()), true);
//		}

		// export system interactions
		for (SystemInteraction interaction : interactionRepository
				.getSystemInteractions()) {
			if (!interaction.isExported()) {
				exportSystemInteraction(interaction);
			}
		}

	}

//	private double normalizeRating(double aRating, int aNumberOfElements) {
//		// return aRating / aNumberOfElements;
//		return aRating;
//	}

	public void propertyChange(PropertyChangeEvent aEvent) {
		if (PreferenceConstants.P_ECG_EXPORT_MIN_RATING_FOR_APPEARANCE
				.equals(aEvent.getProperty())) {
			minProbabilityForApperance = getMinProbabilityForApperance();
		} else if (PreferenceConstants.P_ECG_EXPORT_MIN_RATING_FOR_DISAPPEARANCE
				.equals(aEvent.getProperty())) {
			minProbabilityForDisapperance = getMinProbabilityForDisapperance();
		}
	}

	private double getMinProbabilityForApperance() {
		return FocusTrackerPlugin
				.getDefault()
				.getPluginPreferences()
				.getDouble(
						PreferenceConstants.P_ECG_EXPORT_MIN_RATING_FOR_APPEARANCE);
	}

	private double getMinProbabilityForDisapperance() {
		return FocusTrackerPlugin
				.getDefault()
				.getPluginPreferences()
				.getDouble(
						PreferenceConstants.P_ECG_EXPORT_MIN_RATING_FOR_DISAPPEARANCE);
	}

	private String timestampToXMLString(Date aDate) {

		DateFormat ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		TimeZone timeZone = TimeZone.getDefault();
		ISO8601Local.setTimeZone(timeZone);
		int offset = timeZone.getOffset(aDate.getTime());
		String sign = "+";
		if (offset < 0) {
			offset = -offset;
			sign = "-";
		}
		int hours = offset / 3600000;
		int minutes = (offset - hours * 3600000) / 60000;
		if (offset != hours * 3600000 + minutes * 60000) {
			// E.g. TZ=Asia/Riyadh87
			throw new RuntimeException("TimeZone offset (" + sign + offset
					+ " ms) is not an exact number of minutes");
		}
		DecimalFormat twoDigits = new DecimalFormat("00");
		String ISO8601Now = ISO8601Local.format(aDate) + sign
				+ twoDigits.format(hours) + ":" + twoDigits.format(minutes);

		// return "2006-01-03T09:23:58+06:00";
//		System.err.println(ISO8601Now);
		return ISO8601Now;
	}
}
