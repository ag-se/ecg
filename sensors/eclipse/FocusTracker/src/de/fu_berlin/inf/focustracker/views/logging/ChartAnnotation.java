package de.fu_berlin.inf.focustracker.views.logging;

import de.fu_berlin.inf.focustracker.interaction.Action;

public class ChartAnnotation {

	public static String getChartAnnotation(Action aAction) {
		
		if(aAction == de.fu_berlin.inf.focustracker.interaction.Action.SELECTED) {
			return "Selected";
		} else if (aAction == de.fu_berlin.inf.focustracker.interaction.Action.TEXT_CHANGED){
			return "Text changed";
		}
		return null;
	}
	
}
