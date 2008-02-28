package org.electrocodeogram.cpc.exports.ui.wizards;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.exports.CPCExportsPlugin;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor;
import org.electrocodeogram.cpc.importexport.wizards.AbstractSelectToolAdapterWizardPage;


public class SelectExportToolAdapterWizardPage extends AbstractSelectToolAdapterWizardPage
{
	private static Log log = LogFactory.getLog(SelectExportToolAdapterWizardPage.class);

	public SelectExportToolAdapterWizardPage()
	{
		super("Select Export Tool Adapter", "Select one of the installed export tool adapter implementations.");
	}

	/*
	 * CPC methods.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.wizards.AbstractSelectToolAdapterWizardPage#addToolAdapters()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void addToolAdapters()
	{
		log.trace("addToolAdapters()");

		toolAdapterDescriptors = (List) CPCExportsPlugin.getExportController().getRegisteredExportToolAdapters();
		if (log.isTraceEnabled())
			log.trace("addToolAdapters() - toolAdapterDescriptors: " + toolAdapterDescriptors);

		for (IGenericImportExportDescriptor adapter : toolAdapterDescriptors)
		{
			toolAdapterDropDown.add(adapter.getName());
		}
	}

}
