package org.electrocodeogram.cpc.merge.provider;


import java.util.LinkedList;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.merge.api.strategy.ICloneObjectExtensionMerger;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeContext;


public class MergeContext implements IMergeContext
{
	private ICloneObjectExtensionMerger cloneObjectExtensionMerger;

	private LinkedList<IClone> pendingLocalClones;
	private LinkedList<IClone> pendingRemoteClones;
	private LinkedList<IClone> pendingBaseClones;

	/**
	 * 
	 * @param cloneObjectExtensionMerger never null
	 * @param pendingLocalClones never null
	 * @param pendingRemoteClones never null
	 * @param pendingBaseClones never null
	 */
	public MergeContext(ICloneObjectExtensionMerger cloneObjectExtensionMerger, LinkedList<IClone> pendingLocalClones,
			LinkedList<IClone> pendingRemoteClones, LinkedList<IClone> pendingBaseClones)
	{
		assert (cloneObjectExtensionMerger != null && pendingLocalClones != null && pendingRemoteClones != null && pendingBaseClones != null);

		this.cloneObjectExtensionMerger = cloneObjectExtensionMerger;

		this.pendingLocalClones = pendingLocalClones;
		this.pendingRemoteClones = pendingRemoteClones;
		this.pendingBaseClones = pendingBaseClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IMergeContext#getCloneObjectExtensionMerger()
	 */
	@Override
	public ICloneObjectExtensionMerger getCloneObjectExtensionMerger()
	{
		return cloneObjectExtensionMerger;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IMergeContext#getPendingLocalClones()
	 */
	@Override
	public LinkedList<IClone> getPendingLocalClones()
	{
		return pendingLocalClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IMergeContext#getPendingRemoteClones()
	 */
	@Override
	public LinkedList<IClone> getPendingRemoteClones()
	{
		return pendingRemoteClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IMergeContext#getPendingBaseClones()
	 */
	@Override
	public LinkedList<IClone> getPendingBaseClones()
	{
		return pendingBaseClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IMergeContext#isLocalOrRemoteClonePending()
	 */
	@Override
	public boolean isLocalOrRemoteClonePending()
	{
		return !pendingLocalClones.isEmpty() || !pendingRemoteClones.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "MergeContext[pendingLocal: " + pendingLocalClones + ", pendingRemote: " + pendingRemoteClones
				+ ", pendingBase: " + pendingBaseClones + "]";
	}
}
