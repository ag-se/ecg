package de.fu_berlin.inf.focustracker.interaction;

import java.util.Date;

public abstract class Interaction {

	protected Action action;
	protected double severity;
	protected Date date;
//	protected Date endDate;
	protected Origin origin;
	protected Interaction lastInteraction;
	
	


	public Interaction(Action aAction, double aSeverity, Date aDate, Date aEndDate, Origin aOrigin) {
		action = aAction;
		severity = aSeverity;
		date = aDate;
//		endDate = aEndDate;
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




//	public Date getEndDate() {
//		return endDate;
//	}
//
//
//
//
//	public void setEndDate(Date aEndDate) {
//		endDate = aEndDate;
//	}
//



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
}

