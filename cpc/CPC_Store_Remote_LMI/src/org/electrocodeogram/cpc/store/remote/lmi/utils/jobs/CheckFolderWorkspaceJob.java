package org.electrocodeogram.cpc.store.remote.lmi.utils.jobs;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


public class CheckFolderWorkspaceJob extends WorkspaceJob
{
	private static final Log log = LogFactory.getLog(CheckFolderWorkspaceJob.class);

	private IFolder cpcFolder;
	private boolean createMode;

	public CheckFolderWorkspaceJob(IFolder cpcFolder, boolean createMode)
	{
		super("CheckFolderWorkspaceJob");

		if (log.isTraceEnabled())
			log.trace("CheckFolderWorkspaceJob() - cpcFolder: " + cpcFolder);
		assert (cpcFolder != null);

		this.cpcFolder = cpcFolder;
		this.createMode = createMode;

		//lock the parent folder
		this.setRule(cpcFolder.getParent());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
	{
		if (log.isTraceEnabled())
			log.trace("runInWorkspace() - going to create/remove folder: " + cpcFolder);

		try
		{
			if (createMode)
				cpcFolder.create(false, true, null);
			else
				cpcFolder.delete(false, null);
		}
		catch (CoreException e)
		{
			log.error("runInWorkspace() - unable to create/remove folder: " + cpcFolder + ", createMode: " + createMode
					+ " - " + e, e);
		}

		return Status.OK_STATUS;
	}

}
