package org.electrocodeogram.cpc.merge.provider;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeProvider;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeStrategy;
import org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask;


/**
 * Default implementation for {@link IMergeTask} and {@link IReadableMergeTask}.<br/>
 * Used by {@link IMergeProvider} and {@link IMergeStrategy}.
 * 
 * @author vw
 * 
 * @see IMergeTask
 * @see IReadableMergeTask
 * @see IMergeProvider
 * @see IMergeStrategy
 */
public class MergeTask implements IMergeTask, IReadableMergeTask
{
	private ICloneFile localCloneFile;
	private List<IClone> localClones;
	private String localSourceFileContent;
	boolean localBaseInSyncHint = false;

	private ICloneFile remoteCloneFile;
	private List<IClone> remoteClones;
	private String remoteSourceFileContent;

	private ICloneFile baseCloneFile;
	private List<IClone> baseClones;
	private String baseSourceFileContent;

	private String mergedSourceFileContent;

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask#getLocalCloneFile()
	 */
	@Override
	public ICloneFile getLocalCloneFile()
	{
		return localCloneFile;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask#setLocalCloneFile(org.electrocodeogram.cpc.core.api.data.ICloneFile)
	 */
	@Override
	public void setLocalCloneFile(ICloneFile localCloneFile)
	{
		this.localCloneFile = localCloneFile;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask#getLocalClones()
	 */
	@Override
	public List<IClone> getLocalClones()
	{
		return localClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask#setLocalClones(java.util.List)
	 */
	@Override
	public void setLocalClones(List<IClone> localClones)
	{
		this.localClones = localClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask#getLocalSourceFileContent()
	 */
	@Override
	public String getLocalSourceFileContent()
	{
		return localSourceFileContent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask#setLocalSourceFileContent(java.lang.String)
	 */
	@Override
	public void setLocalSourceFileContent(String localSourceFileContent)
	{
		this.localSourceFileContent = localSourceFileContent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask#isLocalBaseInSyncHint()
	 */
	@Override
	public boolean isLocalBaseInSyncHint()
	{
		return localBaseInSyncHint;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask#setLocalBaseInSyncHint(boolean)
	 */
	@Override
	public void setLocalBaseInSyncHint(boolean localBaseInSyncHint)
	{
		this.localBaseInSyncHint = localBaseInSyncHint;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask#getRemoteCloneFile()
	 */
	@Override
	public ICloneFile getRemoteCloneFile()
	{
		return remoteCloneFile;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask#setRemoteCloneFile(org.electrocodeogram.cpc.core.api.data.ICloneFile)
	 */
	@Override
	public void setRemoteCloneFile(ICloneFile remoteCloneFile)
	{
		this.remoteCloneFile = remoteCloneFile;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask#getRemoteClones()
	 */
	@Override
	public List<IClone> getRemoteClones()
	{
		return remoteClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask#setRemoteClones(java.util.List)
	 */
	@Override
	public void setRemoteClones(List<IClone> remoteClones)
	{
		this.remoteClones = remoteClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask#getRemoteSourceFileContent()
	 */
	@Override
	public String getRemoteSourceFileContent()
	{
		return remoteSourceFileContent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask#setRemoteSourceFileContent(java.lang.String)
	 */
	@Override
	public void setRemoteSourceFileContent(String remoteSourceFileContent)
	{
		this.remoteSourceFileContent = remoteSourceFileContent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask#getBaseCloneFile()
	 */
	@Override
	public ICloneFile getBaseCloneFile()
	{
		return baseCloneFile;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask#setBaseCloneFile(org.electrocodeogram.cpc.core.api.data.ICloneFile)
	 */
	@Override
	public void setBaseCloneFile(ICloneFile baseCloneFile)
	{
		this.baseCloneFile = baseCloneFile;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask#getBaseClones()
	 */
	@Override
	public List<IClone> getBaseClones()
	{
		return baseClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask#setBaseClones(java.util.List)
	 */
	@Override
	public void setBaseClones(List<IClone> baseClones)
	{
		this.baseClones = baseClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask#getBaseSourceFileContent()
	 */
	@Override
	public String getBaseSourceFileContent()
	{
		return baseSourceFileContent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask#setBaseSourceFileContent(java.lang.String)
	 */
	@Override
	public void setBaseSourceFileContent(String baseSourceFileContent)
	{
		this.baseSourceFileContent = baseSourceFileContent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask#getMergedSourceFileContent()
	 */
	@Override
	public String getMergedSourceFileContent()
	{
		return mergedSourceFileContent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask#setMergedSourceFileContent(java.lang.String)
	 */
	@Override
	public void setMergedSourceFileContent(String mergedSourceFileContent)
	{
		this.mergedSourceFileContent = mergedSourceFileContent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask#isValid()
	 */
	@Override
	public boolean isValid()
	{
		/*
		 * Local, remote and merge data is required.
		 * Base data is optional.
		 */

		if (localCloneFile == null || localClones == null || localSourceFileContent == null)
			return false;

		if (remoteCloneFile == null || remoteClones == null || remoteSourceFileContent == null)
			return false;

		if (mergedSourceFileContent == null)
			return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask#isThreeWayMerge()
	 */
	@Override
	public boolean isThreeWayMerge()
	{
		if (!isValid())
			return false;

		if (baseCloneFile == null || baseClones == null || baseSourceFileContent == null)
			return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "MergeTask[localCloneFile: " + localCloneFile + ", localClones: " + localClones + ", localContent: "
				+ CoreStringUtils.truncateString(localSourceFileContent) + ", localBaseInSyncHint: "
				+ localBaseInSyncHint + ", remoteCloneFile: " + remoteCloneFile + ", remoteClones: " + remoteClones
				+ ", remoteContent: " + CoreStringUtils.truncateString(remoteSourceFileContent) + ", baseCloneFile: "
				+ baseCloneFile + ", baseClones: " + baseClones + ", baseContent: "
				+ CoreStringUtils.truncateString(baseSourceFileContent) + ", mergedContent: "
				+ CoreStringUtils.truncateString(mergedSourceFileContent) + "]";
	}
}
