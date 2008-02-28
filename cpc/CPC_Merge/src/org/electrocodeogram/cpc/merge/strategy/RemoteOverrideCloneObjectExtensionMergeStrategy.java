package org.electrocodeogram.cpc.merge.strategy;


import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult;
import org.electrocodeogram.cpc.merge.api.strategy.ICloneObjectExtensionMergeStrategy;
import org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask;


/**
 * A very simple {@link ICloneObjectExtensionMergeStrategy} which simply gives total precedence to
 * all remote extension data and discards all local and base extension data.<br/>
 * <br/>
 * This may be a suitable fallback strategy.<br/>
 * <br/>
 * Supports 2-way-merges.
 * 
 * @author vw
 */
public class RemoteOverrideCloneObjectExtensionMergeStrategy implements ICloneObjectExtensionMergeStrategy
{
	private static final Log log = LogFactory.getLog(RemoteOverrideCloneObjectExtensionMergeStrategy.class);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.ICloneObjectExtensionMergeStrategy#merge(org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask, org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult, org.electrocodeogram.cpc.core.api.data.ICloneObject, org.electrocodeogram.cpc.core.api.data.ICloneObject, org.electrocodeogram.cpc.core.api.data.ICloneObject, org.electrocodeogram.cpc.core.api.data.ICloneObject, java.util.LinkedList, java.util.LinkedList, java.util.LinkedList)
	 */
	@Override
	public Status merge(IReadableMergeTask mergeTask, IMergeResult mergeResult, ICloneObject localCloneObject,
			ICloneObject remoteCloneObject, ICloneObject baseCloneObject, ICloneObject mergedCloneObject,
			LinkedList<ICloneObjectExtension> pendingLocalExtensions,
			LinkedList<ICloneObjectExtension> pendingRemoteExtensions,
			LinkedList<ICloneObjectExtension> pendingBaseExtensions)
	{
		if (log.isTraceEnabled())
			log.trace("merge() - mergeTask: " + mergeTask + ", mergeResult: " + mergeResult + ", localCloneObject: "
					+ localCloneObject + ", remoteCloneObject: " + remoteCloneObject + ", baseCloneObject: "
					+ baseCloneObject + ", mergedCloneObject: " + mergedCloneObject + ", pendingLocalExtensions: "
					+ pendingLocalExtensions + ", pendingRemoteExtensions: " + pendingRemoteExtensions
					+ ", pendingBaseExtensions: " + pendingBaseExtensions);

		boolean extModified = false;
		boolean extLost = false;

		if (!pendingRemoteExtensions.isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("merge() - keeping former remote extensions: " + pendingRemoteExtensions);

			for (ICloneObjectExtension remoteExt : pendingRemoteExtensions)
			{
				mergedCloneObject.addExtension(remoteExt);
			}
			pendingRemoteExtensions.clear();

			extModified = true;
		}

		if (!pendingLocalExtensions.isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("merge() - discarding former local extensions: " + pendingLocalExtensions);
			pendingLocalExtensions.clear();

			extLost = true;
		}

		if (!pendingBaseExtensions.isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("merge() - discarding former base extensions: " + pendingBaseExtensions);
			pendingBaseExtensions.clear();

			extLost = true;
		}

		if (extLost)
			return Status.PARTIAL;
		else if (extModified)
			return Status.FULL;
		else
			return Status.SKIPPED;
	}

}
