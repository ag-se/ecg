package org.electrocodeogram.cpc.imports.ui.wizards;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor;
import org.electrocodeogram.cpc.importexport.wizards.AbstractSelectToolAdapterWizardPage;
import org.electrocodeogram.cpc.imports.CPCImportsPlugin;


public class SelectImportToolAdapterWizardPage extends AbstractSelectToolAdapterWizardPage
{
	private static Log log = LogFactory.getLog(SelectImportToolAdapterWizardPage.class);

	private Button clearExistingClonesCheckbox;

	public SelectImportToolAdapterWizardPage()
	{
		super("Select Import Tool Adapter", "Select one of the installed import tool adapter implementations.");
	}

	/*
	 * WizardPage methods.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.wizards.AbstractSelectToolAdapterWizardPage#subCreateControl()
	 */
	@Override
	protected void subCreateControl()
	{
		Label label = new Label(container, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);
		label.setText("Handling of existing clone data:");

		clearExistingClonesCheckbox = new Button(container, SWT.CHECK);
		clearExistingClonesCheckbox.setText("Delete existing clone data on import.");
	}

	/*
	 * CPC methods.
	 */

	/**
	 * @return true if the user selected the clear-clones-on-import option
	 */
	public boolean isClearExistingClonesEnabled()
	{
		boolean result = clearExistingClonesCheckbox.getSelection();

		if (log.isTraceEnabled())
			log.trace("getSelectedImportToolAdapter() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.wizards.AbstractSelectToolAdapterWizardPage#addToolAdapters()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void addToolAdapters()
	{
		log.trace("addToolAdapters()");

		toolAdapterDescriptors = (List) CPCImportsPlugin.getImportController().getRegisteredImportToolAdapters();
		if (log.isTraceEnabled())
			log.trace("addToolAdapters() - toolAdapterDescriptors: " + toolAdapterDescriptors);

		for (IGenericImportExportDescriptor adapter : toolAdapterDescriptors)
		{
			toolAdapterDropDown.add(adapter.getName());
		}
	}
}
