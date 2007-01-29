package de.fu_berlin.inf.atl.storer;

public interface Storer {

	void log(String s);

	boolean isEnabled();

	void setEnabled(boolean enabled);
	
	void shutdown();
	
}