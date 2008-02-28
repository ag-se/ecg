package org.electrocodeogram.cpc.store.remote.lmi.model;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;


public class CPCNature implements IProjectNature
{
	private IProject project;

	public static final String ID = "org.electrocodeogram.cpc.core.cpcnature";

	public void configure() throws CoreException
	{
		// Add nature-specific information
		// for the project, such as adding a builder
		// to a project's build spec.
	}

	public void deconfigure() throws CoreException
	{
		// Remove the nature-specific information here.
	}

	public IProject getProject()
	{
		return project;
	}

	public void setProject(IProject value)
	{
		project = value;
	}

	//TODO: assign this nature somewhere
	/*
	try {
	  IProjectDescription description = project.getDescription();
	  String[] natures = description.getNatureIds();
	  String[] newNatures = new String[natures.length + 1];
	  System.arraycopy(natures, 0, newNatures, 0, natures.length);
	  newNatures[natures.length] = CPCNature.ID;
	  IStatus status = workspace.validateNatureSet(natures);

	  // check the status and decide what to do
	  if (status.getCode() == IStatus.OK) {
	  	description.setNatureIds(newNatures);
	  	project.setDescription(description, null);
	  } else {
	  	// raise a user error
	...
	  }
	} catch (CoreException e) {
	  // Something went wrong
	}
	 */
}
