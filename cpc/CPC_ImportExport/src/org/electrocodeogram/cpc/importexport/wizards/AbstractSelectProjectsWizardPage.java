package org.electrocodeogram.cpc.importexport.wizards;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


public abstract class AbstractSelectProjectsWizardPage extends WizardPage
{
	private static Log log = LogFactory.getLog(AbstractSelectProjectsWizardPage.class);

	protected List<IProject> initiallySelectedProjects;

	protected CheckboxTableViewer checkboxTableViewer;
	protected MyModel projectCloneDataModel;

	public AbstractSelectProjectsWizardPage(String title, String description)
	{
		super(title);

		setTitle(title);
		setDescription(description);

		initiallySelectedProjects = new LinkedList<IProject>();
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
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());
		setControl(container);

		checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.V_SCROLL);
		checkboxTableViewer.setContentProvider(new MyContentProvider());
		checkboxTableViewer.setLabelProvider(new MyLabelProvider());
		final Table table = checkboxTableViewer.getTable();
		final FormData formData = new FormData();
		formData.bottom = new FormAttachment(100, 0);
		formData.right = new FormAttachment(100, 0);
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		table.setLayoutData(formData);
		table.setHeaderVisible(true);

		final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(200);
		tableColumn.setText("Project");

		final TableColumn tableColumn_1 = new TableColumn(table, SWT.NONE);
		tableColumn_1.setWidth(250);
		tableColumn_1.setText("Clone Statistics");

		//register for selection change notifies
		checkboxTableViewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				updatePageComplete();
			}
		});

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
		if (visible && projectCloneDataModel == null)
		{
			projectCloneDataModel = new MyModel();
			checkboxTableViewer.setInput(projectCloneDataModel);
		}

		//set initial selection
		Object[] checkedElements = new Object[initiallySelectedProjects.size()];
		int i = 0;
		for (IProject project : initiallySelectedProjects)
			checkedElements[i++] = new CloneProject(project, "");
		if (log.isTraceEnabled())
			log.trace("createControl() - setting checked elements: " + CoreUtils.arrayToString(checkedElements));
		checkboxTableViewer.setCheckedElements(checkedElements);

		updatePageComplete();

		super.setVisible(visible);
	}

	/*
	 * CPC methods.
	 */

	/**
	 * Takes an optional use selection at the startup time of the import wizard and uses
	 * it to pre-select any projects which were selected, when the wizard was launched.
	 */
	public void init(IStructuredSelection initialSelection)
	{
		if (log.isTraceEnabled())
			log.trace("init() - initialSelection: " + initialSelection);

		if (initialSelection != null)
		{
			if (initialSelection instanceof IStructuredSelection)
			{
				for (Object selectionItem : ((IStructuredSelection) initialSelection).toList())
				{
					if (selectionItem instanceof IProject)
					{
						initiallySelectedProjects.add((IProject) selectionItem);
					}
					else
					{
						log
								.warn("init() - expected IProject selection item, but got: " + selectionItem
										+ " - ignored.");
					}
				}
			}
			else
			{
				log.debug("init() - selection is not an IStructuredSelection, ignoring");
			}
		}
		else
		{
			log.debug("init() - selection is null, ignoring");
		}

		if (log.isTraceEnabled())
			log.trace("init() - selected projects: " + initiallySelectedProjects);
	}

	/**
	 * A list of projects which the user selected to be processed for this im/export.
	 * 
	 * @return list of selected projects, never null.
	 */
	public List<IProject> getSelectedProjects()
	{
		log.trace("getSelectedProjects()");

		List<IProject> result = new LinkedList<IProject>();
		for (Object checkedElement : checkboxTableViewer.getCheckedElements())
		{
			if (!(checkedElement instanceof CloneProject))
			{
				log.error("getSelectedProjects() - unexpected object type in table - element: " + checkedElement,
						new Throwable());
				continue;
			}

			result.add(((CloneProject) checkedElement).getProject());
		}

		if (log.isTraceEnabled())
			log.trace("getSelectedProjects() - result: " + result);

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
		setPageComplete(false);

		//make sure at least one project is selected
		if (getSelectedProjects().isEmpty())
		{
			setMessage(null);
			setErrorMessage("You need to select at least one project in order to proceed.");
			return;
		}

		//all ok
		setPageComplete(true);

		//TODO: maybe check here whether any of the projects selected for import already has
		//		clone data stored and indicate this fact to the user?
		/*
		if (!sourceDirPath.equals(destinationDirPath))
		{
			setErrorMessage(null);
			setMessage("The plugin.properties file is typically" + " located in the same directory"
					+ " as the plugin.xml file", WARNING);
			return;
		}
		*/

		setMessage(null);
		setErrorMessage(null);
	}

	private class CloneProject
	{
		private IProject project;
		private String statistics;

		/**
		 * 
		 * @param project never null
		 * @param statistics never null
		 */
		CloneProject(IProject project, String statistics)
		{
			assert (project != null && statistics != null);

			this.project = project;
			this.statistics = statistics;
		}

		/**
		 * 
		 * @return never null
		 */
		public IProject getProject()
		{
			return project;
		}

		/**
		 * 
		 * @return never null
		 */
		public String getStatistics()
		{
			return statistics;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((project == null) ? 0 : project.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final CloneProject other = (CloneProject) obj;
			if (project == null)
			{
				if (other.project != null)
					return false;
			}
			else if (!project.equals(other.project))
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
			return "CloneProject[project: " + project + ", statistics: " + statistics + "]";
		}
	}

	private class MyModel
	{

	}

	private class MyContentProvider implements IStructuredContentProvider
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

			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			Object[] results = new Object[projects.length];

			if (log.isTraceEnabled())
				log.trace("MyContentProvider.getElements() - projects: " + CoreUtils.arrayToString(projects));

			for (int i = 0; i < projects.length; ++i)
			{
				results[i] = new CloneProject(projects[i], "");
			}

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
			//this doesn't happen
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

	private class MyLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex)
		{
			if (element == null || (!(element instanceof CloneProject)))
			{
				log.error("MyLabelProvider.getText() - unexpected element type: " + element, new Throwable());
				return "ERROR";
			}

			CloneProject cloneProject = (CloneProject) element;

			switch (columnIndex)
			{
				case 0:
					return cloneProject.getProject().getName();
				case 1:
					return cloneProject.getStatistics();
				default:
					log.error("MyLabelProvider.getText() - unexpected clumnIndex: " + columnIndex, new Throwable());
					return "ERROR";
			}
		}
	}

}
