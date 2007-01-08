package org.electrocodeogram.codereplay.dataProvider;

import java.util.*;

/**
 * A Replay consists of a number of {@link ReplayElement}s that represent the change of a particular text area
 * (whatever was observed) over time. It can be imagined as a movie consisting of a number of single pictures
 * (represented through ReplayElements). The ReplayElements are ordered chronologically.
 *  A pointer indicates the actual position of the Replay in the timeline.
 *   
 * 
 * @author marco kranz
 */
public class Replay {

	// the list contains all replay elements in chronological order
	private TreeMap map;
	// pointer storing the current replay position 
	private Date current_date;
	
	
	/**
	 * @param element the initial ReplayElement
	 */
	public Replay(ReplayElement element) {
		map = new TreeMap();
		addReplayElement(element);
		current_date = element.getTimestamp();
	}
	
	
	/**
	 * Adds a ReplayElement to this Replay and dynamically creates the {@link Diff} for this element.
	 * After insertion the Diff is created from this and the chronologically previous element.
	 * 
	 * @param element the element to be added
	 */
	synchronized void addReplayElement(ReplayElement element){
		/*if("removed".equals(element.getChange()) && element.getTimestamp().before(getLastElement().getTimestamp()))
			return;*/
		map.put(element.getTimestamp(), element);
		ReplayElement previous = getPreviousElement(element.getTimestamp());
		Diff diff = new Diff(0, 0, "");
		if(previous != null){
			String oldcode = previous.getSource();
			String newcode = element.getSource();
			try{
				//TODO: fix diff algorithm!!! 
				diff = SimpleDiff.getSimpleDiff(oldcode, newcode);
			}catch(Exception e){e.printStackTrace();}
		}
		element.setDiff(diff);
		current_date = (Date)map.firstKey();
	}
	
	
	/**
	 * @return a Collection of all {@link ReplayElement}s of this Replay
	 */
	public synchronized Collection getElements(){
		return map.values();
	}
	
	
	/**
	 * @return true if Replay has no elements, false otherwise
	 */
	// not possible??
	private synchronized boolean isEmpty(){
		if(map.isEmpty())
			return true;
		return false;
	}


	/**
	 * @return the last ReplayElement of this Replay
	 */
	public synchronized ReplayElement getLastElement(){
		if(isEmpty())
			return null;
		return (ReplayElement)map.get(map.lastKey());
	}
	
	/**
	 * @return the first ReplayElement of this Replay
	 */
	public synchronized ReplayElement getFirstElement(){
		if(isEmpty())
			return null;
		return (ReplayElement)map.get(map.firstKey());
	}
	
	/**
	 * @return the ReplayElement that chronologically follows the currently active element
	 */
	public synchronized ReplayElement getNextElement(){
		if(isEmpty())
			return null;
		return getNextElement(current_date);
	}
	
	/**
	 * @return the chronologically previous ReplayElement of the currently active elementy
	 */
	public synchronized ReplayElement getPreviousElement(){
		if(isEmpty())
			return null;
		return getPreviousElement(current_date);
	}

	
	/**
	 * @return the currently active ReplayElement
	 */
	public synchronized ReplayElement getCurrentElement(){
		return (ReplayElement)map.get(current_date);
	}

	
	/**
	 * @return the internal unique identifier of this Replay
	 */
	public synchronized String getIdentifier() {
		return ((ReplayElement)map.get(current_date)).getIdentifier();
	}

	/**
	 * @return true if there are enough elements for a replay(no. elements > 1), false otherwise
	 */
	public synchronized boolean hasEnoughElements(){
		if(map.size() > 1) 
			return true;
		else 
			return false;
	}
	
	
	/**
	 * The name of the Replay is something like the name or identifier of an observed method or textarea.
	 * 
	 * @return the name of this Replay
	 */
	public synchronized String getName(){
		return ((ReplayElement)map.get(current_date)).getName();
	}
	
	/**
	 * Moves internal pointer to the chronologically next element in this Replay.
	 * In case the pointer is already at the last position nothing will be done.
	 */
	synchronized void incrementPosition(){
		ReplayElement temp = getNextElement(current_date);
		if(temp != null){
			current_date = temp.getTimestamp();
			//DataProvider.getDataProvider().modelChanged(ModelChangeEvent.REPLAY_CHANGED, this);
		}
	}
	
	/**
	 * Moves internal pointer to the chronologically previous element in this Replay.
	 * In case the pointer is already at the first position nothing will be done.
	 */
	synchronized void decrementPosition(){
		ReplayElement temp = getPreviousElement(current_date);
		if(temp != null){
			current_date = temp.getTimestamp();
			//DataProvider.getDataProvider().modelChanged(ModelChangeEvent.REPLAY_CHANGED, this);
		}
	}
	
	
	/**
	 * Moves internal pointer to last position(if possible).
	 */
	synchronized void jumpToLastPosition(){
		current_date = getLastElement().getTimestamp();
		//DataProvider.getDataProvider().modelChanged(ModelChangeEvent.REPLAY_CHANGED, this);
	}
	
	/**
	 * Moves internal pointer to first position(if possible).
	 */
	synchronized void jumpToFirstPosition(){
		current_date = getFirstElement().getTimestamp();
		//DataProvider.getDataProvider().modelChanged(ModelChangeEvent.REPLAY_CHANGED, this);
	}
	
	
	
	/**
	 * @return true if pointer points on first element, false otherwise
	 */
	public synchronized boolean isStartOfReplay(){
		if(current_date.equals(map.get(map.firstKey())))
			return true;
		return false;
	}
	
	/**
	 * @return true if pointer points on last element, false otherwise
	 */
	public synchronized boolean isEndOfReplay(){
		if(current_date.equals(map.lastKey()))
			return true;
		return false;
	}
	
	
	/**
	 * @return a String representation of this Replay
	 */
	public synchronized String toString(){
		return ((ReplayElement)map.get(current_date)).getName();
	}
	
	/**
	 * Each Replay can have a path describing its position in a directory-like tree structure.
	 * If it has no path the array is empty(array.length == 0).
	 * 
	 * @return the path of this Replay as a String array. Can be of length 0!
	 */
	public synchronized String[] getPath(){
		return ((ReplayElement)map.get(current_date)).getPath();
	}
	
	
	/**
	 * @return the number of ReplayElements in this Replay
	 */
	public synchronized int getSize(){
		return map.size();
	}
	
	/**
	 * convenience method
	 * 
	 * @return the path as a String instead of a String array
	 */
	public synchronized String getFullPathAsString(){
		String[] temp = ((ReplayElement)map.get(current_date)).getPath();
		String result = "";
		for(int i = 0; i < temp.length; i++){
			result = result +"/"+ temp[i];
		}
		result = result +"/"+getName();
		return result;
	}
	
	// not working!
	// get last element at position pos relative to current_position
	// pos must be > 0
	// e.g. if pos = 5; -> current_position - 5
	/*public synchronized ReplayElement getElementAt(int pos){
		SortedMap submap;
		if(pos >= 0){
			submap = map.headMap(current_date);
			if(submap.size() >= pos){
				for(int i = 0; i < pos-1; i++){
					submap = submap.headMap(submap.lastKey());
				}
				return (ReplayElement)submap.get(submap.lastKey());
			}
			else
				return null;
		}
		else
			return null;
	}*/
	
	// get next element by timestamp
	private ReplayElement getNextElement(Date pos){
		ReplayElement result = getCurrentElement();
		SortedMap tail = map.tailMap(pos); // includes all elems with keys >= pos!
		Iterator it = tail.keySet().iterator();
		if(tail.size() > 1){
			it.next();						// move iterator to first element(current_position)
			result = (ReplayElement)tail.get(it.next());
		}
		return result;
	}
	
	// get previous element by timestamp
	private ReplayElement getPreviousElement(Date pos){
		ReplayElement result = null;
		SortedMap head = map.headMap(pos);
		//System.out.println("head: "+head+" size "+head.size());
		if(head.size() > 0)
			result = (ReplayElement)head.get(head.lastKey());
		return result;
	}


	/**
	 * Sets the element as active(only if element is part of this Replay)
	 * 
	 * @param element the ReplayElement that should be set as active element
	 */
	public synchronized void setActiveElement(ReplayElement element) {
		if(element.getIdentifier() == this.getIdentifier()){
			current_date = element.getTimestamp();
		}
	}
}
