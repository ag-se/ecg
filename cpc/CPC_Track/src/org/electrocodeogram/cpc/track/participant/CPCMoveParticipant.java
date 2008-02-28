package org.electrocodeogram.cpc.track.participant;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * @deprecated For exploration only, currently not used. Might prove to be a sensible replacement for
 * 		the CPCResourceChangeListener for file rename/move detection.
 */
@Deprecated
public class CPCMoveParticipant extends MoveParticipant
{
	private static final Log log = LogFactory.getLog(CPCMoveParticipant.class);

	private ICompilationUnit cuElement;

	public CPCMoveParticipant()
	{
		log.trace("CPCMoveParticipant()");
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

		if (element instanceof ICompilationUnit && getArguments().getDestination() instanceof IPackageFragment)
		{
			try
			{
				cuElement = (ICompilationUnit) element;

				IResource resource = cuElement.getUnderlyingResource();
				IResource destination = ((IPackageFragment) getArguments().getDestination()).getCorrespondingResource();

				if (log.isTraceEnabled())
					log.trace("initialize() - move operation initialising - resource: " + resource
							+ ", destination folder: " + destination);

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
		IResource destination = ((IPackageFragment) getArguments().getDestination()).getCorrespondingResource();

		String oldProject = CoreUtils.getProjectnameFromFile(resource);
		String oldPath = resource.getProjectRelativePath().toString();

		String newProject = CoreUtils.getProjectnameFromFile(destination);
		//get the directory
		IPath relPath = destination.getProjectRelativePath();
		relPath = relPath.addTrailingSeparator();
		relPath = relPath.append(cuElement.getElementName());
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
		return "CPCMoveParticipant";
	}

}
