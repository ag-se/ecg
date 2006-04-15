package de.fu_berlin.inf.focustracker.rating.event;

import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;

public class EditorSelectionEvent implements EventHolder {

	private Action action;
	private ElementRegion elementRegion;
	private JavaInteraction lastInteraction;
	
	
	public EditorSelectionEvent(
			Action aAction,
			ElementRegion aElementRegion,
			JavaInteraction aLastInteraction) {
		
		action = aAction;
		elementRegion = aElementRegion;
		lastInteraction = aLastInteraction;
		
	}
	
	public Action getAction() {
		return action;
	}
	public void setAction(Action aAction) {
		action = aAction;
	}

	public JavaInteraction getLastInteraction() {
		return lastInteraction;
	}
	public void setLastInteraction(JavaInteraction aLastInteraction) {
		lastInteraction = aLastInteraction;
	}

	public ElementRegion getElementRegion() {
		return elementRegion;
	}

	public void setElementRegion(ElementRegion aElementRegion) {
		elementRegion = aElementRegion;
	}
	
	
	
}
