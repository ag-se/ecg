package org.electrocodeogram.cpc.importexport.generic;


import org.electrocodeogram.cpc.importexport.api.generic.IGenericStatus;


/**
 * Default implementation of {@link IGenericStatus}.
 * 
 * @author vw
 */
public class GenericStatus implements IGenericStatus
{
	protected int cloneCount = 0;
	protected int cloneGroupCount = 0;
	protected int cloneFileCount = 0;
	protected int processedFileCount = 0;

	public GenericStatus()
	{
	}

	public GenericStatus(int cloneCount, int groupCount, int fileCount)
	{
		this.cloneCount = cloneCount;
		this.cloneGroupCount = groupCount;
		this.cloneFileCount = fileCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IGenericStatus#getCloneCount()
	 */
	@Override
	public int getCloneCount()
	{
		return cloneCount;
	}

	public void setCloneCount(int cloneCount)
	{
		this.cloneCount = cloneCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IGenericStatus#getGroupCount()
	 */
	@Override
	public int getCloneGroupCount()
	{
		return cloneGroupCount;
	}

	public void setCloneGroupCount(int cloneGroupCount)
	{
		this.cloneGroupCount = cloneGroupCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IGenericStatus#getFileCount()
	 */
	@Override
	public int getCloneFileCount()
	{
		return cloneFileCount;
	}

	public void setCloneFileCount(int cloneFileCount)
	{
		this.cloneFileCount = cloneFileCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IGenericStatus#getProcessedFileCount()
	 */
	@Override
	public int getProcessedFileCount()
	{
		return processedFileCount;
	}

	public void setProcessedFileCount(int processedFileCount)
	{
		this.processedFileCount = processedFileCount;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "GenericStatus[clones: " + cloneCount + ", groups: " + cloneGroupCount + ", clone files: "
				+ cloneFileCount + ", proecessed files: " + processedFileCount + "]";
	}

}
