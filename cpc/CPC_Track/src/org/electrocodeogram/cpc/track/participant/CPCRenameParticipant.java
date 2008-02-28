package org.electrocodeogram.cpc.track.participant;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * @deprecated For exploration only, currently not used. Might prove to be a sensible replacement for
 * 		the CPCResourceChangeListener for file rename/move detection.
 */
@Deprecated
public class CPCRenameParticipant extends RenameParticipant
{
	private static final Log log = LogFactory.getLog(CPCRenameParticipant.class);

	private ICompilationUnit cuElement;

	public CPCRenameParticipant()
	{
		log.trace("CPCRenameParticipant()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#initialize(java.lang.Object)
	 */
	@Override
	protected boolean initialize(Object element)
	{
		if (log.isTraceEnabled())
			log.trace("initialize() - element: " + element);

		if (element instanceof ICompilationUnit)
		{
			try
			{
				cuElement = (ICompilationUnit) element;

				IResource resource = cuElement.getUnderlyingResource();
				String oldName = cuElement.getElementName();
				String newName = getArguments().getNewName();

				if (log.isTraceEnabled())
					log.trace("initialize() - rename operation initialising - resource: " + resource + ", old name: "
							+ oldName + ", new name: " + newName);

				return true;
			}
			catch (JavaModelException e)
			{
				log.error("initialize() - resource underlying element not found - element: " + element + " - " + e, e);
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#checkConditions(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext)
	 */
	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException
	{
		if (log.isTraceEnabled())
			log.trace("checkConditions() - context: " + context);

		return RefactoringStatus.create(Status.OK_STATUS);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#createChange(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
	{
		log.trace("createChange()");

		IResource resource = cuElement.getUnderlyingResource();
		//String oldName = cuElement.getElementName();
		String newName = getArguments().getNewName();

		String oldProject = CoreUtils.getProjectnameFromFile((IFile) resource);
		String oldPath = resource.getProjectRelativePath().toString();

		String newProject = oldProject;
		//get the directory
		IPath relPath = resource.getProjectRelativePath();
		relPath = relPath.removeLastSegments(1);
		relPath = relPath.addTrailingSeparator();
		relPath = relPath.append(newName);
		String newPath = relPath.toString();

		return new CPCRenameOrMoveChange(cuElement, oldProject, oldPath, newProject, newPath);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#getName()
	 */
	@Override
	public String getName()
	{
		return "CPCRenameParticipant";
	}

}
