package de.fu_berlin.inf.focustracker.monitor;

import org.eclipse.ui.IWorkbenchPart;

public abstract class AbstractFocusTrackerMonitor implements IFocusTrackerMonitor {

	public abstract void deregisterFromPart();
	
	public void partActivated() {
	}

	public void partClosed() {
	}

	public void partDeactivated() {
	}

	public abstract void registerPart(IWorkbenchPart aPart);

}
