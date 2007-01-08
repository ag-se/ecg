package org.electrocodeogram.codereplay.dataProvider;

import java.util.*;
import org.eclipse.jface.util.*;


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
public class DataProvider {

	// singleton instance
	private static DataProvider dataprovider = null;
//	 used to provide unique identifiers for the replays
	private Identifier identifier_provider = new Identifier();
	// used to provide a mapping between the element identifiers provided by the events
	// and internally used identifiers. this is needed because the element identifiers can 
	// change over time(in case they are directly tied to the element, e.g. if the full 
	// path of the element is used as the identifier) 
	private Hashtable mappingtable = new Hashtable(1000);
	// hashtable containing all Replays, key is the internal identifier(Replay.getIdentifier())
	private Hashtable replaytable = new Hashtable(100);
	// the treemodel
	private TreeModel treemodel = new TreeModel();
	// ModelChangeListener
	private ListenerList listener_list = new ListenerList();
	// the currently active Replay
	private Replay activereplay = null;
	
	
	private DataProvider() {	
	}

	
	/**
	 * Static method that provides the singleton instance of the DataProvider
	 * 
	 * @return The singleton instance.
	 */
	public static synchronized DataProvider getInstance(){
		if(dataprovider == null)
			dataprovider = new DataProvider();
		return dataprovider;
	}
	
	
	/**
	 * Provides the TreeModel that contains the tree representation of all Replays. 
	 * 
	 * @return The TreeModel 
	 */
	public synchronized TreeModel getTreeModel(){
		return treemodel;
	}
	
	
	/**
	 * Adds an {@link IModelChangeListener}.
	 * 
	 * @param listener the listener to add
	 */
	public synchronized void addModelChangeListener(IModelChangeListener listener){
		listener_list.add(listener);
	}
	
	/**
	 * Removes a previously added listener
	 * 
	 * @param listener the listener to remove
	 */
	public synchronized void removeModelChangeListener(IModelChangeListener listener){
		listener_list.remove(listener);
	}
	
	/**
	 * This method should be called to inform about the change of an external identifier
	 * (external identifiers are the ones used when events are written on sensor side).
	 * This only happens while initial creation of Replays from a file.  
	 * 
	 * @param oldIdentifier
	 * @param newIdentifier
	 */
	public synchronized void changeIdentifier(String oldIdentifier, String newIdentifier){
		String internalIdentifier = (String)mappingtable.remove(oldIdentifier);
		mappingtable.put(newIdentifier, internalIdentifier);
		modelChanged(ModelChangeEvent.IDENTIFIER_CHANGED, null);
	}
	
	// insert new replay element, called in eventreader
	/**
	 * Adds a ReplayElement. Internally this means that this element is added to its corresponding {@link Replay},
	 * if there is no such Replay(because its the first element in this Replay), a new one will be created.
	 * 
	 * @param element the ReplayElement to insert
	 */
	public synchronized void insertReplayElement(ReplayElement element){
		
//		type of change, used for ModelChangeEvent
		int cause;
		Replay rep = null;
		String new_identifier;
		String identifier = element.getIdentifier();
		if(!mappingtable.containsKey(identifier)){
			new_identifier = identifier_provider.getNewIdentifier();
			element.setIdentifier(new_identifier);
			mappingtable.put(identifier, new_identifier);
			rep = new Replay(element);
			replaytable.put(new_identifier, rep);
			cause = ModelChangeEvent.NEW_REPLAY;
		}
		// otherwise add an element to the appropriate replay
		else{
			new_identifier = (String)mappingtable.get(identifier);
			rep = (Replay)replaytable.get(new_identifier);
			element.setIdentifier(new_identifier);
			rep.addReplayElement(element);
			cause = ModelChangeEvent.NEW_ELEMENT;
		}
		if(cause == ModelChangeEvent.NEW_ELEMENT)
			treemodel.addNode(rep);
		modelChanged(cause, rep);
		// debug
		//System.out.println("identifier: "+rep.getIdentifier()+" name: "+rep.getName());
	}
	
	
	/**
	 * Sets the active Replay. Only one Replay can be active at any time.
	 * All activities (like increment position, decrement position...)
	 * are executed on the active {@link Replay}.
	 * 
	 * @param identifier the internal identifier of the Replay
	 */
	public synchronized void setActiveReplay(String identifier){
		activereplay = (Replay)replaytable.get(identifier);
		modelChanged(ModelChangeEvent.REPLAY_CHANGED, activereplay);
		modelChanged(ModelChangeEvent.ELEMENT_CHANGED, activereplay);
	}
	
	
	/**
	 * @return The currently active {@link Replay}
	 */
	public synchronized Replay getActiveReplay(){
		return activereplay;
	}
	
	// called to fire ModelChangeEvents
	private void modelChanged(int cause, Replay rep){
		Object[] listeners = listener_list.getListeners();
		 for (int i = 0; i < listeners.length; ++i) {
		    ((IModelChangeListener) listeners[i]).modelChange(new ModelChangeEvent(cause, rep));
		 }
	}

	/**
	 * Increment the replay position of the currently active {@link Replay}.
	 */
	public synchronized void incrementReplayPosition() {
		activereplay.incrementPosition();
		modelChanged(ModelChangeEvent.ELEMENT_CHANGED, activereplay);
	}
	
	/**
	 * Decrement the replay position of the currently active {@link Replay}.
	 */
	public synchronized void decrementReplayPosition() {
		activereplay.decrementPosition();
		modelChanged(ModelChangeEvent.ELEMENT_CHANGED, activereplay);
	}
	
	/**
	 * Set the replay pointer to the last replay element.
	 * See {@link Replay} for further information.
	 */
	public synchronized void jumpToLastPosition(){
		activereplay.jumpToLastPosition();
		modelChanged(ModelChangeEvent.ELEMENT_CHANGED, activereplay);
	}
	
	/**
	 * Set the replay pointer to the first replay element.
	 * See {@link Replay} for further information.
	 */
	public synchronized void jumpToFirstPosition(){
		activereplay.jumpToFirstPosition();
		modelChanged(ModelChangeEvent.ELEMENT_CHANGED, activereplay);
	}
	

	// merges two replays by adding all replay elements of the moved_from replays to
	// the moved_to replay. moved_from replays are deleted after successful merge.
	/**
	 * Merges two replays by adding all replay elements of the 'moved from' replays to
	 * the 'moved to' replay. 'Moved from' replays are deleted after successful merge.
	 * 
	 * @param from the source Replays 
	 * @param to the target Replay 
	 */
	public synchronized void mergeReplays(Object[] from, Object to) {
		Replay movedFrom;
		String movedToID = ((TreeNode)to).getIdentifier();
		Replay movedTo = (Replay)replaytable.get(movedToID);
		String movedFromID;
		
		for(int i = 0; i < from.length; i++){
			movedFromID = ((TreeNode)from[i]).getIdentifier();
			movedFrom = (Replay)replaytable.get(movedFromID);
			Enumeration keys = mappingtable.keys();
			String movedFromOrigIdentifier = null;
			//System.out.println("mergeReplays(): before change mapping id");
			while(keys.hasMoreElements()){								// change id mapping from old to new replay id
				movedFromOrigIdentifier = (String)keys.nextElement();
				if(((String)mappingtable.get(movedFromOrigIdentifier)).equals(movedFromID)){
					mappingtable.remove(movedFromOrigIdentifier);
					mappingtable.put(movedFromOrigIdentifier, movedToID);
				}
			}
			Collection movedElements = movedFrom.getElements();
			Iterator it = movedElements.iterator();
			ReplayElement element;
			String[] path = movedTo.getPath();
			//System.out.println("mergeReplays(): before move elements");
			try{
			while(it.hasNext()){										// move replay elements from old to new replay
				element = (ReplayElement)it.next();
				element.setIdentifier(movedToID);
				element.setPath(path);
				movedTo.addReplayElement(element);
			}
			}catch(Exception e){e.printStackTrace();}
			//System.out.println("mergeReplays(): before rebuild tree");
			replaytable.remove(movedFromID);
		}
		Enumeration replays = replaytable.elements();
		treemodel.rebuildTree(replays);
		modelChanged(ModelChangeEvent.REPLAY_REMOVED, movedTo);
	}
	
	
//	 clear all
	/**
	 * Resets the complete datamodel. Afterwards it is in the same state as at the start of the Program.
	 * (for example called before new data is loaded) 
	 */
	public synchronized void reset() {
		mappingtable.clear();
		replaytable.clear();
		activereplay = null;
		treemodel.reset();
	}
	
	
	/**
	 * @return An Enumeration of all available {@link Replay}s.
	 */
	public synchronized Enumeration getReplays(){
		return replaytable.elements();
	}
	

	/**
	 * Moves the pointer of the currently active {@link Replay} to the ReplayElement
	 * that is given as argument.
	 * 
	 * @param element The ReplayElement the pointer should be moved to. 
	 */
	public synchronized void setActiveElement(ReplayElement element) {
		activereplay.setActiveElement(element);
		modelChanged(ModelChangeEvent.ELEMENT_CHANGED, activereplay);
	}
	
//	 used to provide unique identifiers for the replays
	class Identifier{
		
		private int identifier = 0;
		
		public String getNewIdentifier(){
			return Integer.toString(++identifier);
		}
	}
}
