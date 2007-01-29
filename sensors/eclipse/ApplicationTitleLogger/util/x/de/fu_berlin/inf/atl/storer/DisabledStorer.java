package de.fu_berlin.inf.atl.storer;

public class DisabledStorer implements Storer {

	public void log(String s) {
		// Does nothing
	}

	public boolean isEnabled() {
		return false;
	}

	public void setEnabled(boolean enabled) {
		// Does nothing
	}

	public void shutdown() {
		// Does nothing
	}
}
