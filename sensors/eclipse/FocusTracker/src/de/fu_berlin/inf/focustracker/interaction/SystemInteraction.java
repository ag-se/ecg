package de.fu_berlin.inf.focustracker.interaction;

import java.util.Date;

public class SystemInteraction extends Interaction {

	public SystemInteraction(Action aAction, double aRating, Date aDate, Date aEndDate, Origin aOrigin) {
		super(aAction, aRating, aDate, aOrigin);
	}

	@Override
	public String toString() {
		return date + ": "   + origin + " - " + " SYSTEM ";
	}

}
