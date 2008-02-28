package org.electrocodeogram.cpc.merge.provider;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult;
import org.electrocodeogram.cpc.core.api.provider.merge.MergeException;
import org.electrocodeogram.cpc.merge.api.strategy.ICloneObjectExtensionMergeStrategy;
import org.electrocodeogram.cpc.merge.api.strategy.ICloneObjectExtensionMerger;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeStrategy;
import org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask;
import org.electrocodeogram.cpc.merge.strategy.RemoteOverrideCloneObjectExtensionMergeStrategy;


/**
 * Simple default implementation of {@link ICloneObjectExtensionMerger}.
 * 
 * @author vw
 */
public class CloneObjectExtensionMerger implements ICloneObjectExtensionMerger
{
	private static final Log log = LogFactory.getLog(CloneObjectExtensionMerger.class);

	private List<ICloneObjectExtensionMergeStrategy> registeredStrategies = null;

	public CloneObjectExtensionMerger()
	{
		log.trace("CloneObjectExtensionMerger()");

		initialiseStrategies();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.ICloneObjectExtensionMerger#merge(org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask, org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult, org.electrocodeogram.cpc.core.api.data.ICloneObject, org.electrocodeogram.cpc.core.api.data.ICloneObject, org.electrocodeogram.cpc.core.api.data.ICloneObject, org.electrocodeogram.cpc.core.api.data.ICloneObject)
	 */
	@Override
	public void merge(IReadableMergeTask mergeTask, IMergeResult mergeResult, ICloneObject localCloneObject,
			ICloneObject remoteCloneObject, ICloneObject baseCloneObject, ICloneObject mergedCloneObject)
			throws MergeException
	{
		if (log.isTraceEnabled())
			log.trace("merge() - mergeTask: " + mergeTask + ", mergeResult: " + mergeResult + ", localCloneObject: "
					+ localCloneObject + ", remoteCloneObject: " + remoteCloneObject + ", baseCloneObject: "
					+ baseCloneObject + ", mergedCloneObject: " + mergedCloneObject);

		LinkedList<ICloneObjectExtension> pendingLocalExtensions = new LinkedList<ICloneObjectExtension>();
		pendingLocalExtensions.addAll(localCloneObject.getExtensions());

		LinkedList<ICloneObjectExtension> pendingRemoteExtensions = new LinkedList<ICloneObjectExtension>();
		pendingRemoteExtensions.addAll(remoteCloneObject.getExtensions());

		LinkedList<ICloneObjectExtension> pendingBaseExtensions = new LinkedList<ICloneObjectExtension>();
		if (baseCloneObject != null)
			pendingBaseExtensions.addAll(baseCloneObject.getExtensions());

		callStrategies(mergeTask, mergeResult, localCloneObject, remoteCloneObject, baseCloneObject, mergedCloneObject,
				pendingLocalExtensions, pendingRemoteExtensions, pendingBaseExtensions);
	}

	/**
	 * Executes all registered {@link ICloneObjectExtensionMergeStrategy}s in order of their priority till the first strategy
	 * returns {@link ICloneObjectExtensionMergeStrategy.Status#BREAK} or all strategies have been run.
	 */
	private void callStrategies(IReadableMergeTask mergeTask, IMergeResult mergeResult, ICloneObject localCloneObject,
			ICloneObject remoteCloneObject, ICloneObject baseCloneObject, ICloneObject mergedCloneObject,
			LinkedList<ICloneObjectExtension> pendingLocalExtensions,
			LinkedList<ICloneObjectExtension> pendingRemoteExtensions,
			LinkedList<ICloneObjectExtension> pendingBaseExtensions) throws MergeException
	{
		//now run each strategy in turn
		for (ICloneObjectExtensionMergeStrategy strategy : registeredStrategies)
		{
			if (log.isTraceEnabled())
				log.trace("callExtStrategies() - strategy: " + strategy);

			ICloneObjectExtensionMergeStrategy.Status status = strategy.merge(mergeTask, mergeResult, localCloneObject,
					remoteCloneObject, baseCloneObject, mergedCloneObject, pendingLocalExtensions,
					pendingRemoteExtensions, pendingBaseExtensions);

			if (log.isTraceEnabled())
				log.trace("callExtStrategies() - status: " + status);

			if (ICloneObjectExtensionMergeStrategy.Status.BREAK.equals(status))
			{
				log.trace("callExtStrategies() - aborting further execution of strategies by request of strategy: "
						+ strategy);
				break;
			}
		}

		if (log.isTraceEnabled())
			log.trace("callExtStrategies() - result: " + mergedCloneObject);
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

		registeredStrategies = new LinkedList<ICloneObjectExtensionMergeStrategy>();

		//TODO: use extension point here

		registeredStrategies.add(new RemoteOverrideCloneObjectExtensionMergeStrategy());
	}

}
