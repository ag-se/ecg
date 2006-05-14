package de.fu_berlin.inf.focustracker.interaction;

import java.util.Date;

public abstract class Interaction {

	protected Action action;

	protected double severity;

	protected Date date;

	protected Origin origin;

	protected Interaction lastInteraction;

	private boolean exported;

	public Interaction(Action aAction, double aSeverity, Date aDate,
			Date aEndDate, Origin aOrigin) {
		action = aAction;
		severity = aSeverity;
		date = aDate;
		origin = aOrigin;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action aAction) {
		action = aAction;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date aDate) {
		date = aDate;
	}

	public Origin getOrigin() {
		return origin;
	}

	public void setOrigin(Origin aOrigin) {
		origin = aOrigin;
	}

	public double getSeverity() {
		return severity;
	}

	public void setSeverity(double aSeverity) {
		severity = aSeverity;
	}

	public Interaction getLastInteraction() {
		return lastInteraction;
	}

	public void setLastInteraction(Interaction aLastInteraction) {
		lastInteraction = aLastInteraction;
	}

	public boolean isExported() {
		return exported;
	}

	public void setExported(boolean aExported) {
		exported = aExported;
	}

}
