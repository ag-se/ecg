package org.electrocodeogram.cpc.importexport.generic;


import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor;
import org.electrocodeogram.cpc.importexport.api.generic.IImportExportTask;


/**
 * Default implementation of {@link IImportExportTask}.<br/>
 * Usually extended by more specific task descriptors in <em>CPC Imports</em> and <em>CPC Exports</em>.
 * 
 * @author vw
 */
public class ImportExportTask implements IImportExportTask
{
	private static final Log log = LogFactory.getLog(ImportExportTask.class);

	protected IGenericImportExportDescriptor toolAdapter;
	protected Map<String, String> toolAdapterOptions;
	protected List<IProject> projects;

	public ImportExportTask()
	{
		log.trace("ImportExportTask()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IImportExportTask#getToolAdapter()
	 */
	@Override
	public IGenericImportExportDescriptor getToolAdapter()
	{
		return toolAdapter;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IImportExportTask#setToolAdapter(org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor)
	 */
	@Override
	public void setToolAdapter(IGenericImportExportDescriptor toolAdapter)
	{
		if (log.isTraceEnabled())
			log.trace("setToolAdapter(): " + toolAdapter);

		this.toolAdapter = toolAdapter;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IImportExportTask#getToolAdapterOptions()
	 */
	@Override
	public Map<String, String> getToolAdapterOptions()
	{
		return toolAdapterOptions;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IImportExportTask#setToolAdapterOptions(java.util.Map)
	 */
	@Override
	public void setToolAdapterOptions(Map<String, String> toolAdapterOptions)
	{
		if (log.isTraceEnabled())
			log.trace("setToolAdapterOptions(): " + toolAdapterOptions);

		this.toolAdapterOptions = toolAdapterOptions;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IImportExportTask#getProjects()
	 */
	@Override
	public List<IProject> getProjects()
	{
		return projects;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IImportExportTask#setProjects(java.util.List)
	 */
	@Override
	public void setProjects(List<IProject> projects)
	{
		if (log.isTraceEnabled())
			log.trace("setProjects(): " + projects);

		this.projects = projects;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IImportExportTask#isValid()
	 */
	@Override
	public boolean isValid()
	{
		return (toolAdapter != null && toolAdapterOptions != null && projects != null && !projects.isEmpty());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "ImportExportTask[" + subToString() + "]";
	}

	protected String subToString()
	{
		return "toolAdapter: " + toolAdapter + ", toolAdapterOptions: " + toolAdapterOptions + ", projects: "
				+ projects;
	}

}
