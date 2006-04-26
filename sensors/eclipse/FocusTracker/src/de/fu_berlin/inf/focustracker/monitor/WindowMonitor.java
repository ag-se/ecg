package de.fu_berlin.inf.focustracker.monitor;

import java.util.Date;

import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.Origin;
import de.fu_berlin.inf.focustracker.interaction.SystemInteraction;


public class WindowMonitor implements IWindowListener {

	public void windowActivated(IWorkbenchWindow aWindow) {
		SystemInteraction interaction = new SystemInteraction(Action.WINDOW_ACTIVED, 0f, new Date(), null, Origin.WINDOW_SYSTEM);
		EventDispatcher.getInstance().notifyInteractionObserved(interaction);
	}

	public void windowDeactivated(IWorkbenchWindow aWindow) {
		SystemInteraction interaction = new SystemInteraction(Action.WINDOW_DEACTIVATED, 0f, new Date(), null, Origin.WINDOW_SYSTEM);
		EventDispatcher.getInstance().notifyInteractionObserved(interaction);
	}

	public void windowClosed(IWorkbenchWindow aWindow) {
	}

	public void windowOpened(IWorkbenchWindow aWindow) {
	}

}
