package de.fu_berlin.inf.focustracker.interaction;

import java.util.Date;

public abstract class Interaction {

	protected Action action;

	protected double rating;

	protected Date date;

	protected Origin origin;

	protected Interaction lastInteraction;

	private boolean exported;
	
	protected String comment = "";
	
	public Interaction(Action aAction, double aRating, Date aDate, Origin aOrigin) {
		action = aAction;
		rating = aRating;
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

	public double getRating() {
		return rating;
	}

	public void setRating(double aSeverity) {
		rating = aSeverity;
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

	public String getComment() {
		return comment;
	}

	public void setComment(String aComment) {
		comment = aComment;
	}

}
