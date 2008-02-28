package org.electrocodeogram.cpc.importexport.wizards;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericExtensionOption;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor;


public class AbstractGenericToolAdapterOptionsWizardPage extends WizardPage
{
	private static Log log = LogFactory.getLog(AbstractGenericToolAdapterOptionsWizardPage.class);

	private TableViewer tableViewer;
	private IGenericImportExportDescriptor selectedToolAdapter;

	public AbstractGenericToolAdapterOptionsWizardPage(String title, String description)
	{
		super(title);
		setTitle(title);
		setDescription(description);
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

		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());
		setControl(container);

		tableViewer = new TableViewer(container, SWT.BORDER);
		tableViewer.setContentProvider(new MyContentProvider());
		//tableViewer.setLabelProvider(new MyLabelProvider());
		final Table table = tableViewer.getTable();
		final FormData formData = new FormData();
		formData.bottom = new FormAttachment(100, 0);
		formData.right = new FormAttachment(100, 0);
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		table.setLayoutData(formData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final TableViewerColumn tableColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		tableColumn.getColumn().setWidth(200);
		tableColumn.getColumn().setText("Option");
		tableColumn.setLabelProvider(new MyColumnLabelProvider(0));

		final TableViewerColumn tableColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		tableColumn_1.getColumn().setWidth(250);
		tableColumn_1.getColumn().setText("Value");
		tableColumn_1.setLabelProvider(new MyColumnLabelProvider(1));
		tableColumn_1.setEditingSupport(new MyEditingSupport(tableViewer));

		updatePageComplete();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible)
	{
		/*
		 * Update the content before becoming visible.
		 */
		selectedToolAdapter = ((AbstractImportExportClonesWizard) getWizard()).getSelectedToolAdapter();
		assert (selectedToolAdapter != null);

		tableViewer.setInput(selectedToolAdapter);
		updatePageComplete();

		super.setVisible(visible);
	}

	/**
	 * @return a map containing all the configuration data provided by the user, never null.
	 */
	public Map<String, String> getOptionMap()
	{
		log.trace("getOptionMap()");

		int len = (selectedToolAdapter != null ? selectedToolAdapter.getOptionDefinitions().size() : 0);
		Map<String, String> result = new HashMap<String, String>(len);

		if (selectedToolAdapter != null)
			for (IGenericExtensionOption option : selectedToolAdapter.getOptionDefinitions())
				result.put(option.getId(), option.getValue());

		if (log.isTraceEnabled())
			log.trace("getOptionMap() - result: " + result);

		return result;
	}

	public List<IGenericExtensionOption> getOptionList()
	{
		if (selectedToolAdapter == null)
			return new ArrayList<IGenericExtensionOption>();

		return selectedToolAdapter.getOptionDefinitions();
	}

	/*
	 * Private methods.
	 */

	/**
	 * Listener method which is called whenever one of the form elements on this wizard page is modified.
	 */
	protected void updatePageComplete()
	{
		setPageComplete(false);

		//make sure all required options are set
		boolean allOptionsSet = true;
		if (selectedToolAdapter != null)
		{
			for (IGenericExtensionOption option : selectedToolAdapter.getOptionDefinitions())
			{
				if (option.getValue() == null)
				{
					allOptionsSet = false;
				}
			}
		}

		if (!allOptionsSet)
		{
			setMessage(null);
			setErrorMessage("You need to provide values for all mandatory (*) options in order to proceed.");
			return;
		}

		//all ok
		setPageComplete(true);

		setMessage(null);
		setErrorMessage(null);
	}

	protected class MyContentProvider implements IStructuredContentProvider
	{
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement)
		{
			if (log.isTraceEnabled())
				log.trace("MyContentProvider.getElements() - inputElement: " + inputElement);

			if (inputElement == null || !(inputElement instanceof IGenericImportExportDescriptor))
			{
				log.error("MyContentProvider.getElements() - unexpected input element type: " + inputElement,
						new Throwable());
				return null;
			}

			IGenericImportExportDescriptor descriptor = (IGenericImportExportDescriptor) inputElement;
			Object[] results = new Object[descriptor.getOptionDefinitions().size()];

			if (log.isTraceEnabled())
				log.trace("MyContentProvider.getElements() - options: " + descriptor.getOptionDefinitions());

			int i = 0;
			for (IGenericExtensionOption option : descriptor.getOptionDefinitions())
				results[i++] = option;

			if (log.isTraceEnabled())
				log.trace("MyContentProvider.getElements() - results: " + CoreUtils.arrayToString(results));

			return results;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
			//nothing to do
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose()
		{
			//nothing to dispose
		}
	}

	protected class MyColumnLabelProvider extends ColumnLabelProvider
	{
		private int columnIndex;

		MyColumnLabelProvider(int columnIndex)
		{
			this.columnIndex = columnIndex;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element)
		{
			if (element == null || (!(element instanceof IGenericExtensionOption)))
			{
				log.error("MyColumnLabelProvider.getText() - unexpected element type: " + element, new Throwable());
				return "ERROR";
			}

			IGenericExtensionOption option = (IGenericExtensionOption) element;

			switch (columnIndex)
			{
				case 0:
					//we append a * for mandatory fields (those without default values)
					return option.getName() + (option.getDefaultValue() == null ? " *" : "");
				case 1:
					return option.getValue();
				default:
					log.error("MyColumnLabelProvider.getText() - unexpected clumnIndex: " + columnIndex,
							new Throwable());
					return "ERROR";
			}
		}
	}

	protected class MyEditingSupport extends EditingSupport
	{
		private CellEditor textEditor;

		MyEditingSupport(TableViewer viewer)
		{
			super(viewer);

			if (log.isTraceEnabled())
				log.trace("MyEditingSupport() - viewer: " + viewer);

			textEditor = new TextCellEditor(viewer.getTable());
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
		 */
		@Override
		protected boolean canEdit(Object element)
		{
			if (log.isTraceEnabled())
				log.trace("MyEditingSupport.canEdit() - element: " + element + " - result: true");

			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
		 */
		@Override
		protected CellEditor getCellEditor(Object element)
		{
			if (log.isTraceEnabled())
				log.trace("MyEditingSupport.getCellEditor() - element: " + element + " - result: " + textEditor);

			return textEditor;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
		 */
		@Override
		protected Object getValue(Object element)
		{
			if (log.isTraceEnabled())
				log.trace("MyEditingSupport.getValue() - element: " + element);

			if (element == null || (!(element instanceof IGenericExtensionOption)))
			{
				log.error("MyEditingSupport.getValue() - unexpected element type: " + element, new Throwable());
				return "ERROR";
			}

			Object result = ((IGenericExtensionOption) element).getValue();
			if (result == null)
				result = "";

			if (log.isTraceEnabled())
				log.trace("MyEditingSupport.getValue() - result: " + result);

			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
		 */
		@Override
		protected void setValue(Object element, Object value)
		{
			if (log.isTraceEnabled())
				log.trace("MyEditingSupport.setValue() - element: " + element + ", value: " + value);

			if (element == null || (!(element instanceof IGenericExtensionOption)))
			{
				log.error("MyEditingSupport.setValue() - unexpected element type - element: " + element + ", value: "
						+ value, new Throwable());
				return;
			}

			((IGenericExtensionOption) element).setValue(value.toString());
			getViewer().update(element, null);

			updatePageComplete();
		}

	}
}
