package org.electrocodeogram.cpc.optimiser.task;


import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.IClone.State;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode;


/**
 * This task goes through all files within the specified projects retrieves their
 * clone data and checks whether any of the clones are orphans.<br/>
 * All clones which are orphans but don't have the {@link IClone.State#ORPHAN} state
 * will have that state set.<br/>
 * <br/>
 * If the {@link #OPTION_REMOVE_ORPHAN_CLONES} option is set, orphan clones will be
 * deleted by this task.
 * 
 * @author vw
 */
public class OrphanDetectionTask extends AbstractGlobalCloneTask
{
	private static final Log log = LogFactory.getLog(OrphanDetectionTask.class);

	/**
	 * If this value is set to "<em>1</em>", detected orphan clones will be
	 * deleted.
	 */
	public static final String OPTION_REMOVE_ORPHAN_CLONES = "removeOrphanClones";

	private boolean removeOrphanClones = false;
	private int processedFileCount = 0;
	private int processedCloneCount = 0;
	private int updatedCloneCount = 0;
	private int deletedCloneCount = 0;

	public OrphanDetectionTask()
	{
		super("CPC:OrphanDetectionTaskJob", true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.optimiser.task.AbstractGlobalCloneTask#handleOptions(java.util.List, java.util.Map)
	 */
	@Override
	protected void handleOptions(List<IProject> projects, Map<String, String> optionsMap)
	{
		if (log.isTraceEnabled())
			log.trace("handleOptions() - projects: " + projects + ", optionsMap: " + optionsMap);

		/*
		 * Get configuration settings.
		 */
		if (optionsMap != null && optionsMap.containsKey(OPTION_REMOVE_ORPHAN_CLONES)
				&& "1".equals(optionsMap.get(OPTION_REMOVE_ORPHAN_CLONES)))
			removeOrphanClones = true;

		if (log.isTraceEnabled())
			log.trace("handleOptions() - options - removeOrphanClones: " + removeOrphanClones);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.optimiser.task.AbstractGlobalCloneTask#handleFile(org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider, org.eclipse.core.resources.IFile, org.electrocodeogram.cpc.core.api.data.ICloneFile)
	 */
	@Override
	protected void handleFile(IStoreProvider storeProvider, IFile file, ICloneFile cloneFile)
			throws IllegalArgumentException, StoreLockingException
	{
		++processedFileCount;

		//get clone data for file
		List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid());

		for (IClone clone : clones)
		{
			++processedCloneCount;

			//check whether this clone is an orphan.
			if (clone.getGroupUuid() == null)
			{
				//yep, it is an orphan
				handleOrphan(storeProvider, clone, true);
			}
			else
			{
				//get group members
				List<IClone> groupClones = storeProvider.getClonesByGroup(clone.getGroupUuid());
				if (groupClones.size() <= 1)
				{
					if (groupClones.isEmpty() || !groupClones.get(0).getUuid().equals(clone.getUuid()))
					{
						//something is very wrong here x_X
						log.error("run() - clone is not a member of its own clone group - clone: " + clone
								+ ", groupClones: " + groupClones + ", clone file: " + cloneFile + ", file clones: "
								+ clones, new Throwable());
						//we skip this clone
						continue;
					}

					//yep, also an orphan
					handleOrphan(storeProvider, clone, true);
				}
				else
				{
					//this is not an orphan
					if (State.ORPHAN.equals(clone.getCloneState()))
					{
						//but it is marked as orphan! we need to update that.
						handleOrphan(storeProvider, clone, false);
					}
				}
			}
		}
	}

	/**
	 * Marks the given clone as orphan or non-orphan depending on the <em>isOrphan</em> value.<br/>
	 * Will delete a clone which is to be marked as orphan, if <em>removeOrphanClones</em> is set.
	 */
	private void handleOrphan(IStoreProvider storeProvider, IClone clone, boolean isOrphan)
			throws IllegalArgumentException, StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("handleOrphan() - clone: " + clone + ", isOrphan: " + isOrphan);
		assert (clone != null);

		//make sure there is work for us.
		if ((isOrphan && IClone.State.ORPHAN.equals(clone.getCloneState()))
				|| (!isOrphan && !State.ORPHAN.equals(clone.getCloneState())))
		{
			//nothing to do!
			log.trace("handleOrphan() - clone state is already set correctly, nothing to do, skipping.");
			return;
		}

		//update the clone state
		if (isOrphan)
		{
			if (removeOrphanClones)
			{
				log.trace("handleOrphan() - deleting orphaned clone.");
				storeProvider.removeClone(clone);

				++deletedCloneCount;
				return;
			}

			clone.setCloneState(State.ORPHAN, 0, null);
		}
		else
		{
			clone.setCloneState(State.MODIFIED, 0, null);
		}

		++updatedCloneCount;

		//notify the store provider
		storeProvider.updateClone(clone, UpdateMode.MOVED);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.optimiser.task.AbstractGlobalCloneTask#handleDone()
	 */
	@Override
	protected void handleDone()
	{
		log.info("Orphan detection task finished - files: " + processedFileCount + " - clones processed: "
				+ processedCloneCount + ", updated: " + updatedCloneCount + ", deleted: " + deletedCloneCount);
	}

}
