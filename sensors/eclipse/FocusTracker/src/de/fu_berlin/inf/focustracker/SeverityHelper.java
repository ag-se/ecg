package de.fu_berlin.inf.focustracker;

import de.fu_berlin.inf.focustracker.interaction.Action;

public class SeverityHelper {

	public static double calculateSeverity(Action aAction, double aSeverity) {
		
		double newSeverity = 0d;
		switch (aAction) {
		case LOSING_FOCUS:
			newSeverity = calculateLosingFocusSeverity(aSeverity);
			break;
		case NEIGHBOUR_SELECTED:
			newSeverity = calculateNeighbourOfSelectedSeverity(aSeverity);
			
		default:
			break;
		}
		return adjustSeverity(newSeverity);
	}

	private static double calculateLosingFocusSeverity(double aSeverity) {
		return aSeverity - (aSeverity / 10);
	}
	
	private static double calculateNeighbourOfSelectedSeverity(double aSeverity) {
		return /*aSeverity +*/ 0.2f;
	}
	
	public static double adjustSeverity(double aSeverity) {
		if (aSeverity < 0f) {
			return 0d;
		} else if (aSeverity > 1f) {
			return 1d;
		} else {
			return aSeverity;
		}
	}
	
}
