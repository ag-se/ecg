package org.electrocodeogram.cpc.store.remote.sql.provider;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRevision;


public class CPCRevision implements ICPCRevision
{
	protected String revisionId;
	protected ICloneFile cloneFile;
	protected List<IClone> clones;

	public CPCRevision()
	{

	}

	public CPCRevision(String revisionId, ICloneFile cloneFile, List<IClone> clones)
	{
		assert (revisionId != null && cloneFile != null && clones != null);

		this.revisionId = revisionId;
		this.cloneFile = cloneFile;
		this.clones = clones;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.remote.sql.api.adapter.ICPCRevision#getRevisionId()
	 */
	@Override
	public String getRevisionId()
	{
		return revisionId;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.remote.sql.api.adapter.ICPCRevision#setRevisionId(java.lang.String)
	 */
	@Override
	public void setRevisionId(String revisionId)
	{
		this.revisionId = revisionId;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.remote.sql.api.adapter.ICPCRevision#getCloneFile()
	 */
	@Override
	public ICloneFile getCloneFile()
	{
		return cloneFile;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.remote.sql.api.adapter.ICPCRevision#setCloneFile(org.electrocodeogram.cpc.core.api.data.ICloneFile)
	 */
	@Override
	public void setCloneFile(ICloneFile cloneFile)
	{
		this.cloneFile = cloneFile;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.remote.sql.api.adapter.ICPCRevision#getClones()
	 */
	@Override
	public List<IClone> getClones()
	{
		return clones;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.remote.sql.api.adapter.ICPCRevision#setClones(java.util.List)
	 */
	@Override
	public void setClones(List<IClone> clones)
	{
		this.clones = clones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.remote.sql.api.adapter.ICPCRevision#isValid()
	 */
	@Override
	public boolean isValid()
	{
		if (revisionId == null || cloneFile == null || clones == null)
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
		return "CPCRevision[rev: " + revisionId + ", cloneFile: " + cloneFile + ", clones: " + clones + "]";
	}

}
