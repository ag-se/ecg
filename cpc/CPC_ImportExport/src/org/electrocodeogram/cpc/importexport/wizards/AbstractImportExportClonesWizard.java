package org.electrocodeogram.cpc.importexport.wizards;


import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericExtensionOption;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportConfigurationOptionException;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportFailureException;
import org.electrocodeogram.cpc.importexport.api.wizards.IImportExportExtensionOptionWizardPage;
import org.electrocodeogram.cpc.importexport.api.wizards.IImportExportExtensionOptionWizardPageDescriptor;


/**
 * Generic import/export wizard which is used by <em>CPC Imports</em> and <em>CPC Exports</em>.
 * 
 * @author vw
 */
public abstract class AbstractImportExportClonesWizard extends Wizard
{
	private static Log log = LogFactory.getLog(AbstractImportExportClonesWizard.class);

	protected IStructuredSelection initialSelection;

	protected AbstractWelcomeWizardPage welcomeWizardPage;
	protected AbstractSelectProjectsWizardPage selectProjectsWizardPage;
	protected AbstractSelectToolAdapterWizardPage selectImportToolAdapterWizardPage;
	protected AbstractGenericToolAdapterOptionsWizardPage genericImportToolAdapterOptionsWizardPage;
	protected AbstractReadyToImportExportWizardPage readyToImportWizardPage;
	protected List<IImportExportExtensionOptionWizardPage> extraOptionPages;

	protected Map<String, String> currentOptionMap;

	/*
	 * cache for values on perform finish (needed due to thread access restrictions)
	 */
	protected IGenericImportExportDescriptor cachedSelectedToolAdapter;
	protected List<IProject> cachedSelectedProjects;
	protected Map<String, String> cachedOptionMap;

	protected String title;

	public AbstractImportExportClonesWizard(String title)
	{
		this.title = title;

		currentOptionMap = new HashMap<String, String>(10);
		extraOptionPages = new LinkedList<IImportExportExtensionOptionWizardPage>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages()
	{
		log.trace("addPages()");

		setWindowTitle(title);

		subAddPages();

		addPage(welcomeWizardPage);
		addPage(selectProjectsWizardPage);
		addPage(selectImportToolAdapterWizardPage);
		addPage(genericImportToolAdapterOptionsWizardPage);
		addPage(readyToImportWizardPage);

		selectProjectsWizardPage.init(initialSelection);
	}

	/**
	 * Initialises the protected page variables with instances of the correct wizard pages.
	 */
	protected abstract void subAddPages();

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page)
	{
		//check if we need to add new option pages
		if (page == selectImportToolAdapterWizardPage)
		{
			/*
			 * We're just leaving the import tool adapter wizard page.
			 * Lets clean out all old extra option pages.
			 * And add new ones for the given adapter.
			 */
			log.trace("getNextPage() - cleaning out old option pages.");
			extraOptionPages.clear();

			List<IImportExportExtensionOptionWizardPageDescriptor> optionPageDescriptors = selectImportToolAdapterWizardPage
					.getSelectedToolAdapter().getOptionPages();
			if (!optionPageDescriptors.isEmpty())
			{
				if (log.isTraceEnabled())
					log.trace("getNextPage() - adding new option pages: " + optionPageDescriptors);

				//create instances of all pages and add them to the wizard.
				for (IImportExportExtensionOptionWizardPageDescriptor descriptor : optionPageDescriptors)
				{
					IImportExportExtensionOptionWizardPage optionPage;
					try
					{
						optionPage = ((ImportExportExtensionOptionWizardPageDescriptor) descriptor).getInstance();
					}
					catch (CoreException e)
					{
						log.error("getNextPage() - unable to create new option page, skipping - " + descriptor + " - "
								+ e, e);
						continue;
					}
					optionPage.init(currentOptionMap);
					extraOptionPages.add(optionPage);
					addPage(optionPage);
				}

				//the first new option page is the next page
				return extraOptionPages.get(0);
			}
		}

		//we might also be somewhere inside the option pages atm
		int idx = extraOptionPages.indexOf(page);
		if (idx >= 0)
		{
			/*
			 * Ok, this is an option page.
			 * If there is a followup page, use that one, otherwise
			 * jump to the final page.
			 */
			if (extraOptionPages.size() > idx + 1)
			{
				//ok, the followup page is the next page
				return extraOptionPages.get(idx + 1);
			}
			else
			{
				//jump to generic options page or final page
				//depending on whether generic options are set or not
				if (!selectImportToolAdapterWizardPage.getSelectedToolAdapter().getOptionDefinitions().isEmpty())
				{
					//generic options page is next
					return genericImportToolAdapterOptionsWizardPage;
				}
				else
				{
					//final ready page is next
					return readyToImportWizardPage;
				}
			}
		}

		//don't allow moving to any of the followup pages
		if (page == readyToImportWizardPage)
			return null;

		return super.getNextPage(page);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	@Override
	public IWizardPage getPreviousPage(IWizardPage page)
	{
		/*
		 * We need to do some special previous handling, if we're on the ready page
		 * or the generic options page or within some extra options pages. 
		 */
		int idx = extraOptionPages.indexOf(page);
		if (idx >= 0)
		{
			/*
			 * Ok, this is an option page.
			 * If there is a previous page, use that one, otherwise
			 * jump to the tool adapter selection page.
			 */
			if (idx > 0)
			{
				return extraOptionPages.get(idx - 1);
			}
			else
			{
				return selectImportToolAdapterWizardPage;
			}
		}

		/*
		 * If we're on the final page, we need to know whether there are generic options or
		 * extra option pages.
		 */
		if (page == readyToImportWizardPage)
		{
			if (!selectImportToolAdapterWizardPage.getSelectedToolAdapter().getOptionDefinitions().isEmpty())
			{
				//generic options page was the previous page
				return genericImportToolAdapterOptionsWizardPage;
			}
			else if (!extraOptionPages.isEmpty())
			{
				//there are option pages, jump to the last one of them
				return extraOptionPages.get(extraOptionPages.size() - 1);
			}
			else
			{
				//back to the tool adapter selection page
				return selectImportToolAdapterWizardPage;
			}
		}

		/*
		 * If we're on the extra options page, we may need to jump to the last extra options page. 
		 */
		if (page == genericImportToolAdapterOptionsWizardPage)
		{
			if (!extraOptionPages.isEmpty())
			{
				//there are option pages, jump to the last one of them
				return extraOptionPages.get(extraOptionPages.size() - 1);
			}
		}

		return super.getPreviousPage(page);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#needsProgressMonitor()
	 */
	@Override
	public boolean needsProgressMonitor()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	@Override
	public boolean canFinish()
	{
		//check if anything is preventing our super class from finishing
		if (!super.canFinish())
			return false;

		//otherwise we can finish, once the last non-result page was completed
		return readyToImportWizardPage.isPageComplete();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish()
	{
		log.trace("performFinish()");

		//cache values
		cachedSelectedToolAdapter = getSelectedToolAdapter();
		cachedSelectedProjects = getSelectedProjects();
		cachedOptionMap = getOptionMap();

		subPerformFinish();

		try
		{
			//FIXME: this doesn't display a progressbar yet
			getContainer().run(true, true, new IRunnableWithProgress()
			{
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					try
					{
						performImportExportOperation(monitor);
					}
					catch (Exception e)
					{
						//re-throw all exceptions as InterruptedException or InvocationTargetException
						if (e instanceof InterruptedException)
							throw (InterruptedException) e;
						else if (e instanceof InvocationTargetException)
							throw (InvocationTargetException) e;
						else
							throw new InvocationTargetException(e);
					}
				}
			});
		}
		catch (InvocationTargetException e)
		{
			handleError(e);
			return false;
		}
		catch (InterruptedException e)
		{
			// User canceled, so stop but don't close wizard.
			log.debug("performFinish() - user cancelled operation");
			MessageDialog.openInformation(getShell(), "Operation Cancelled",
					"The operation was successfully cancelled.");
			return false;
		}

		handleSuccess();

		//ok, close the wizard
		return true;
	}

	/**
	 * Executed before the operation is dispatched.<br/>
	 * Can be used to initialise more caching structures.<br/>
	 * The default implementation does nothing.
	 */
	protected void subPerformFinish()
	{

	}

	/**
	 * Called if the {@link AbstractImportExportClonesWizard#performImportExportOperation(IProgressMonitor)} method
	 * throws any exception during execution.
	 */
	protected abstract void handleError(InvocationTargetException e);

	/**
	 * Called if the user cancelled the import/export operation.
	 */
	protected abstract void handleInterrupted(InterruptedException e);

	/**
	 * Called after the import/export operation was executed successfully.<br/>
	 * Will usually open a popup window.
	 */
	protected abstract void handleSuccess();

	/**
	 * Executes the clone data import. 
	 */
	protected abstract void performImportExportOperation(IProgressMonitor monitor)
			throws ImportExportConfigurationOptionException, ImportExportFailureException, InterruptedException;

	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
		if (log.isTraceEnabled())
			log.trace("init() - workbench: " + workbench + ", selection: " + selection);

		this.initialSelection = selection;
	}

	public List<IProject> getSelectedProjects()
	{
		return selectProjectsWizardPage.getSelectedProjects();
	}

	public IGenericImportExportDescriptor getSelectedToolAdapter()
	{
		return selectImportToolAdapterWizardPage.getSelectedToolAdapter();
	}

	public Map<String, String> getOptionMap()
	{
		//make sure generic options are set too
		currentOptionMap.putAll(genericImportToolAdapterOptionsWizardPage.getOptionMap());
		return currentOptionMap;
	}

	public List<IGenericExtensionOption> getOptionList()
	{
		return genericImportToolAdapterOptionsWizardPage.getOptionList();
	}

}
