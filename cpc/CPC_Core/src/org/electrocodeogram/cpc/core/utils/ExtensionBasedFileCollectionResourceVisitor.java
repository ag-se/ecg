package org.electrocodeogram.cpc.core.utils;


import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;


/**
 * An {@link IResourceVisitor} which collects all {@link IFile}s with a given file extensions
 * in a given list. 
 * 
 * @author vw
 */
public class ExtensionBasedFileCollectionResourceVisitor implements IResourceVisitor
{
	private static final Log log = LogFactory.getLog(ExtensionBasedFileCollectionResourceVisitor.class);

	private Set<String> fileExtensions;
	private List<IFile> result;

	/**
	 * 
	 * @param fileExtensions never null
	 * @param result never null
	 */
	public ExtensionBasedFileCollectionResourceVisitor(Set<String> fileExtensions, List<IFile> result)
	{
		assert (fileExtensions != null && result != null);

		if (fileExtensions.isEmpty())
			log
					.warn("ExtensionBasedFileCollectionResourceVisitor() - no file extensions specified, returing empty result.");

		this.fileExtensions = fileExtensions;
		this.result = result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
	 */
	@Override
	public boolean visit(IResource resource) throws CoreException
	{
		//special case: no extensions specified -> empty result
		if (fileExtensions.isEmpty())
			return false;

		if (resource instanceof IFile)
		{
			String extension = ((IFile) resource).getFileExtension();
			if (extension != null && fileExtensions.contains(extension.toLowerCase()))
				result.add((IFile) resource);
		}

		return true;
	}
}
