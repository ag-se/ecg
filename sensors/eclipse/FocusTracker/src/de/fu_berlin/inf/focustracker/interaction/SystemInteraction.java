package de.fu_berlin.inf.focustracker.interaction;

import java.util.Date;

public class SystemInteraction extends Interaction {

	private boolean exported;
	
	public SystemInteraction(Action aAction, double aSeverity, Date aDate, Date aEndDate, Origin aOrigin) {
		super(aAction, aSeverity, aDate, aEndDate, aOrigin);
	}

	@Override
	public String toString() {
		return date + ": "   + origin + " - " + " SYSTEM ";
	}

	public boolean isExported() {
		return exported;
	}

	public void setExported(boolean aExported) {
		exported = aExported;
	}
	
}
