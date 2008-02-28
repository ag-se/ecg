package org.electrocodeogram.cpc.exports.api.exports.adapter;


import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.importexport.api.generic.IImportExportToolAdapterTask;


/**
 * Configuration data collection object for the {@link IExportToolAdapter}.
 * <p>
 * <b>NOTE:</b> the clone data itself is not part of the export task description.
 * The export tool adapter needs to fetch the data from the provided store provider
 * instance. The main rationale for this is that it might not always be possible
 * to load all clone data into memory. By providing a store provider instance
 * an export tool adapter can process the clone data file by file.
 * <p>
 * <b>NOTE:</b> The caller of an {@link IExportToolAdapter} does <b>not</b> hold a lock on the store provider.
 * 
 * @author vw
 */
public interface IExportToolAdapterTask extends IImportExportToolAdapterTask
{
	/**
	 * Retrieves the {@link IStoreProvider} instance which should be used to obtain the
	 * clone data for the files which were selected for this export.
	 * <p>
	 * <b>NOTE:</b> The caller of an {@link IExportToolAdapter} does <b>not</b> hold a lock on the store provider.
	 * 
	 * @return valid {@link IStoreProvider} reference, never null.
	 */
	public IStoreProvider getStoreProvider();

	/**
	 * Sets the {@link IStoreProvider} instance which should be used to obtain the
	 * clone data for the files which were selected for this export.
	 * 
	 * @param storeProvider valid {@link IStoreProvider} reference, never null.
	 */
	public void setStoreProvider(IStoreProvider storeProvider);
}
