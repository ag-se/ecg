package org.electrocodeogram.cpc.ui.views.codereplay;

/**
 * Must be implemented to be able to register at the {@link ReplayControl} to receive {@link ReplayActionEvent}s.
 * (used for logic -> GUI communication)
 * 
 * @author marco kranz
 */
public interface IReplayActionListener {

	/**
	 * Will be called if an ReplayActionEvent is created in {@link ReplayControl}
	 * 
	 * @param event the event
	 */
	void ReplayAction(ReplayActionEvent event);
}
