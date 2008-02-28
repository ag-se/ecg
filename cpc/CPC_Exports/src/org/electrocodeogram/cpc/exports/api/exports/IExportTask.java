package org.electrocodeogram.cpc.exports.api.exports;


import org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterDescriptor;
import org.electrocodeogram.cpc.importexport.api.generic.IImportExportTask;


/**
 * This interface represents a complete description of an export task.
 * <br>
 * It includes all configuration options and data required by the {@link IExportController} implementation.
 * <p>
 * An {@link IImportExportTask#setToolAdapter(org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor)}
 * value needs to implement {@link IExportToolAdapterDescriptor} in order for this task to be valid.
 * <p>
 * A new instance can be obtained from {@link IExportController#createTask()}.
 * 
 * @author vw
 * 
 * @see IExportController
 * @see IImportExportTask
 */
public interface IExportTask extends IImportExportTask
{
	/*
	 * Defines no additional methods.
	 */
}
