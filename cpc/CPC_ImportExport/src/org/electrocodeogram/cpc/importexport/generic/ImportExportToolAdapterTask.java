package org.electrocodeogram.cpc.importexport.generic;


import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.electrocodeogram.cpc.importexport.api.generic.IImportExportToolAdapterTask;


public class ImportExportToolAdapterTask implements IImportExportToolAdapterTask
{
	private List<IFile> files;
	private Map<String, String> options;

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IImportExportToolAdapterTask#getFiles()
	 */
	@Override
	public List<IFile> getFiles()
	{
		return files;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IImportExportToolAdapterTask#setFiles(java.util.List)
	 */
	@Override
	public void setFiles(List<IFile> files)
	{
		this.files = files;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IImportExportToolAdapterTask#getOptions()
	 */
	@Override
	public Map<String, String> getOptions()
	{
		return options;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IImportExportToolAdapterTask#setOptions(java.util.Map)
	 */
	@Override
	public void setOptions(Map<String, String> options)
	{
		this.options = options;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IImportExportToolAdapterTask#isValid()
	 */
	@Override
	public boolean isValid()
	{
		return (files != null && !files.isEmpty() && options != null);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "ImportExportToolAdapterTask[" + subToString() + "]";
	}

	protected String subToString()
	{
		return "files: " + files + ", options: " + options;
	}
}
