package org.electrocodeogram.cpc.imports.api.imports;


import java.util.List;
import java.util.Map;

import org.electrocodeogram.cpc.importexport.api.generic.IImportExportTask;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapterDescriptor;
import org.electrocodeogram.cpc.imports.api.imports.strategy.IImportFilterStrategy;
import org.electrocodeogram.cpc.imports.api.imports.strategy.IImportFilterStrategyDescriptor;


/**
 * This interface represents a complete description of an import task.
 * <br>
 * It includes all configuration options and data required by the {@link IImportController} implementation.
 * <p>
 * An {@link IImportExportTask#setToolAdapter(org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor)}
 * value needs to implement {@link IImportToolAdapterDescriptor} in order for this task to be valid.
 * <p>
 * A new instance can be obtained from {@link IImportController#createTask()}.
 * 
 * @author vw
 * 
 * @see IImportController
 * @see IImportExportTask
 */
public interface IImportTask extends IImportExportTask
{
	/**
	 * Whether existing clone data should be purged before processing the import.
	 * 
	 * @return true if existing clone data should be deleted
	 */
	public boolean isClearExistingClones();

	/**
	 * Specifies whether existing clone data should be purged before processing the import.
	 * 
	 * @param clearExistingClones true if existing clone data should be deleted
	 */
	public void setClearExistingClones(boolean clearExistingClones);

	/**
	 * Retrieves a list of descriptors of all {@link IImportFilterStrategy}s which should be applied to
	 * this import.
	 * <br>
	 * If this is null the default strategies will be applied.
	 * <br>
	 * If this is an empty list, no strategies will be applied.
	 * 
	 * @return list of {@link IImportFilterStrategyDescriptor}s, may be NULL.
	 */
	public List<IImportFilterStrategyDescriptor> getImportFilterStrategies();

	/**
	 * Sets a list of descriptors of all {@link IImportFilterStrategy}s which should be applied to
	 * this import.
	 * 
	 * @param importFilterStrategies a list of {@link IImportFilterStrategyDescriptor}s, may be NULL.
	 * 
	 * @see #getImportFilterStrategies()
	 */
	public void setImportFilterStrategies(List<IImportFilterStrategyDescriptor> importFilterStrategies);

	/**
	 * Retrieves configuration options for all {@link IImportFilterStrategy}s.
	 * <p>
	 * Only {@link IImportFilterStrategy}s which defined configuration options in their
	 * extension descriptor are guaranteed to be listed in the map.
	 * <p>
	 * If {@link IImportTask#getImportFilterStrategies()} is null, this value may also be null.
	 * 
	 * @return a map which <b>may</b> contain configuration option maps for each {@link IImportFilterStrategy}, may be NULL.
	 */
	public Map<IImportFilterStrategyDescriptor, Map<String, String>> getImportFilterStrategyOptions();

	/**
	 * Sets the configuration options for all {@link IImportFilterStrategy}s.
	 * 
	 * @param importFilterStrategyOptions a map which <b>may</b> contain configuration option maps
	 * 		for each {@link IImportFilterStrategy}, may be NULL.
	 */
	public void setImportFilterStrategyOptions(
			Map<IImportFilterStrategyDescriptor, Map<String, String>> importFilterStrategyOptions);

}
