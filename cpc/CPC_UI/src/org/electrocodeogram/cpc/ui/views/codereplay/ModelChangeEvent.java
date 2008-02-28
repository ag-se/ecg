package org.electrocodeogram.cpc.ui.views.codereplay;


/**
 * This event documents various changes to the data model.
 * For details see the event causes below.
 * 
 * @author marco kranz
 */
public class ModelChangeEvent{
	/**
	 * indicates the identifier change of a Replay
	 */
	public static final int IDENTIFIER_CHANGED = 0;
	/**
	 * a new {@link Replay} has been added
	 */
	public static final int NEW_REPLAY = 1;
	/**
	 * an {@link Replay} has been removed
	 */
	public static final int REPLAY_REMOVED = 2;
	/**
	 * a new {@link ReplayElement} has been added
	 */
	public static final int NEW_ELEMENT = 3;
	/**
	 * a {@link Replay} has changed
	 */
	public static final int REPLAY_CHANGED = 4;
	/**
	 * a {@link ReplayElement} has changed
	 */
	public static final int ELEMENT_CHANGED = 5;
	// event reason, see above 
	private int cause;
	// modified replay or null in case of IDENTIFIER_CHANGED
	private Replay last_replay;
	
	/**
	 * @param c event cause
	 * @param rep the affected Replay(can be null)
	 */
	public ModelChangeEvent(int c, Replay rep){
		cause = c;
		last_replay = rep;
	}
	
	/**
	 * @return the event cause
	 */
	public int getCause(){
		return cause;
	}
	
	/**
	 * @return the affected Replay(can be null)
	 */
	public Replay getReplay(){
		return last_replay;
	}
}
