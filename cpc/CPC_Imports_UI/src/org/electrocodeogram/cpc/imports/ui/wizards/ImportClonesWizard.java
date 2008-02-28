package org.electrocodeogram.cpc.imports.ui.wizards;


import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportConfigurationOptionException;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportFailureException;
import org.electrocodeogram.cpc.importexport.wizards.AbstractImportExportClonesWizard;
import org.electrocodeogram.cpc.imports.CPCImportsPlugin;
import org.electrocodeogram.cpc.imports.api.imports.IImportController;
import org.electrocodeogram.cpc.imports.api.imports.IImportTask;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapterDescriptor;


public class ImportClonesWizard extends AbstractImportExportClonesWizard
{
	private static Log log = LogFactory.getLog(ImportClonesWizard.class);

	/*
	 * cache for values on perform finish (needed due to thread access restrictions)
	 */
	protected boolean cachedClearExistingClones;

	public ImportClonesWizard()
	{
		super("CPC - Import Static Clone Data");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.wizards.AbstractImportExportClonesWizard#subAddPages()
	 */
	@Override
	public void subAddPages()
	{
		log.trace("subAddPages()");

		welcomeWizardPage = new WelcomeWizardPage();
		selectProjectsWizardPage = new SelectProjectsWizardPage();
		selectImportToolAdapterWizardPage = new SelectImportToolAdapterWizardPage();
		genericImportToolAdapterOptionsWizardPage = new GenericImportToolAdapterOptionsWizardPage();
		readyToImportWizardPage = new ReadyToImportWizardPage();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.wizards.AbstractImportExportClonesWizard#subPerformFinish()
	 */
	@Override
	protected void subPerformFinish()
	{
		cachedClearExistingClones = isClearExistingClonesEnabled();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.wizards.AbstractImportExportClonesWizard#handleError(java.lang.reflect.InvocationTargetException)
	 */
	@Override
	protected void handleError(InvocationTargetException e)
	{
		if (e.getCause() instanceof ImportExportConfigurationOptionException)
		{
			log.warn("performFinish() - import failed due to misconfiguration - " + e);
			MessageDialog.openError(getShell(), "Configuration Error", e.getCause().getMessage());
		}
		else if (e.getCause() instanceof ImportExportFailureException)
		{
			log.error("performFinish() - import failed - " + e, e);
			MessageDialog.openError(getShell(), "Import Error", e.getCause().getMessage());
		}
		else
		{
			log.fatal("performFinish() - unexpected exception during import execution - " + e, e);
			MessageDialog.openError(getShell(), "INTERNAL ERROR", e.getCause().getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.wizards.AbstractImportExportClonesWizard#handleInterrupted(java.lang.InterruptedException)
	 */
	@Override
	protected void handleInterrupted(InterruptedException e)
	{
		log.debug("handleInterrupted() - user cancelled static clone data import.");

		MessageDialog.openInformation(getShell(), "Import Cancelled",
				"The import operation was successfully cancelled.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.wizards.AbstractImportExportClonesWizard#handleSuccess()
	 */
	@Override
	protected void handleSuccess()
	{
		log.debug("handleSuccess() - static clone data import finished successfully.");

		MessageDialog.openInformation(getShell(), "Import Successful",
				"The import operation was successfully executed.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.wizards.AbstractImportExportClonesWizard#performImportExportOperation(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void performImportExportOperation(IProgressMonitor monitor)
			throws ImportExportConfigurationOptionException, ImportExportFailureException, InterruptedException
	{
		if (log.isTraceEnabled())
			log.trace("performImportOperation() - monitor: " + monitor);

		assert (cachedSelectedProjects != null && !cachedSelectedProjects.isEmpty()
				&& cachedSelectedToolAdapter != null && cachedOptionMap != null);

		/*
		 * Execute the import.
		 */
		IImportController controller = CPCImportsPlugin.getImportController();
		assert (controller != null);
		IImportTask importTask = controller.createTask();

		assert (cachedSelectedToolAdapter instanceof IImportToolAdapterDescriptor);
		importTask.setToolAdapter(cachedSelectedToolAdapter);
		importTask.setToolAdapterOptions(cachedOptionMap);
		importTask.setImportFilterStrategies(null);
		importTask.setImportFilterStrategyOptions(null);
		importTask.setProjects(cachedSelectedProjects);
		importTask.setClearExistingClones(cachedClearExistingClones);

		controller.executeImport(monitor, importTask);
	}

	public boolean isClearExistingClonesEnabled()
	{
		return ((SelectImportToolAdapterWizardPage) selectImportToolAdapterWizardPage).isClearExistingClonesEnabled();
	}

}
