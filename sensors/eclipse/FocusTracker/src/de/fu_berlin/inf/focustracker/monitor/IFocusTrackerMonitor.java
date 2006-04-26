package de.fu_berlin.inf.focustracker.monitor;

import org.eclipse.ui.IWorkbenchPart;

public interface IFocusTrackerMonitor {

	void deregisterFromPart();

	void registerPart(IWorkbenchPart aPart);

}
