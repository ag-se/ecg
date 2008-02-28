package org.electrocodeogram.cpc.ui.data;


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


public class CloneDataModel
{
	private static Log log = LogFactory.getLog(CloneDataModel.class);

	private static CloneDataModel instance;

	private IStoreProvider storeProvider;
	private Set<ICloneDataChangeListener> listeners;
	private List<IClone> cloneData;
	private Map<String, ICloneGroup> cloneGroupRegistry;
	private ICloneFile currentCloneFile;
	private List<CloneDataChange> eventQueue;

	/**
	 * This class is a singleton, use <em>getInstance()</em> to acquire an instance.
	 * 
	 * @see CloneDataModel#getInstance()
	 */
	private CloneDataModel()
	{
		log.trace("CloneDataModel()");

		eventQueue = new LinkedList<CloneDataChange>();
		listeners = new HashSet<ICloneDataChangeListener>();
		cloneData = new LinkedList<IClone>();
		cloneGroupRegistry = new HashMap<String, ICloneGroup>();
		storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class);
		//TODO: do we maybe want to take the currently active project into account for provider selection?
		//		and what about on-the-fly provider replacements? It might be a good idea to wrap the provider
		//		lookup somewhere in the main plugin class

		//TODO: / FIXME: This is not the right place for this check. Due to race conditions it is not
		//				 guaranteed that the editor windows are already open at this point.
		//				 This code leads to clone views randomly showing or not showing clone data at
		//				 startup.
		//on first creation we might already have missed some critical editorpart activation/open events
		//we therefore just initialise ourself with the data from the currently active editor
		IFile activeFile = CoreUtils.getActiveTextEditorFile();
		if (activeFile != null)
		{
			if (log.isTraceEnabled())
				log.trace("CloneDataModel() - loading clone data for file: " + activeFile);

			ICloneFile cloneFile = CoreUtils.getCloneFileForFile(storeProvider, activeFile);
			if (cloneFile != null)
			{
				//initialise our model with this file
				loadCloneData(cloneFile);
			}
		}
		else
			log.trace("CloneDataModel() - no active editor");
	}

	public synchronized static CloneDataModel getInstance()
	{
		log.trace("getInstance()");

		if (instance == null)
			instance = new CloneDataModel();

		return instance;
	}

	public synchronized void addChangeListener(ICloneDataChangeListener listener)
	{
		if (log.isTraceEnabled())
			log.trace("addChangeListener(): " + listener);

		listeners.add(listener);
	}

	public synchronized void removeChangeListener(ICloneDataChangeListener listener)
	{
		if (log.isTraceEnabled())
			log.trace("removeChangeListener(): " + listener);

		listeners.remove(listener);
	}

	/**
	 * Retrieves an array of the current clones for this model.<br/>
	 * <br/>
	 * The returned clone objects may not be modified.
	 * 
	 * @return array of current clone data, elements must not be modified, never null.
	 */
	public synchronized IClone[] getCloneData()
	{
		log.trace("getCloneData()");

		IClone[] result;

		if (cloneData.isEmpty())
			result = new IClone[0];
		else
			result = cloneData.toArray(new IClone[cloneData.size()]);

		if (log.isTraceEnabled())
			log.trace("getCloneData() - result: " + CoreUtils.arrayToString(result));

		return result;
	}

	public synchronized ICloneGroup[] getCloneGroupData()
	{
		log.trace("getCloneGroupData()");

		ICloneGroup[] result;

		if (cloneGroupRegistry.isEmpty())
			result = new ICloneGroup[0];
		else
			result = cloneGroupRegistry.values().toArray(new ICloneGroup[cloneGroupRegistry.size()]);

		if (log.isTraceEnabled())
			log.trace("getCloneData() - result: " + CoreUtils.arrayToString(result));

		return result;
	}

	/**
	 * @return the currently active file, the clone model is only interested in clone data for this file
	 */
	public synchronized ICloneFile getCurrentCloneFile()
	{
		if (log.isTraceEnabled())
			log.trace("getCurrentCloneFile() - result: " + currentCloneFile);

		return currentCloneFile;
	}

	/**
	 * Retrieves the clone group for the given clone group uuid from the model.<br/>
	 * This will return NULL if the clone group in question is not part of the model.
	 * 
	 * @param groupUuid the uuid of the group to lookup, never null.
	 * @return the clone group in question or NULL if that group is not part of the model.
	 */
	public synchronized ICloneGroup getCloneGroupByUuid(String groupUuid)
	{
		if (log.isTraceEnabled())
			log.trace("getCloneGroupByUuid() - groupUuid: " + groupUuid);
		assert (groupUuid != null);

		return cloneGroupRegistry.get(groupUuid);
	}

	/**
	 * Retrieves the clone file for a given clone file uuid from the model.<br/>
	 * Delegates to Store Provider with optional internal caching.
	 * 
	 * @param fileUuid uuid of the file to lookup, never null.
	 * @return clone file object for given uuid or NULL if not found.
	 */
	public synchronized ICloneFile getCloneFileByUuid(String fileUuid)
	{
		if (log.isTraceEnabled())
			log.trace("getCloneFileByUuid() - fileUuid: " + fileUuid);
		assert (fileUuid != null);

		//check if this is our current file
		if (currentCloneFile != null && currentCloneFile.getUuid().equals(fileUuid))
			return currentCloneFile;

		//TODO: we might want to add some caching here

		//delegate to store provider
		return storeProvider.lookupCloneFile(fileUuid);
	}

	/**
	 * Retrieves a list of clones which intersect with the given range in at least one character.<br/>
	 * <br/>
	 * The returned clone objects may not be modified.
	 * 
	 * @param offset start offset of the range, 0-based, always &gt;= 0.
	 * @param length length of the range, always &gt;= 0.
	 * 		A length of 0 will be handled like a length of 1.
	 * @return list of clones in range, elements may not be modified, never null.
	 */
	public synchronized List<IClone> getClonesForRange(int offset, int length)
	{
		if (log.isTraceEnabled())
			log.trace("getClonesForRange() - offset: " + offset + ", length: " + length);

		List<IClone> result = new LinkedList<IClone>();

		for (IClone clone : cloneData)
		{
			if (clone.intersects(offset, length))
			{
				result.add(clone);
			}
		}

		if (log.isTraceEnabled())
			log.trace("getClonesForRange() - result: " + result);

		return result;
	}

	/**
	 * @return a valid store provider instance, never null.
	 */
	public IStoreProvider getStoreProvider()
	{
		assert (storeProvider != null);
		return storeProvider;
	}

	/**
	 * Called whenever a user actions leads to a situation where clone data views should not display
	 * any clone data. I.e. if the user just closed an editor or the editor lost focus in favour of
	 * another editor.
	 */
	public void clearCloneData()
	{
		synchronized (this)
		{
			log.trace("clearCloneData()");

			CloneDataChange event = null;

			//if we don't store any clones atm, we don't need to do anything
			if (!cloneData.isEmpty())
			{
				if (event == null)
					event = new CloneDataChange();

				event.setRemovedClones(cloneData.toArray(new IClone[cloneData.size()]));

				//remove all clone data
				cloneData.clear();
			}

			if (!cloneGroupRegistry.isEmpty())
			{
				if (event == null)
					event = new CloneDataChange();

				event.setRemovedCloneGroups(cloneGroupRegistry.values().toArray(
						new ICloneGroup[cloneGroupRegistry.size()]));

				//remove all clone group data
				cloneGroupRegistry.clear();
			}

			//and notify all views about the change
			if (event != null)
				queueListenerNotification(event);

			currentCloneFile = null;
		}

		notifyListeners();
	}

	/**
	 * Called whenever a user action invalidates all prior clone data and requires a refresh. I.e. if the
	 * user switched the focus to another editor. We then have to remove all the clones from the old file
	 * from all views and load the clones for the new file from the store provider. 
	 * 
	 * @param project name of the project, never null
	 * @param path path to the file currently being edited by the user, relative to project, never null
	 */
	public void loadCloneData(String project, String path)
	{
		assert (project != null && path != null);

		if (log.isTraceEnabled())
			log.trace("loadCloneData() - project: " + project + ", path: " + path);

		//now get the clone data for the file
		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(project, path, true, true);
		if (cloneFile == null)
		{
			log.error("loadCloneData() - unable to obtain clone file instance for file - project: " + project
					+ ", path: " + path, new Throwable());
			return;
		}

		loadCloneData(cloneFile);
	}

	/**
	 * @param cloneFile the file currently being edited by the user, never null
	 * 
	 * @see CloneDataModel#loadCloneData(ICloneFile)
	 */
	@SuppressWarnings("unchecked")
	public void loadCloneData(ICloneFile cloneFile)
	{
		synchronized (this)
		{
			if (log.isTraceEnabled())
				log.trace("loadCloneData() - cloneFile: " + cloneFile);
			assert (cloneFile != null);

			IClone[] removedClones = null;
			ICloneGroup[] removedCloneGroups = null;

			//first remove any old entries
			if (!cloneData.isEmpty())
			{
				if (log.isTraceEnabled())
					log.trace("loadCloneData() - removing " + cloneData.size() + " old clones");

				removedClones = cloneData.toArray(new IClone[cloneData.size()]);
				cloneData.clear();
			}
			else
			{
				removedClones = new IClone[0];
			}
			if (!cloneGroupRegistry.isEmpty())
			{
				if (log.isTraceEnabled())
					log.trace("loadCloneData() - removing " + cloneGroupRegistry.size() + " old clone groups");

				removedCloneGroups = cloneGroupRegistry.values().toArray(new ICloneGroup[cloneGroupRegistry.size()]);
				cloneGroupRegistry.clear();
			}
			else
			{
				removedCloneGroups = new ICloneGroup[0];
			}

			//now get the clone data for the given file
			cloneData = storeProvider.getClonesByFile(cloneFile.getUuid());

			//why do i need this cast? x_X
			CoreUtils.sealList((List) cloneData);

			//fill cloneGroupData

			//first we get a list with all group uuids
			Set<String> groupUuids = new HashSet<String>(cloneData.size());
			for (IClone clone : cloneData)
			{
				if (clone.getGroupUuid() != null)
					groupUuids.add(clone.getGroupUuid());
			}

			//now we get the data for each group
			for (String groupUuid : groupUuids)
			{
				ICloneGroup cloneGroup = storeProvider.lookupCloneGroup(groupUuid);
				cloneGroup.seal();
				cloneGroupRegistry.put(groupUuid, cloneGroup);
			}

			currentCloneFile = cloneFile;

			if (log.isTraceEnabled())
				log.trace("loadCloneData() - loaded - " + cloneData.size() + " clones, " + cloneGroupRegistry.size()
						+ " clone groups");

			//notify all views about this change
			//but only if something changed (we don't send an event if the view was empty before and
			//will still be empty after this update)
			if (removedClones.length > 0 || removedCloneGroups.length > 0 || !cloneData.isEmpty()
					|| !cloneGroupRegistry.isEmpty())
			{
				CloneDataChange event = new CloneDataChange();

				event.setAddedClones(cloneData.toArray(new IClone[cloneData.size()]));
				event.setRemovedClones(removedClones);
				event.setAddedCloneGroups(cloneGroupRegistry.values().toArray(
						new ICloneGroup[cloneGroupRegistry.size()]));
				event.setRemovedCloneGroups(removedCloneGroups);

				queueListenerNotification(event);
			}
		}

		notifyListeners();
	}

	/**
	 * Called whenever the current clone data was modified.<br/>
	 * Must only be called with clone data for the currently active file {@see CloneDataModel#getCurrentFile()}.
	 * 
	 * @param addedClones list of clones which were updated, MAY BE NULL
	 * @param updatedClones list of clones which data was changed, MAY BE NULL
	 * @param removedClones list of clones which were removed, MAY BE NULL
	 */
	public void cloneDataModified(List<IClone> addedClones, List<IClone> updatedClones, List<IClone> removedClones)
	{
		synchronized (this)
		{
			if (log.isTraceEnabled())
				log.trace("cloneDataModified() - addedClones: " + addedClones + ", updatedClones: " + updatedClones
						+ ", removedClones: " + removedClones);

			IClone[] addedClonesArray = null;
			IClone[] removedClonesArray = null;
			IClone[] updatedClonesArray = null;
			List<ICloneGroup> addedCloneGroups = new LinkedList<ICloneGroup>();
			List<ICloneGroup> updatedCloneGroups = new LinkedList<ICloneGroup>();
			List<ICloneGroup> removedCloneGroups = new LinkedList<ICloneGroup>();

			/*
			 * Update clone data
			 */

			if ((addedClones != null) && (!addedClones.isEmpty()))
			{
				//add new clones
				cloneData.addAll(addedClones);

				//prepare data for listeners
				addedClonesArray = addedClones.toArray(new IClone[addedClones.size()]);
			}

			if ((removedClones != null) && (!removedClones.isEmpty()))
			{
				//remove deleted clones
				cloneData.removeAll(removedClones);

				//prepare data for listeners
				removedClonesArray = removedClones.toArray(new IClone[removedClones.size()]);
			}

			if ((updatedClones != null) && (!updatedClones.isEmpty()))
			{
				//replace all updated clones
				cloneData.removeAll(updatedClones);
				cloneData.addAll(updatedClones);

				updatedClonesArray = updatedClones.toArray(new IClone[updatedClones.size()]);
			}

			/*
			 * Update clone group data
			 */

			//get a list of all potentially affected group uuids
			Set<String> groupUuids = new HashSet<String>(20);
			if (addedClones != null)
				for (IClone clone : addedClones)
					if (clone.getGroupUuid() != null)
						groupUuids.add(clone.getGroupUuid());
			if (removedClones != null)
				for (IClone clone : removedClones)
					if (clone.getGroupUuid() != null)
						groupUuids.add(clone.getGroupUuid());
			if (updatedClones != null)
				for (IClone clone : updatedClones)
					if (clone.getGroupUuid() != null)
						groupUuids.add(clone.getGroupUuid());

			if (log.isTraceEnabled())
				log.trace("cloneDataModified() - potentially modified group Uuids: " + groupUuids);

			//re-get group entries for affected group uuids
			for (String groupUuid : groupUuids)
			{
				ICloneGroup cloneGroup = storeProvider.lookupCloneGroup(groupUuid);
				ICloneGroup oldCloneGroup = cloneGroupRegistry.get(groupUuid);
				if (cloneGroup == null)
				{
					//the group doesn't exist, make sure it is not part of our cloneGroupData list
					if (oldCloneGroup != null)
					{
						//ok, remove the old entry
						cloneGroupRegistry.remove(groupUuid);
						removedCloneGroups.add(oldCloneGroup);
					}
				}
				else
				{
					//the group exists, check if we know it
					if (oldCloneGroup == null)
					{
						//we haven't stored that group so far, add it
						cloneGroupRegistry.put(groupUuid, cloneGroup);
						addedCloneGroups.add(cloneGroup);
					}
					else
					{
						//the group was potentially modified, re-add it
						cloneGroupRegistry.put(groupUuid, cloneGroup);
						updatedCloneGroups.add(cloneGroup);
					}
				}
			}

			if (log.isTraceEnabled())
				log.trace("cloneDataModified() - Clones - " + (addedClonesArray != null ? addedClonesArray.length : 0)
						+ " added, " + (updatedClonesArray != null ? updatedClonesArray.length : 0) + " updated, "
						+ (removedClonesArray != null ? removedClonesArray.length : 0) + " removed - Groups - "
						+ addedCloneGroups.size() + " added, " + updatedCloneGroups.size() + " updated, "
						+ removedCloneGroups.size() + " removed");

			/*
			 * Notify all views about this change
			 */

			//if there was a change
			if (addedClonesArray != null || removedClonesArray != null || updatedClonesArray != null)
			{
				CloneDataChange event = new CloneDataChange();

				event.setAddedClones(addedClonesArray);
				event.setUpdatedClones(updatedClonesArray);
				event.setRemovedClones(removedClonesArray);
				if (!addedCloneGroups.isEmpty())
					event.setAddedCloneGroups(addedCloneGroups.toArray(new ICloneGroup[addedCloneGroups.size()]));
				if (!updatedCloneGroups.isEmpty())
					event.setUpdatedCloneGroups(updatedCloneGroups.toArray(new ICloneGroup[updatedCloneGroups.size()]));
				if (!removedCloneGroups.isEmpty())
					event.setRemovedCloneGroups(removedCloneGroups.toArray(new ICloneGroup[removedCloneGroups.size()]));

				queueListenerNotification(event);
			}
		}

		notifyListeners();
	}

	/**
	 * Should be called whenever the user selects/highlights clones in any view.<br/>
	 * This will result in a corresponding {@link CloneDataChange} event being sent to all
	 * registered {@link ICloneDataChangeListener}s. The result is that all views will
	 * share one selection.
	 * 
	 * @param selectionOrigin an identifier string which uniquely identifies the source of this selection change.
	 * 		It will be included in the {@link CloneDataChange} event to allow the origin to ignore events which
	 * 		were generated by itself.
	 * @param selectedClones a list of clones which were just selected by the user, never null.
	 */
	public void clonesSelected(String selectionOrigin, List<IClone> selectedClones)
	{
		if (log.isTraceEnabled())
			log.trace("clonesSelected() - selectedClones: " + selectedClones);
		assert (selectedClones != null);

		//make sure something was selected
		if (selectedClones.isEmpty())
		{
			log.trace("clonesSelected() - ignoring empty selection.");
			return;
		}

		//create a new clone data change event
		CloneDataChange newEvent = new CloneDataChange(selectionOrigin, selectedClones
				.toArray(new IClone[selectedClones.size()]));
		queueListenerNotification(newEvent);

		//notify all listeners
		notifyListeners();
	}

	/**
	 * Enqueues a clone data change event for sending to all registered listeners.<br/>
	 * {@link CloneDataModel#notifyListeners()} will need to be called for this event to be actually dispatched.
	 * 
	 * @param event the event to queue for dispatching, never null
	 */
	private synchronized void queueListenerNotification(CloneDataChange event)
	{
		if (log.isTraceEnabled())
			log.trace("queueListenerNotification(): " + event);
		assert (event != null);

		if (!event.containsData())
			log.warn("queueListenerNotification() - dropping empty event: " + event);

		eventQueue.add(event);
	}

	/**
	 * Dispatches queued clone data change events to all registered listeners.<br/>
	 * Does nothing if no events are pending.<br/>
	 * <br/>
	 * <b>IMPORTANT:</b> This method <b>must not</b> be called from within a <em>synchronized</em> block. That would cause deadlocks.
	 */
	private void notifyListeners()
	{
		log.trace("notifyListeners()");

		List<CloneDataChange> myEvents = new LinkedList<CloneDataChange>();
		List<ICloneDataChangeListener> myListeners = new LinkedList<ICloneDataChangeListener>();

		//get all pending events from the queue and clear it in a thread safe manner
		synchronized (this)
		{
			if (eventQueue.isEmpty())
				return;

			myEvents.addAll(eventQueue);
			eventQueue.clear();

			//we also want a thread safe list of listeners
			myListeners.addAll(listeners);
		}

		if (log.isTraceEnabled())
			log.trace("notifyListeners() - queue size: " + myEvents + ", listener count: " + myListeners.size());

		for (CloneDataChange event : myEvents)
		{
			for (ICloneDataChangeListener listener : myListeners)
			{
				if (log.isTraceEnabled())
					log.trace("notifyListeners() - notifying: " + listener);

				listener.cloneDataChanged(event);
			}
		}
	}
}
