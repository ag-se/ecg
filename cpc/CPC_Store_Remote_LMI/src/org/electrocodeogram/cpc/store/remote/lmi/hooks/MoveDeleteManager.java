package org.electrocodeogram.cpc.store.remote.lmi.hooks;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.IProgressMonitor;


public class MoveDeleteManager implements IMoveDeleteHook
{

	@Override
	public boolean deleteFile(IResourceTree tree, IFile file, int updateFlags, IProgressMonitor monitor)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteFolder(IResourceTree tree, IFolder folder, int updateFlags, IProgressMonitor monitor)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteProject(IResourceTree tree, IProject project, int updateFlags, IProgressMonitor monitor)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean moveFile(IResourceTree tree, IFile source, IFile destination, int updateFlags,
			IProgressMonitor monitor)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean moveFolder(IResourceTree tree, IFolder source, IFolder destination, int updateFlags,
			IProgressMonitor monitor)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean moveProject(IResourceTree tree, IProject source, IProjectDescription description, int updateFlags,
			IProgressMonitor monitor)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
