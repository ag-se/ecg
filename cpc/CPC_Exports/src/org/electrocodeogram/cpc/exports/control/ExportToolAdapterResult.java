package org.electrocodeogram.cpc.exports.control;


import org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult;


/**
 * Default implementation for {@link IExportToolAdapterResult}.
 * 
 * @author vw
 */
public class ExportToolAdapterResult implements IExportToolAdapterResult
{
	protected int totalCloneCount = -1;
	protected int exportedCloneCount = -1;
	protected int totalCloneGroupCount = -1;
	protected int exportedCloneGroupCount = -1;
	protected int totalCloneFileCount = -1;
	protected int exportedCloneFileCount = -1;

	public ExportToolAdapterResult()
	{

	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult#getTotalCloneCount()
	 */
	@Override
	public int getTotalCloneCount()
	{
		return totalCloneCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult#setTotalCloneCount(int)
	 */
	@Override
	public void setTotalCloneCount(int totalCloneCount)
	{
		this.totalCloneCount = totalCloneCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult#getExportedCloneCount()
	 */
	@Override
	public int getExportedCloneCount()
	{
		return exportedCloneCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult#setExportedCloneCount(int)
	 */
	@Override
	public void setExportedCloneCount(int exportedCloneCount)
	{
		this.exportedCloneCount = exportedCloneCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult#getTotalCloneGroupCount()
	 */
	@Override
	public int getTotalCloneGroupCount()
	{
		return totalCloneGroupCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult#setTotalCloneGroupCount(int)
	 */
	@Override
	public void setTotalCloneGroupCount(int totalCloneGroupCount)
	{
		this.totalCloneGroupCount = totalCloneGroupCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult#getExportedCloneGroupCount()
	 */
	@Override
	public int getExportedCloneGroupCount()
	{
		return exportedCloneGroupCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult#setExportedCloneGroupCount(int)
	 */
	@Override
	public void setExportedCloneGroupCount(int exportedCloneGroupCount)
	{
		this.exportedCloneGroupCount = exportedCloneGroupCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult#getTotalCloneFileCount()
	 */
	@Override
	public int getTotalCloneFileCount()
	{
		return totalCloneFileCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult#setTotalCloneFileCount(int)
	 */
	@Override
	public void setTotalCloneFileCount(int totalCloneFileCount)
	{
		this.totalCloneFileCount = totalCloneFileCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult#getExportedCloneFileCount()
	 */
	@Override
	public int getExportedCloneFileCount()
	{
		return exportedCloneFileCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult#setExportedCloneFileCount(int)
	 */
	@Override
	public void setExportedCloneFileCount(int exportedCloneFileCount)
	{
		this.exportedCloneFileCount = exportedCloneFileCount;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "ExportToolAdapterResult[clones: " + exportedCloneCount + "/" + totalCloneCount + ", groups: "
				+ exportedCloneGroupCount + "/" + totalCloneGroupCount + ", files: " + exportedCloneFileCount + "/"
				+ totalCloneFileCount + "]";
	}
}
