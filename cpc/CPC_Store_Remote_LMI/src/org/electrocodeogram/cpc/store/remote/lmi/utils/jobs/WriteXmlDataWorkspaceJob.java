package org.electrocodeogram.cpc.store.remote.lmi.utils.jobs;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.store.remote.lmi.utils.XMLPersistenceUtils;


/**
 * Special worker job which is used by the {@link XMLPersistenceUtils} class
 * to write clone data to cpc xml clone data file.
 * 
 * @author vw
 * 
 * @see XMLPersistenceUtils#writeXmlData(IFile, org.electrocodeogram.cpc.core.api.data.ICloneFile, java.util.List)
 */
public class WriteXmlDataWorkspaceJob extends WorkspaceJob
{
	private static final Log log = LogFactory.getLog(WriteXmlDataWorkspaceJob.class);

	private IFile cpcFile;
	private String content;

	public WriteXmlDataWorkspaceJob(IFolder cpcFolder, IFile cpcFile, String content)
	{
		super("WriteXmlDataWorkspaceJob");

		if (log.isTraceEnabled())
			log.trace("WriteXmlDataWorkspaceJob() - cpcFolder: " + cpcFolder + ", cpcFile: " + cpcFile + ", content: "
					+ CoreStringUtils.truncateString(content));
		assert (cpcFile != null && content != null);

		this.cpcFile = cpcFile;
		this.content = content;

		this.setRule(cpcFolder);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
	{
		if (log.isTraceEnabled())
			log.trace("runInWorkspace() - going to write file: " + cpcFile);

		try
		{
			InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));

			//check whether we need to create or update the file
			if (!cpcFile.exists())
			{
				//create the file
				cpcFile.create(is, true, null);
			}
			else
			{
				//update/overwrite the file
				markReadOnly(false);
				cpcFile.setContents(is, true, false, null);
			}

			//make sure the file is write protected
			markReadOnly(true);
		}
		catch (UnsupportedEncodingException e)
		{
			log.error("runInWorkspace() - exception while writing file data - file: " + cpcFile + " - " + e, e);
		}
		catch (CoreException e)
		{
			log.error("runInWorkspace() - exception while writing file data - file: " + cpcFile + " - " + e, e);
		}

		//TODO: should we return something other than OK_STATUS in case of one of the errors above?
		return Status.OK_STATUS;
	}

	private void markReadOnly(boolean readOnly) throws CoreException
	{
		ResourceAttributes attributes = cpcFile.getResourceAttributes();
		if (attributes != null)
		{
			attributes.setReadOnly(readOnly);
			cpcFile.setResourceAttributes(attributes);
		}
		else if (log.isDebugEnabled())
			log
					.debug("markReadOnly() - unable to mark file read-only, resource attributes are null - file: "
							+ cpcFile);
	}
}
