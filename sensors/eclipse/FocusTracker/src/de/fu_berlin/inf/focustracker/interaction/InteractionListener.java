package de.fu_berlin.inf.focustracker.interaction;

import java.util.List;

public interface InteractionListener {

	public void notifyInteractionObserved(List<? extends Interaction> aInteractions);
}
