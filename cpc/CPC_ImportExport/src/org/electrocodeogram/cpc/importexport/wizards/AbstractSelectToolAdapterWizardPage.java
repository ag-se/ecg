package org.electrocodeogram.cpc.importexport.wizards;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor;


public abstract class AbstractSelectToolAdapterWizardPage extends WizardPage
{
	private static Log log = LogFactory.getLog(AbstractSelectToolAdapterWizardPage.class);

	protected Composite container;
	protected Combo toolAdapterDropDown;
	protected List<IGenericImportExportDescriptor> toolAdapterDescriptors;

	public AbstractSelectToolAdapterWizardPage(String title, String description)
	{
		super(title);
		setTitle(title);
		setDescription(description);

		toolAdapterDescriptors = new LinkedList<IGenericImportExportDescriptor>();
	}

	/*
	 * WizardPage methods.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent)
	{
		if (log.isTraceEnabled())
			log.trace("createControl() - parent: " + parent);

		container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		setControl(container);

		GridData gridData;
		Label label;

		label = new Label(container, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);
		label.setText("Tool Adapter Implementation:");

		toolAdapterDropDown = new Combo(container, SWT.DROP_DOWN | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		toolAdapterDropDown.setLayoutData(gridData);

		toolAdapterDropDown.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				updatePageComplete();
			}

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				updatePageComplete();
			}
		});

		subCreateControl();

		/*
		 * Add content. 
		 */
		addToolAdapters();

		updatePageComplete();
	}

	/**
	 * Can be used to add additional content to the page, below the tool adapter dropdown.<br/>
	 * The default implementation does nothing.
	 */
	protected void subCreateControl()
	{

	}

	/*
	 * CPC methods.
	 */

	/**
	 * @return currently selected import tool adapter, NULL if no adapter is selected.
	 */
	public IGenericImportExportDescriptor getSelectedToolAdapter()
	{
		int idx = toolAdapterDropDown.getSelectionIndex();
		if (idx < 0)
			return null;

		IGenericImportExportDescriptor result = toolAdapterDescriptors.get(idx);

		if (log.isTraceEnabled())
			log.trace("getSelectedImportToolAdapter() - result: " + result);

		return result;
	}

	/*
	 * Private methods.
	 */

	/**
	 * Listener method which is called whenever one of the form elements on this wizard page is modified.
	 */
	protected void updatePageComplete()
	{
		log.trace("updatePageComplete()");

		setPageComplete(false);

		//make sure a provider was selected
		if (getSelectedToolAdapter() == null)
		{
			setMessage(null);
			setErrorMessage("You need to select a tool adapter implementation.");
			return;
		}

		//all ok
		setPageComplete(true);

		setMessage(null);
		setErrorMessage(null);
	}

	/**
	 * Adds the available {@link IGenericImportExportDescriptor}s to the <em>toolAdapterDescriptors</em> list.
	 */
	protected abstract void addToolAdapters();
}
