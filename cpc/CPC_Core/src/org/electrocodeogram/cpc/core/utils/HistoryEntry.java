package org.electrocodeogram.cpc.core.utils;


import org.electrocodeogram.cpc.core.api.data.CloneDiff;


/**
 * Result object for {@link CoreHistoryUtils#getCloneAllContents(org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider, org.electrocodeogram.cpc.core.api.data.IClone, org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension)}.
 * 
 * @author vw
 * 
 * @see CoreHistoryUtils
 * @see CloneDiff
 */
public class HistoryEntry
{
	private CloneDiff diff;
	private String contentAfterDiff;

	/**
	 * For use in {@link CoreHistoryUtils} only.
	 * 
	 * @param diff the {@link CloneDiff} element for this entry, never null.
	 * @param contentAfterDiff the clone content <b>after</b> the {@link CloneDiff} of this entry has been applied, never null.
	 */
	HistoryEntry(CloneDiff diff, String contentAfterDiff)
	{
		assert (diff != null && contentAfterDiff != null);

		this.diff = diff;
		this.contentAfterDiff = contentAfterDiff;
	}

	/**
	 * @return the {@link CloneDiff} element for this entry, never null.
	 */
	public CloneDiff getDiff()
	{
		return diff;
	}

	/**
	 * @return the clone content <b>after</b> the {@link CloneDiff} of this entry has been applied, never null.
	 */
	public String getContentAfterDiff()
	{
		return contentAfterDiff;
	}
}
