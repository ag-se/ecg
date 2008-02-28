package org.electrocodeogram.cpc.track.participant;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;


/**
 * @deprecated For exploration only, currently not used. Might prove to be a sensible replacement for
 * 		the CPCResourceChangeListener for file rename/move detection.
 */
@Deprecated
public class CPCRenameOrMoveChange extends Change
{
	private static final Log log = LogFactory.getLog(CPCRenameOrMoveChange.class);

	private Object element;
	private String oldProject;
	private String oldPath;
	private String newProject;
	private String newPath;

	public CPCRenameOrMoveChange(Object element, String oldProject, String oldPath, String newProject, String newPath)
	{
		if (log.isTraceEnabled())
			log.trace("CPCRenameOrMoveChange() - element: " + element + ", oldProject: " + oldProject + ", oldPath: "
					+ oldPath + ", newProject: " + newProject + ", newPath: " + newPath);

		this.element = element;
		this.oldProject = oldProject;
		this.oldPath = oldPath;
		this.newProject = newProject;
		this.newPath = newPath;
	}

	public String getOldProject()
	{
		return oldProject;
	}

	public String getOldPath()
	{
		return oldPath;
	}

	public String getNewProject()
	{
		return newProject;
	}

	public String getNewPath()
	{
		return newPath;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "CPCRenameOrMoveChange[oldProject: " + oldProject + ", oldPath: " + oldPath + ", newProject: "
				+ newProject + ", newPath: " + newPath + "]";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getModifiedElement()
	 */
	@Override
	public Object getModifiedElement()
	{
		return element;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	@Override
	public String getName()
	{
		return toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#initializeValidationData(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void initializeValidationData(IProgressMonitor pm)
	{
		//nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#isValid(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException
	{
		return RefactoringStatus.create(Status.OK_STATUS);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Change perform(IProgressMonitor pm) throws CoreException
	{
		if (log.isTraceEnabled())
			log.trace("perform() - this: " + toString());

		//return undo change
		return new CPCRenameOrMoveChange(element, newProject, newPath, oldProject, oldPath);
	}
}
