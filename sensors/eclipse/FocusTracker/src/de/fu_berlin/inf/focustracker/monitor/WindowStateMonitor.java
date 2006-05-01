package de.fu_berlin.inf.focustracker.monitor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.Origin;
import de.fu_berlin.inf.focustracker.interaction.SystemInteraction;
import de.fu_berlin.inf.focustracker.repository.Element;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;


public class WindowStateMonitor implements IWindowListener {

	Map<Element, Double> deactivatedElements = new HashMap<Element, Double>(); 
	
	public void windowActivated(IWorkbenchWindow aWindow) {
		SystemInteraction interaction = new SystemInteraction(Action.MAIN_WINDOW_ACTIVED, 1d, new Date(), null, Origin.WINDOW_SYSTEM);
		EventDispatcher.getInstance().notifyInteractionObserved(interaction);
		// TODO: interaction!!!
//		System.err.println("Main window activated");
		
//		for (Element element : deactivatedElements.keySet()) {
//			element.setRating(deactivatedElements.get(element));
//		}
		deactivatedElements.clear();
	}

	public void windowDeactivated(IWorkbenchWindow aWindow) {
		SystemInteraction interaction = new SystemInteraction(Action.MAIN_WINDOW_DEACTIVATED, 1d, new Date(), null, Origin.WINDOW_SYSTEM);
		EventDispatcher.getInstance().notifyInteractionObserved(interaction);
		// TODO: interaction!!!
//		System.err.println("Main window deactivated");
		
		for (Element element : InteractionRepository.getInstance().getElementsWithRating()) {
			deactivatedElements.put(element, element.getRating());
			element.setRating(0d);
		}
		
	}

	public void windowClosed(IWorkbenchWindow aWindow) {
	}

	public void windowOpened(IWorkbenchWindow aWindow) {
	}

}
