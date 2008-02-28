package org.electrocodeogram.cpc.store.remote.lmi.utils.jobs;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


public class DeleteFileWorkspaceJob extends WorkspaceJob
{
	private static final Log log = LogFactory.getLog(DeleteFileWorkspaceJob.class);

	private IFile cpcFile;

	public DeleteFileWorkspaceJob(IFile cpcFile)
	{
		super("DeleteFileWorkspaceJob");

		if (log.isTraceEnabled())
			log.trace("DeleteFileWorkspaceJob() - cpcFile: " + cpcFile);
		assert (cpcFile != null);

		this.cpcFile = cpcFile;

		//lock the parent folder
		this.setRule(cpcFile.getParent());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
	{
		if (log.isTraceEnabled())
			log.trace("runInWorkspace() - going to remove file: " + cpcFile);

		try
		{
			cpcFile.delete(true, false, null);
		}
		catch (CoreException e)
		{
			log.error("runInWorkspace() - unable to remove file: " + cpcFile + " - " + e, e);
		}

		return Status.OK_STATUS;
	}
}
