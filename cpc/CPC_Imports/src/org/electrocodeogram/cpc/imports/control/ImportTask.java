package org.electrocodeogram.cpc.imports.control;


import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.importexport.generic.ImportExportTask;
import org.electrocodeogram.cpc.imports.api.imports.IImportTask;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapterDescriptor;
import org.electrocodeogram.cpc.imports.api.imports.strategy.IImportFilterStrategyDescriptor;


/**
 * Default implementation of {@link IImportTask}.
 * 
 * @author vw
 */
public class ImportTask extends ImportExportTask implements IImportTask
{
	private static Log log = LogFactory.getLog(ImportTask.class);

	private List<IImportFilterStrategyDescriptor> importFilterStrategies;
	private Map<IImportFilterStrategyDescriptor, Map<String, String>> importFilterStrategyOptions;
	private boolean clearExistingClones;

	public ImportTask()
	{
		log.trace("ImportTask()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportTask#getImportFilterStrategies()
	 */
	@Override
	public List<IImportFilterStrategyDescriptor> getImportFilterStrategies()
	{
		return importFilterStrategies;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportTask#setImportFilterStrategies(java.util.List)
	 */
	@Override
	public void setImportFilterStrategies(List<IImportFilterStrategyDescriptor> importFilterStrategies)
	{
		if (log.isTraceEnabled())
			log.trace("setImportFilterStrategies(): " + importFilterStrategies);

		this.importFilterStrategies = importFilterStrategies;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportTask#getImportFilterStrategyOptions()
	 */
	@Override
	public Map<IImportFilterStrategyDescriptor, Map<String, String>> getImportFilterStrategyOptions()
	{
		return importFilterStrategyOptions;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportTask#setImportFilterStrategyOptions(java.util.Map)
	 */
	@Override
	public void setImportFilterStrategyOptions(
			Map<IImportFilterStrategyDescriptor, Map<String, String>> importFilterStrategyOptions)
	{
		if (log.isTraceEnabled())
			log.trace("setImportFilterStrategyOptions(): " + importFilterStrategyOptions);

		this.importFilterStrategyOptions = importFilterStrategyOptions;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportTask#isClearExistingClones()
	 */
	@Override
	public boolean isClearExistingClones()
	{
		return clearExistingClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportTask#setClearExistingClones(boolean)
	 */
	@Override
	public void setClearExistingClones(boolean clearExistingClones)
	{
		if (log.isTraceEnabled())
			log.trace("setClearExistingClones(): " + clearExistingClones);

		this.clearExistingClones = clearExistingClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.generic.ImportExportTask#isValid()
	 */
	@Override
	public boolean isValid()
	{
		//make sure all fields are set
		if (!super.isValid())
			return false;

		//make sure that the tool adapter has the right type
		if (!(toolAdapter instanceof IImportToolAdapterDescriptor))
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
		return "ImportTask[" + subToString() + ", importFilterStrategies: " + importFilterStrategies
				+ ", importFilterStrategyOptions: " + importFilterStrategyOptions + ", clearExistingClones: "
				+ clearExistingClones + "]";
	}
}
