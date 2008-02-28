package org.electrocodeogram.cpc.merge.provider;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeProvider;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResultPerspective;
import org.electrocodeogram.cpc.core.utils.CoreClonePositionUtils;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeStrategy;
import org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult;


/**
 * Default implementation of {@link IMergeResult} and {@link IWriteableMergeResult}.<br/>
 * Used by {@link IMergeProvider} and {@link IMergeStrategy}.
 * 
 * @author vw
 * 
 * @see IMergeResult
 * @see IWriteableMergeResult
 * @see IMergeProvider
 * @see IMergeStrategy
 * @see MergeResultPerspective
 */
public class MergeResult implements IMergeResult, IWriteableMergeResult
{
	private static final Log log = LogFactory.getLog(MergeResult.class);

	private Status status;
	private ICloneFile cloneFile;
	private MergeResultPerspective localPerspective;
	private MergeResultPerspective remotePerspective;
	private List<IClone> cachedMergedClones = null;

	/**
	 * Creates a new {@link MergeResult} instance.<br/>
	 * The given {@link ICloneFile} instance should be a <b>cloned instance</b>.<br/>
	 * All clone lists are initialised as empty lists.
	 * 
	 * @param cloneFile a to be cloned {@link ICloneFile} instance for this result object.
	 */
	public MergeResult(ICloneFile cloneFile)
	{
		status = Status.NO_MERGE;

		this.cloneFile = cloneFile;

		localPerspective = new MergeResultPerspective("local");
		remotePerspective = new MergeResultPerspective("remote");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult#getStatus()
	 */
	@Override
	public Status getStatus()
	{
		return status;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult#setStatus(org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult.Status)
	 */
	@Override
	public void setStatus(Status status)
	{
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult#isFullyMerged()
	 */
	@Override
	public boolean isFullyMerged()
	{
		return Status.FULL_MERGE.equals(status);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult#getCloneFile()
	 */
	@Override
	public ICloneFile getCloneFile()
	{
		return cloneFile;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult#getLocalPerspective()
	 */
	@Override
	public IMergeResultPerspective getLocalPerspective()
	{
		return localPerspective;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult#getRemotePerspective()
	 */
	@Override
	public IMergeResultPerspective getRemotePerspective()
	{
		return remotePerspective;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult#addClone(org.electrocodeogram.cpc.core.api.data.IClone, org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult.Type, org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult.Type)
	 */
	@Override
	public void addClone(IClone clone, Type localType, Type remoteType)
	{
		addCloneLocal(clone, localType);
		addCloneRemote(clone, remoteType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult#addCloneLocal(org.electrocodeogram.cpc.core.api.data.IClone, org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult.Type)
	 */
	@Override
	public void addCloneLocal(IClone clone, Type localType)
	{
		localPerspective.addClone(clone, localType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult#addCloneRemote(org.electrocodeogram.cpc.core.api.data.IClone, org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult.Type)
	 */
	@Override
	public void addCloneRemote(IClone clone, Type remoteType)
	{
		remotePerspective.addClone(clone, remoteType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult#addClones(java.util.Collection, org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult.Type, org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult.Type)
	 */
	@Override
	public void addClones(Collection<IClone> clones, Type localType, Type remoteType)
	{
		for (IClone clone : clones)
			addClone(clone, localType, remoteType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult#addClonesLocal(java.util.Collection, org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult.Type)
	 */
	@Override
	public void addClonesLocal(Collection<IClone> clones, Type localType)
	{
		for (IClone clone : clones)
			addCloneLocal(clone, localType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult#addClonesRemote(java.util.Collection, org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult.Type)
	 */
	@Override
	public void addClonesRemote(Collection<IClone> clones, Type remoteType)
	{
		for (IClone clone : clones)
			addCloneRemote(clone, remoteType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult#getFinalMergedClones()
	 */
	@Override
	public List<IClone> getMergedClones()
	{
		if (cachedMergedClones == null)
		{
			//not yet cached, generate data.
			int maxLen = localPerspective.getAddedClones().size() + localPerspective.getMovedClones().size()
					+ localPerspective.getModifiedClones().size() + localPerspective.getUnchangedClones().size();
			Set<IClone> finalMergedClones = new HashSet<IClone>(maxLen);

			finalMergedClones.addAll(localPerspective.getAddedClones());
			finalMergedClones.addAll(localPerspective.getMovedClones());
			finalMergedClones.addAll(localPerspective.getModifiedClones());
			finalMergedClones.addAll(localPerspective.getUnchangedClones());

			//the API specification does not require us to sort the list, but we need it for
			//internal debugging purposes.
			List<IClone> tmpList = new ArrayList<IClone>(finalMergedClones);
			Collections.sort(tmpList);
			cachedMergedClones = Collections.unmodifiableList(tmpList);

			if (CPCCorePlugin.isDebugChecking())
			{
				/*
				 * The merged clone data should be the same. No matter whether we build it from the
				 * local perspective diff data or from the remote perspective diff data.
				 */
				Set<IClone> tmpMergedClones = new HashSet<IClone>(maxLen);

				tmpMergedClones.addAll(remotePerspective.getAddedClones());
				tmpMergedClones.addAll(remotePerspective.getMovedClones());
				tmpMergedClones.addAll(remotePerspective.getModifiedClones());
				tmpMergedClones.addAll(remotePerspective.getUnchangedClones());

				List<IClone> tmpMergedClonesList = new ArrayList<IClone>(tmpMergedClones);
				Collections.sort(tmpMergedClonesList);
				if (!CoreClonePositionUtils.cloneListsEqual(cachedMergedClones, tmpMergedClonesList))
				{
					log.error(
							"getMergedClones() - merged clone data differs for local and remote perspective - merged(local): "
									+ cachedMergedClones + ", merged(remote): " + tmpMergedClonesList + ", this: "
									+ this, new Throwable());
				}
			}
		}

		return cachedMergedClones;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "MergeResult[status: " + status + ", cloneFile: " + cloneFile + ", localPerspective: "
				+ localPerspective + ", remotePerspective: " + remotePerspective + "]";
	}
}
