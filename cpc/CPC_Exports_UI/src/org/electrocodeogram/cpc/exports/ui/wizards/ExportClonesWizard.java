package org.electrocodeogram.cpc.exports.ui.wizards;


import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.electrocodeogram.cpc.exports.CPCExportsPlugin;
import org.electrocodeogram.cpc.exports.api.exports.IExportController;
import org.electrocodeogram.cpc.exports.api.exports.IExportTask;
import org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterDescriptor;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportConfigurationOptionException;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportFailureException;
import org.electrocodeogram.cpc.importexport.wizards.AbstractImportExportClonesWizard;


public class ExportClonesWizard extends AbstractImportExportClonesWizard
{
	private static Log log = LogFactory.getLog(ExportClonesWizard.class);

	public ExportClonesWizard()
	{
		super("CPC - Export Clone Data");
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
		selectImportToolAdapterWizardPage = new SelectExportToolAdapterWizardPage();
		genericImportToolAdapterOptionsWizardPage = new GenericExportToolAdapterOptionsWizardPage();
		readyToImportWizardPage = new ReadyToExportWizardPage();
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
			log.warn("performFinish() - export failed due to misconfiguration - " + e);
			MessageDialog.openError(getShell(), "Configuration Error", e.getCause().getMessage());
		}
		else if (e.getCause() instanceof ImportExportFailureException)
		{
			log.error("performFinish() - export failed - " + e, e);
			MessageDialog.openError(getShell(), "Export Error", e.getCause().getMessage());
		}
		else
		{
			log.fatal("performFinish() - unexpected exception during export execution - " + e, e);
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
		log.debug("handleInterrupted() - user cancelled clone data export.");

		MessageDialog.openInformation(getShell(), "Export Cancelled",
				"The export operation was successfully cancelled.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.wizards.AbstractImportExportClonesWizard#handleSuccess()
	 */
	@Override
	protected void handleSuccess()
	{
		log.debug("handleSuccess() - clone data export finished successfully.");

		MessageDialog.openInformation(getShell(), "Export Successful",
				"The export operation was successfully executed.");
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
			log.trace("performImportExportOperation() - monitor: " + monitor);

		assert (cachedSelectedProjects != null && !cachedSelectedProjects.isEmpty()
				&& cachedSelectedToolAdapter != null && cachedOptionMap != null);

		/*
		 * Execute the import.
		 */
		IExportController controller = CPCExportsPlugin.getExportController();
		assert (controller != null);
		IExportTask exportTask = controller.createTask();

		assert (cachedSelectedToolAdapter instanceof IExportToolAdapterDescriptor);
		exportTask.setToolAdapter(cachedSelectedToolAdapter);
		exportTask.setToolAdapterOptions(cachedOptionMap);
		exportTask.setProjects(cachedSelectedProjects);

		controller.executeExport(monitor, exportTask);
	}

}
