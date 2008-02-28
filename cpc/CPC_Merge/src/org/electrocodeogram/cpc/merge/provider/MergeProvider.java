package org.electrocodeogram.cpc.merge.provider;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeProvider;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask;
import org.electrocodeogram.cpc.core.api.provider.merge.MergeException;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.merge.api.strategy.ICloneObjectExtensionMergeStrategy;
import org.electrocodeogram.cpc.merge.api.strategy.ICloneObjectExtensionMerger;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeContext;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeStrategy;
import org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask;
import org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult;
import org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult.Type;
import org.electrocodeogram.cpc.merge.strategy.DropAllCloneDataFallbackStrategy;
import org.electrocodeogram.cpc.merge.strategy.FullMergeStrategy;
import org.electrocodeogram.cpc.merge.strategy.LocalFileNotModifiedStrategy;
import org.electrocodeogram.cpc.merge.strategy.LocalOrRemoteCloneDeletionsStrategy;


/**
 * Default {@link IMergeProvider} implementation.
 * <br>
 * Does some simple checking and then delegates all further merge processing to the
 * registered {@link IMergeStrategy}s.<br/>
 * <br/>
 * It is up to the registered strategies whether this {@link IMergeProvider} can
 * sensibly handle 2-way-merges or whether such merges will lead to loss of all
 * clone data.<br/>
 * <br/>
 * General cases of interest (per clone) during merging (3-way-merge):<br/>
 * <ul>
 * 	<li>clone was unchanged</li>
 * 	<li>clone was unchanged, but position might have changed due to merge</li>
 * 	<li>clone was unchanged, but position and content might have changed due to merge (could happen only on manual merge?)</li>
 * 	<li>clone was only locally moved</li>
 * 	<li>clone was only remotely moved</li>
 * 	<li>clone was locally and remotely moved</li>
 * 	<li>clone was only locally modified</li>
 * 	<li>clone was only remotely modified</li>
 * 	<li>clone was locally and remotely modified (potential merge conflict)</li>
 * 	<li>clone was only locally deleted (potential merge conflict, if remotely modified)</li>
 * 	<li>clone was only remotely deleted (potential merge conflict, if locally modified)</li>
 * 	<li>clone was locally and remotely deleted</li>
 * 	<li>clone was locally added (potential merge conflict)</li>
 * 	<li>clone was remotely added (potential merge conflict)</li>
 * 	<li>...?</li>
 * </ul>
 * <br/>
 * Special cases:<br/>
 * <ul>
 * 	<li>local and remote files have no clone data (nothing to merge)</li>
 * 	<li>only local file has no clone data (simple overwrite)</li>
 * 	<li>only remote file has no clone data (someone in the team not using CPC?)</li>
 * </ul>
 * 
 * @author vw
 */
public class MergeProvider implements IMergeProvider, IManagableProvider
{
	private static final Log log = LogFactory.getLog(MergeProvider.class);

	private List<IMergeStrategy> registeredStrategies = null;
	private ICloneObjectExtensionMerger cloneObjectExtensionMerger = null;

	public MergeProvider()
	{
		log.trace("MergeProvider()");
	}

	/*
	 * IMergeProvider methods.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeProvider#createTask()
	 */
	@Override
	public IMergeTask createTask()
	{
		log.trace("createTask()");

		return new MergeTask();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeProvider#merge(org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask)
	 */
	@Override
	public IMergeResult merge(IMergeTask iMergeTask) throws IllegalArgumentException, MergeException
	{
		if (log.isTraceEnabled())
			log.trace("merge() - merge task: " + iMergeTask);
		if (iMergeTask == null || !(iMergeTask instanceof MergeTask) || !iMergeTask.isValid())
			throw new IllegalArgumentException("illegal IMergeTask given: " + iMergeTask);

		MergeTask mergeTask = (MergeTask) iMergeTask;

		/*
		 * Initialise result object
		 */
		MergeResult mergeResult;
		try
		{
			mergeResult = new MergeResult((ICloneFile) mergeTask.getLocalCloneFile().clone());
		}
		catch (CloneNotSupportedException e)
		{
			throw new MergeException("Merge Failed - unable to clone clone file: " + mergeTask.getLocalCloneFile(), e);
		}

		/*
		 * Do some initial checks to see whether there is any work to do.
		 */
		if (mergeTask.getLocalClones().isEmpty() && mergeTask.getRemoteClones().isEmpty())
		{
			log.trace("merge() - local and remote versions have no clone data, nothing to be merged");

			mergeResult.setStatus(IMergeResult.Status.FULL_MERGE);
			//TODO: do we need to update the clonefile here somehow?
		}

		/*
		 * Some integrity checking.
		 */
		if (mergeTask.isThreeWayMerge())
		{
			//file uuids of all entries should match
			if (!mergeTask.getLocalCloneFile().getUuid().equals(mergeTask.getBaseCloneFile().getUuid())
					|| !mergeTask.getLocalCloneFile().getUuid().equals(mergeTask.getRemoteCloneFile().getUuid()))
			{
				log.warn("merge() - file uuid missmatch in 3-way-merge - local file: " + mergeTask.getLocalCloneFile()
						+ ", remote file: " + mergeTask.getRemoteCloneFile() + ", base file: "
						+ mergeTask.getBaseCloneFile() + ", merge task: " + mergeTask, new Throwable());
			}
		}
		else
		{
			//file uuids of all entries should match
			if (!mergeTask.getLocalCloneFile().getUuid().equals(mergeTask.getRemoteCloneFile().getUuid()))
			{
				log.warn("merge() - file uuid missmatch in 2-way-merge - local file: " + mergeTask.getLocalCloneFile()
						+ ", remote file: " + mergeTask.getRemoteCloneFile() + ", merge task: " + mergeTask,
						new Throwable());
			}
		}

		/*
		 * Now dispatch the merge to the registered strategies.
		 */
		LinkedList<IClone> pendingLocalClones = new LinkedList<IClone>();
		CoreUtils.cloneCloneList(mergeTask.getLocalClones(), pendingLocalClones, false);

		LinkedList<IClone> pendingRemoteClones = new LinkedList<IClone>();
		CoreUtils.cloneCloneList(mergeTask.getRemoteClones(), pendingRemoteClones, false);

		LinkedList<IClone> pendingBaseClones = new LinkedList<IClone>();
		if (mergeTask.isThreeWayMerge())
			CoreUtils.cloneCloneList(mergeTask.getBaseClones(), pendingBaseClones, false);

		IMergeContext mergeContext = new MergeContext(cloneObjectExtensionMerger, pendingLocalClones,
				pendingRemoteClones, pendingBaseClones);

		callStrategies(mergeTask, mergeResult, mergeContext);

		/*
		 * Check if there are any pending clones left.
		 * This shouldn't happen. There should always be some fallback strategy
		 * which drops all clones which couldn't be handled.
		 */
		if (!pendingLocalClones.isEmpty() || !pendingRemoteClones.isEmpty())
		{
			log
					.warn("merge() - unhandled clones remaining after all strategies have been executed, dropping clone data - pendingLocalClones: "
							+ pendingLocalClones + ", pendingRemoteClones: " + pendingRemoteClones);

			/*
			 * All still pending local clones are "dropped"/"lost" clones.
			 */

			mergeResult.addClonesLocal(pendingLocalClones, Type.LOST);
			pendingLocalClones.clear();

			mergeResult.addClonesRemote(pendingRemoteClones, Type.LOST);
			pendingRemoteClones.clear();
		}

		/*
		 * All strategies have been executed, we have the final merge result.
		 */
		if (log.isTraceEnabled())
			log.trace("merge() - final merge result: " + mergeResult);

		return mergeResult;
	}

	/*
	 * IProvider methods.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		return "CPC Merge - Merge Provider";
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onLoad()
	 */
	@Override
	public void onLoad()
	{
		log.trace("onLoad()");

		//get all registered strategies
		initialiseStrategies();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onUnload()
	 */
	@Override
	public void onUnload()
	{
		log.trace("onUnload()");
	}

	/**
	 * Executes all registered {@link IMergeStrategy}s in order of their priority till the first strategy
	 * returns {@link IMergeStrategy.Status#BREAK} or all strategies have been run.
	 */
	private void callStrategies(IReadableMergeTask mergeTask, IWriteableMergeResult mergeResult,
			IMergeContext mergeContext) throws MergeException
	{
		//now run each strategy in turn
		for (IMergeStrategy strategy : registeredStrategies)
		{
			if (log.isTraceEnabled())
				log.trace("callStrategies() - strategy: " + strategy);

			IMergeStrategy.Status status = strategy.merge(mergeTask, mergeResult, mergeContext);

			if (log.isTraceEnabled())
				log.trace("callStrategies() - status: " + status);

			if (IMergeStrategy.Status.BREAK.equals(status))
			{
				log.trace("callStrategies() - aborting further execution of strategies by request of strategy: "
						+ strategy);
				break;
			}
		}

		if (log.isTraceEnabled())
			log.trace("callStrategies() - result: " + mergeResult);
	}

	/**
	 * Retrieves all registered {@link IMergeStrategy} extensions from the
	 * corresponding extension point and adds them to the <em>registeredStrategies</em> list,
	 * ordered by descending priority.<br/>
	 * Also initialises {@link ICloneObjectExtensionMergeStrategy} extensions.
	 */
	private void initialiseStrategies()
	{
		log.trace("initialiseStrategies()");

		registeredStrategies = new LinkedList<IMergeStrategy>();

		//TODO: use extension point here

		registeredStrategies.add(new LocalFileNotModifiedStrategy());
		registeredStrategies.add(new LocalOrRemoteCloneDeletionsStrategy());
		registeredStrategies.add(new FullMergeStrategy());
		registeredStrategies.add(new DropAllCloneDataFallbackStrategy());

		//TODO: should we allow clients to replace this merger too?
		cloneObjectExtensionMerger = new CloneObjectExtensionMerger();
	}

}
