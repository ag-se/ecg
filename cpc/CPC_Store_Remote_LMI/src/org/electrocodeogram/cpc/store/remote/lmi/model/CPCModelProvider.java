package org.electrocodeogram.cpc.store.remote.lmi.model;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.mapping.ModelStatus;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.electrocodeogram.cpc.store.remote.lmi.CPCStoreRemoteLMIPlugin;
import org.electrocodeogram.cpc.store.remote.lmi.utils.XMLPersistenceUtils;


public class CPCModelProvider extends org.eclipse.core.resources.mapping.ModelProvider
{
	private static Log log = LogFactory.getLog(CPCModelProvider.class);

	public static final String PROVIDER_ID = "org.electrocodeogram.cpc.store.remote.lmi.modelProvider";

	public CPCModelProvider()
	{
		log.trace("CPCModelProvider()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ModelProvider#validateChange(org.eclipse.core.resources.IResourceDelta, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus validateChange(IResourceDelta delta, IProgressMonitor monitor)
	{
		if (log.isTraceEnabled())
			log.trace("validateChange() - delta: " + delta + ", monitor: " + monitor);

		List<IStatus> problems = new LinkedList<IStatus>();
		try
		{
			delta.accept(new ResourceDeltaVisitor(delta, problems));
		}
		catch (CoreException e)
		{
			log.error("validateChange() - error while validating resource change - delta: " + delta + " - " + e, e);
		}

		if (problems.isEmpty())
			return super.validateChange(delta, monitor);

		/*
		 * At this point we have some pending error messages.
		 * We need to object to the change here.
		 */
		if (problems.size() == 1)
			return (IStatus) problems.get(0);

		return new MultiStatus(CPCStoreRemoteLMIPlugin.PLUGIN_ID, 0, (IStatus[]) problems.toArray(new IStatus[problems
				.size()]), "CPC: Multiple potential side effects have been found.", null);
	}

	/*
	@Override
	public ResourceMapping[] getMappings(IResource resource, ResourceMappingContext context, IProgressMonitor monitor)
			throws CoreException
	{
		if (log.isTraceEnabled())
			log.trace("getMappings() - resource: " + resource + ", context: " + context + ", monitor: " + monitor);

		return super.getMappings(resource, context, monitor);
		//return null;
		//return new ResourceMapping[0];
	}

	@Override
	public ResourceMapping[] getMappings(IResource[] resources, ResourceMappingContext context, IProgressMonitor monitor)
			throws CoreException
	{
		if (log.isTraceEnabled())
		{
			log.trace("getMappings() - resources: " + resources + ", context: " + context + ", monitor: " + monitor);

			if (resources != null)
				for (IResource res : resources)
					log.trace("  resource: " + res);
		}

		return super.getMappings(resources, context, monitor);
		//return null;
		//return new ResourceMapping[0];
	}
	*/

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ModelProvider#getMappings(org.eclipse.core.resources.mapping.ResourceTraversal[], org.eclipse.core.resources.mapping.ResourceMappingContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public ResourceMapping[] getMappings(ResourceTraversal[] traversals, ResourceMappingContext context,
			IProgressMonitor monitor) throws CoreException
	{
		if (log.isTraceEnabled())
			log.trace("getMappings() - traversals: " + traversals + ", context: " + context + ", monitor: " + monitor);

		if (traversals == null)
			return null;

		if (log.isTraceEnabled())
		{
			for (ResourceTraversal trav : traversals)
			{
				log.trace("  traversal: " + trav + ", depth: " + trav.getDepth() + ", flags: " + trav.getFlags());
				for (IResource res : trav.getResources())
				{
					log.trace("    resource: " + res);
				}
			}
		}

		//check the lists of resources in the traversal for java files and create a mapping for
		//each java file
		List<ResourceMapping> mappings = new LinkedList<ResourceMapping>();
		for (ResourceTraversal trav : traversals)
		{
			trav.accept(new MappingResouceVisitor(mappings));
		}

		if (log.isTraceEnabled())
			log.trace("resulting mappings: " + mappings);

		if (!mappings.isEmpty())
			//ok, we got result mappings
			return mappings.toArray(new ResourceMapping[mappings.size()]);

		return super.getMappings(traversals, context, monitor);
		//return new ResourceMapping[0];
	}

	/**
	 * Creates a {@link ResourceMapping} for the given {@link IResource}.<br/>
	 * The type of mapping returned ({@link CPCFolderResourceMapping}, {@link CPCJavaResourceMapping},
	 * {@link CPCDataResourceMapping}) depends on the type and name of the given resource.
	 * 
	 * @param resource the resource to generate a {@link CPCResourceMapping} for, never null.
	 * @return a {@link CPCResourceMapping} or NULL if this is not a cpc resource.
	 * 
	 * TODO: reuse code of {@link MappingResouceVisitor} here?
	 */
	public static ResourceMapping createMapping(IResource resource)
	{
		if (log.isTraceEnabled())
			log.trace("createMapping() - resource: " + resource);
		assert (resource != null);

		if (resource instanceof IFile)
		{
			IFile file = (IFile) resource;

			//TODO: add support for other source files here?
			if ("java".equalsIgnoreCase(file.getFileExtension()))
			{
				//a java source file
				return new CPCJavaResourceMapping(file);
			}
			else if (XMLPersistenceUtils.XML_PERSISTENCE_EXTENSION.equalsIgnoreCase(file.getFileExtension()))
			{
				//a cpc data file
				return new CPCDataResourceMapping(file);
			}
		}
		else if (resource instanceof IFolder)
		{
			return new CPCFolderResourceMapping((IFolder) resource);
		}

		//we don't have a cpc resource mapping for this type
		return null;
	}

	/**
	 * Checks whether the given resource is a CPC data file.<br/>
	 * Typically such files are located within a "<em>.cpc</em>" folder and have
	 * a file extension of "<em>cpc</em>".
	 * 
	 * @param resource the resource to check, never null.
	 * @return true if this is a CPC data file, false otherwise.
	 */
	public static boolean isCPCDataResource(IResource resource)
	{
		if (log.isTraceEnabled())
			log.trace("isCPCDataResource() - resource: " + resource);
		assert (resource != null);

		if (!(resource instanceof IFile))
			return false;

		if (!XMLPersistenceUtils.XML_PERSISTENCE_EXTENSION.equalsIgnoreCase(((IFile) resource).getFileExtension()))
			return false;

		//TODO: additionally, we could also check whether the file is located within a cpc data folder.

		//ok, it is a cpc data file
		return true;
	}

	/**
	 * {@link IResourceDeltaVisitor} which is used by {@link CPCModelProvider#validateChange(IResourceDelta, IProgressMonitor)}
	 * to build a list of error messages which highlight problems that could arise by the given user action.<br/>
	 * If no problems are detected, nothing is added to the error message list.<br/>
	 * <br/>
	 * For now we only ensure that cpc data files are not deleted manually by the user.
	 * 
	 * @author vw
	 */
	private class ResourceDeltaVisitor implements IResourceDeltaVisitor
	{
		private List<IStatus> problems;
		private IResourceDelta rootDelta;

		public ResourceDeltaVisitor(IResourceDelta rootDelta, List<IStatus> errorMessages)
		{
			this.rootDelta = rootDelta;
			this.problems = errorMessages;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException
		{
			IResource res = delta.getResource();

			if (res == null)
			{
				log.warn("ResourceDeltaVisitor.visit() - resource in delta is null: " + delta);
				return false;
			}

			//we don't want to process derived resources/directories
			//i.e. the build dir (usually "/bin").
			if (res.isDerived())
			{
				if (log.isTraceEnabled())
					log.trace("ResourceDeltaVisitor.visit() - skipping derived resource: " + res);

				return false;
			}

			//we're only interested in files
			if (res.getType() == IResource.FILE)
			{
				// a file
				IFile file = (IFile) res;

				if (XMLPersistenceUtils.XML_PERSISTENCE_EXTENSION.equalsIgnoreCase(file.getFileExtension()))
				{
					if (log.isTraceEnabled())
						log.trace("ResourceDeltaVisitor.visit() - cpc file: " + res);

					//make sure this file is actually located within a cpc data folder
					if (!XMLPersistenceUtils.XML_PERSISTENCE_DIRECTORY.equalsIgnoreCase(res.getParent().getName()))
					{
						log
								.warn("ResourceDeltaVisitor.visit() - file with cpc extension found outside of cpc data folder, ignoring: "
										+ res);
						return true;
					}

					if (delta.getKind() == IResourceDelta.REMOVED)
					{
						/*
						 * This cpc file was just deleted.
						 * Lets make sure that the corresponding source file was also removed.
						 */

						IFile sourceFile = XMLPersistenceUtils.getSourceFileForCPCDataResource(file);
						if (sourceFile != null && sourceFile.exists())
						{
							//still exists in the workspace, but maybe it is going to be deleted by
							//this delta too?
							IResourceDelta sourceDelta = rootDelta.findMember(sourceFile.getFullPath());
							if (sourceDelta == null || sourceDelta.getKind() != IResourceDelta.REMOVED)
							{
								/*
								 * The source file still exists. This is a potential problem.
								 */
								String errorMessage = "CPC: Illegal removal of cpc data file: " + file;
								log.debug(errorMessage);
								problems.add(new ModelStatus(IStatus.ERROR, CPCStoreRemoteLMIPlugin.PLUGIN_ID,
										getDescriptor().getId(), errorMessage));
							}
						}
					}
				}
			}

			return true;
		}

	}

	private class MappingResouceVisitor implements IResourceVisitor
	{
		private List<ResourceMapping> mappings;

		public MappingResouceVisitor(List<ResourceMapping> mappings)
		{
			this.mappings = mappings;
		}

		public boolean visit(IResource res)
		{
			if (log.isTraceEnabled())
				log.trace("visiting resouce: " + res);

			//we don't want to process derived resources/directories
			//i.e. the build dir (usually "/bin").
			if (res.isDerived())
			{
				if (log.isTraceEnabled())
					log.trace("skipping derived resource: " + res);

				return false;
			}
			//TODO: atm eclipse is copying our cpc files to the build dir, we should prevent that

			//res.isPhantom()
			//TODO: do we need to check for phantoms here?

			if (res.getType() == IResource.FOLDER)
			{
				// a directory
				IFolder folder = (IFolder) res;

				if (XMLPersistenceUtils.XML_PERSISTENCE_DIRECTORY.equalsIgnoreCase(folder.getName()))
				{
					if (log.isTraceEnabled())
						log.trace("cpc dir: " + res);
				}
				else
				{
					if (log.isTraceEnabled())
						log.trace("other dir: " + res);
				}

				mappings.add(new CPCFolderResourceMapping(folder));
			}
			else if (res.getType() == IResource.FILE)
			{
				// a file
				IFile file = (IFile) res;

				//TODO: add support for other source files here?
				if ("java".equalsIgnoreCase(file.getFileExtension()))
				{
					if (log.isTraceEnabled())
						log.trace("java source file: " + res);

					//Object cpcmap = file.getAdapter(ResourceMapping.class);
					//log.trace("cpc adapter: " + cpcmap);

					mappings.add(new CPCJavaResourceMapping(file));
				}
				else if (XMLPersistenceUtils.XML_PERSISTENCE_EXTENSION.equalsIgnoreCase(file.getFileExtension()))
				{
					if (log.isTraceEnabled())
						log.trace("cpc file: " + res);

					//make sure this file is actually located within a cpc data folder
					if (!XMLPersistenceUtils.XML_PERSISTENCE_DIRECTORY.equalsIgnoreCase(res.getParent().getName()))
					{
						log
								.warn("MappingResouceVisitor.visit() - file with cpc extension found outside of cpc data folder, ignoring: "
										+ res);
						return true;
					}

					mappings.add(new CPCDataResourceMapping(file));
				}
				else
				{
					if (log.isTraceEnabled())
						log.trace("non-source file: " + res);
				}
			}

			return true;
		}
	}

}
