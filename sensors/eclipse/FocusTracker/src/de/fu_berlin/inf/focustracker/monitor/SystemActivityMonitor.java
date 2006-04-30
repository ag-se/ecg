package de.fu_berlin.inf.focustracker.monitor;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class SystemActivityMonitor implements Listener {

	
	private long lastActivityTimestamp = 0;
	
	public void handleEvent(Event aEvent) {
		
//		System.err.println(aEvent.data + " - " + aEvent.x + " - " + aEvent.y);
		lastActivityTimestamp = System.currentTimeMillis();
	}

	public long getLastActivityTimestamp() {
		return lastActivityTimestamp;
	}

	
}
