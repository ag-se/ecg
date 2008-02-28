package org.electrocodeogram.cpc.ui.views.codereplay;


import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * A Replay consists of a number of {@link ReplayElement}s that represent the change of a particular text area
 * (whatever was observed) over time. It can be imagined as a movie consisting of a number of single pictures
 * (represented through ReplayElements). The ReplayElements are ordered chronologically.
 *  A pointer indicates the actual position of the Replay in the timeline.
 *   
 * 
 * @author marco kranz
 */
public class Replay
{
	// the list contains all replay elements in chronological order
	private TreeSet<ReplayElement> set;

	// pointer storing the current replay position 
	private ReplayElement current;

	public Replay()
	{
		set = new TreeSet<ReplayElement>();
		current = null;
	}

	/**
	 * @param element the initial ReplayElement
	 */
	public Replay(ReplayElement element)
	{
		set = new TreeSet<ReplayElement>();
		addReplayElement(element);
		current = element;
	}

	/**
	 * Adds a ReplayElement to this Replay and dynamically creates the {@link Diff} for this element.
	 * After insertion the Diff is created from this and the chronologically previous element.
	 * 
	 * @param element the element to be added
	 */
	synchronized void addReplayElement(ReplayElement element)
	{
		set.add(element);
		current = set.first();
	}

	/**
	 * @return a Collection of all {@link ReplayElement}s of this Replay
	 */
	public synchronized Collection<ReplayElement> getElements()
	{
		return set;
	}

	/**
	 * @return true if Replay has no elements, false otherwise
	 */
	// not possible??
	private synchronized boolean isEmpty()
	{
		if (set.isEmpty())
			return true;
		return false;
	}

	/**
	 * @return the last ReplayElement of this Replay
	 */
	public synchronized ReplayElement getLastElement()
	{
		if (isEmpty())
			return null;
		return set.last();
	}

	/**
	 * @return the first ReplayElement of this Replay
	 */
	public synchronized ReplayElement getFirstElement()
	{
		if (isEmpty())
			return null;
		return set.first();
	}

	/**
	 * @return the ReplayElement that chronologically follows the currently active element
	 */
	public synchronized ReplayElement getNextElement()
	{
		if (isEmpty())
			return null;
		return getNextElement(current);
	}

	/**
	 * @return the chronologically previous ReplayElement of the currently active element
	 */
	public synchronized ReplayElement getPreviousElement()
	{
		if (isEmpty())
			return null;
		return getPreviousElement(current);
	}

	/**
	 * @return the currently active ReplayElement
	 */
	public synchronized ReplayElement getCurrentElement()
	{
		return current;
	}

	/**
	 * @return the internal unique identifier of this Replay
	 */
	public synchronized String getIdentifier()
	{
		return current.getIdentifier();
	}

	/**
	 * @return true if there are enough elements for a replay(no. elements > 1), false otherwise
	 */
	public synchronized boolean hasEnoughElements()
	{
		if (set.size() > 1)
			return true;
		else
			return false;
	}

	/**
	 * The name of the Replay is something like the name or identifier of an observed method or textarea.
	 * 
	 * @return the name of this Replay
	 */
	public synchronized String getName()
	{
		return current.getName();
	}

	/**
	 * Moves internal pointer to the chronologically next element in this Replay.
	 * In case the pointer is already at the last position nothing will be done.
	 */
	synchronized void incrementPosition()
	{
		ReplayElement temp = getNextElement(current);
		if (temp != null)
		{
			current = temp;
			//DataProvider.getDataProvider().modelChanged(ModelChangeEvent.REPLAY_CHANGED, this);
		}
	}

	/**
	 * Moves internal pointer to the chronologically previous element in this Replay.
	 * In case the pointer is already at the first position nothing will be done.
	 */
	synchronized void decrementPosition()
	{
		ReplayElement temp = getPreviousElement(current);
		if (temp != null)
		{
			current = temp;
			//DataProvider.getDataProvider().modelChanged(ModelChangeEvent.REPLAY_CHANGED, this);
		}
	}

	/**
	 * Moves internal pointer to last position(if possible).
	 */
	synchronized void jumpToLastPosition()
	{
		current = getLastElement();
		//DataProvider.getDataProvider().modelChanged(ModelChangeEvent.REPLAY_CHANGED, this);
	}

	/**
	 * Moves internal pointer to first position(if possible).
	 */
	synchronized void jumpToFirstPosition()
	{
		current = getFirstElement();
		//DataProvider.getDataProvider().modelChanged(ModelChangeEvent.REPLAY_CHANGED, this);
	}

	/**
	 * @return true if pointer points on first element, false otherwise
	 */
	public synchronized boolean isStartOfReplay()
	{
		if (current.equals(set.first()))
			return true;
		return false;
	}

	/**
	 * @return true if pointer points on last element, false otherwise
	 */
	public synchronized boolean isEndOfReplay()
	{
		if (current.equals(set.last()))
			return true;
		return false;
	}

	/**
	 * @return a String representation of this Replay
	 */
	@Override
	public synchronized String toString()
	{
		return current.getName();
	}

	/**
	 * Each Replay can have a path describing its position in a directory-like tree structure.
	 * If it has no path the array is empty(array.length == 0).
	 * 
	 * @return the path of this Replay as a String array. Can be of length 0!
	 */
	public synchronized String[] getPath()
	{
		return current.getPath();
	}

	/**
	 * @return the number of ReplayElements in this Replay
	 */
	public synchronized int getSize()
	{
		return set.size();
	}

	/**
	 * convenience method
	 * 
	 * @return the path as a String instead of a String array
	 */
	public synchronized String getFullPathAsString()
	{
		String[] temp = current.getPath();
		String result = "";
		for (int i = 0; i < temp.length; i++)
		{
			result = result + "/" + temp[i];
		}
		result = result + "/" + getName();
		return result;
	}

	// get next element by timestamp
	private ReplayElement getNextElement(ReplayElement pos)
	{
		ReplayElement result = getCurrentElement();
		SortedSet<ReplayElement> tail = set.tailSet(pos); // includes all elems with keys >= pos!
		Iterator<ReplayElement> it = tail.iterator();
		if (tail.size() > 1)
		{
			it.next(); // skip current
			result = it.next(); // move iterator to element after current
		}
		return result;
	}

	// get previous element by timestamp
	private ReplayElement getPreviousElement(ReplayElement pos)
	{
		ReplayElement result = null;
		SortedSet<ReplayElement> head = set.headSet(pos);
		//System.out.println("head: "+head+" size "+head.size());
		if (head.size() > 0)
			result = head.last();
		return result;
	}

	/**
	 * Sets the element as active(only if element is part of this Replay)
	 * 
	 * @param element the ReplayElement that should be set as active element
	 */
	public synchronized void setActiveElement(ReplayElement element)
	{
		if (element.getIdentifier() == this.getIdentifier())
		{
			current = element;
		}
	}
}
