package org.electrocodeogram.codereplay.pluginControl;

/**
 * Event indicating changes to the state of the replay(replay here means the action, not the datatype).
 * 
 * @author marco kranz
 */
public class ReplayActionEvent {
	
	/**
	 * indicates that the countdown has changed
	 */
	public static final int COUNTDOWN_CHANGED = 0;
	/**
	 * indicates that the replay has been stopped
	 */
	public static final int REPLAY_STOPPED = 1;
	
	private int cause;

	/**
	 * @param c the event cause
	 */
	public ReplayActionEvent(int c){
		cause = c;
	}
	
	/**
	 * @return one of the possible event causes
	 */
	public int getCause(){
		return cause;
	}
}
