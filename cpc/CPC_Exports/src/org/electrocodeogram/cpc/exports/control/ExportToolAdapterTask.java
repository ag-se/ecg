package org.electrocodeogram.cpc.exports.control;


import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterTask;
import org.electrocodeogram.cpc.importexport.generic.ImportExportToolAdapterTask;


/**
 * Default implementation of {@link IExportToolAdapterTask}.
 * 
 * @author vw
 */
public class ExportToolAdapterTask extends ImportExportToolAdapterTask implements IExportToolAdapterTask
{
	protected IStoreProvider storeProvider = null;

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterTask#getStoreProvider()
	 */
	@Override
	public IStoreProvider getStoreProvider()
	{
		return storeProvider;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterTask#setStoreProvider(org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider)
	 */
	@Override
	public void setStoreProvider(IStoreProvider storeProvider)
	{
		this.storeProvider = storeProvider;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.generic.ImportExportToolAdapterTask#isValid()
	 */
	@Override
	public boolean isValid()
	{
		if (!super.isValid())
			return false;

		if (storeProvider == null)
			return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.generic.ImportExportToolAdapterTask#toString()
	 */
	@Override
	public String toString()
	{
		return "ExportToolAdapterTask[" + subToString() + ", storeProvider: " + storeProvider + "]";
	}

}
