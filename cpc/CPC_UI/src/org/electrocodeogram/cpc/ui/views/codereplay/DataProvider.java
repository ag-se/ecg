package org.electrocodeogram.cpc.ui.views.codereplay;


import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The class DataProvider is(as the name implies) the main class of the data layer.
 * Its job is to keep all Data in a consistent state. This is accomplished by restricting
 * modify access of all data to this class only. This means this class provides the complete
 * interface for data manipulation.
 * in addition to that this class maintains the overall data model. It contains a list of all
 * available replays(see {@link Replay}) and keeps track of the currently active(selected) replay.
 * 
 * This class is, like the other main classes, implemented as a singleton. This is to provide
 * easy access from everywhere(by calling {@link DataProvider.getInstance}) and because there is 
 * no reason to have multiple instances of this class anyway. 
 * 
 * @author marco kranz
 */
public class DataProvider /*implements ICloneDataChangeListener*/
{
	private static final Log log = LogFactory.getLog(DataProvider.class);

	// singleton instance
	private static DataProvider dataprovider = null;

	// ModelChangeListener
	private Set<IModelChangeListener> listener_list = new HashSet<IModelChangeListener>();

	// the currently active Replay
	private Replay activereplay = null;

	private DataProvider()
	{
		log.trace("DataProvider()");

		// Don't automatically load data for each selected clone, that is too slow.
		//CloneDataModel.getInstance().addChangeListener(this);

		// Instead we now have a special context menu option in all clone views which
		// enables the user to open the replay view for a specific clone.
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.ui.data.ICloneDataChangeListener#cloneDataChanged(org.electrocodeogram.cpc.ui.data.CloneDataChange)
	 */
	/*
	@Override
	public void cloneDataChanged(final CloneDataChange event)
	{
		if (event.getSelectedClones() == null || event.getSelectedClones().length == 0)
		{
			//no clone selected
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					setActiveReplay(null);
				}
			});
		}
		else
		{
			//new clone selected
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					setActiveReplay(new CPCReplay(event.getSelectedClones()[0]));
				}
			});

		}
	}
	*/

	/**
	 * Static method that provides the singleton instance of the DataProvider
	 * 
	 * @return The singleton instance.
	 */
	public static synchronized DataProvider getInstance()
	{
		if (dataprovider == null)
			dataprovider = new DataProvider();
		return dataprovider;
	}

	/**
	 * Adds an {@link IModelChangeListener}.
	 * 
	 * @param listener the listener to add
	 */
	public synchronized void addModelChangeListener(IModelChangeListener listener)
	{
		listener_list.add(listener);
	}

	/**
	 * Removes a previously added listener
	 * 
	 * @param listener the listener to remove
	 */
	public synchronized void removeModelChangeListener(IModelChangeListener listener)
	{
		listener_list.remove(listener);
	}

	public synchronized void setActiveReplay(Replay replay)
	{
		if (log.isTraceEnabled())
			log.trace("setActiveReplay() - replay: " + replay);

		activereplay = replay;

		modelChanged(ModelChangeEvent.NEW_REPLAY, activereplay);
		modelChanged(ModelChangeEvent.REPLAY_CHANGED, activereplay);
		modelChanged(ModelChangeEvent.NEW_ELEMENT, activereplay);
	}

	/**
	 * @return The currently active {@link Replay}
	 */
	public synchronized Replay getActiveReplay()
	{
		return activereplay;
	}

	// called to fire ModelChangeEvents
	private void modelChanged(int cause, Replay rep)
	{
		if (log.isTraceEnabled())
			log.trace("modelChanged() - cause: " + cause + ", replay: " + rep);

		for (IModelChangeListener listener : listener_list)
		{
			if (log.isTraceEnabled())
				log.trace("modelChanged() - notifying: " + listener);

			listener.modelChange(new ModelChangeEvent(cause, rep));
		}
	}

	/**
	 * Increment the replay position of the currently active {@link Replay}.
	 */
	public synchronized void incrementReplayPosition()
	{
		activereplay.incrementPosition();
		modelChanged(ModelChangeEvent.ELEMENT_CHANGED, activereplay);
	}

	/**
	 * Decrement the replay position of the currently active {@link Replay}.
	 */
	public synchronized void decrementReplayPosition()
	{
		activereplay.decrementPosition();
		modelChanged(ModelChangeEvent.ELEMENT_CHANGED, activereplay);
	}

	/**
	 * Set the replay pointer to the last replay element.
	 * See {@link Replay} for further information.
	 */
	public synchronized void jumpToLastPosition()
	{
		activereplay.jumpToLastPosition();
		modelChanged(ModelChangeEvent.ELEMENT_CHANGED, activereplay);
	}

	/**
	 * Set the replay pointer to the first replay element.
	 * See {@link Replay} for further information.
	 */
	public synchronized void jumpToFirstPosition()
	{
		activereplay.jumpToFirstPosition();
		modelChanged(ModelChangeEvent.ELEMENT_CHANGED, activereplay);
	}

	/**
	 * Resets the complete datamodel. Afterwards it is in the same state as at the start of the Program.
	 * (for example called before new data is loaded) 
	 */
	public synchronized void reset()
	{
		activereplay = null;
	}

	/**
	 * Moves the pointer of the currently active {@link Replay} to the ReplayElement
	 * that is given as argument.
	 * 
	 * @param element The ReplayElement the pointer should be moved to. 
	 */
	public synchronized void setActiveElement(ReplayElement element)
	{
		if (log.isTraceEnabled())
			log.trace("setActiveElement() - element: " + element);

		activereplay.setActiveElement(element);

		modelChanged(ModelChangeEvent.ELEMENT_CHANGED, activereplay);
	}
}
