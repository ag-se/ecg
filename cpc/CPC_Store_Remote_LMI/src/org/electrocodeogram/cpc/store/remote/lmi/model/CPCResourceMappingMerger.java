package org.electrocodeogram.cpc.store.remote.lmi.model;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IDiffVisitor;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.ResourceMappingMerger;
import org.eclipse.team.core.mapping.provider.MergeStatus;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.core.mapping.provider.SynchronizationContext;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.utils.CoreEditorUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.store.remote.lmi.CPCStoreRemoteLMIPlugin;
import org.electrocodeogram.cpc.store.remote.lmi.utils.LMIUtils;
import org.electrocodeogram.cpc.store.remote.lmi.utils.XMLPersistenceUtils;


public class CPCResourceMappingMerger extends ResourceMappingMerger
{
	private static final Log log = LogFactory.getLog(CPCResourceMappingMerger.class);

	private CPCModelProvider modelProvider;
	private IStoreProvider storeProvider;

	public CPCResourceMappingMerger(CPCModelProvider modelProvider)
	{
		log.trace("CPCResourceMappingMerger()");

		this.modelProvider = modelProvider;

		storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class);
		assert (storeProvider != null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.ResourceMappingMerger#getModelProvider()
	 */
	@Override
	protected ModelProvider getModelProvider()
	{
		if (log.isTraceEnabled())
			log.trace("getModelProvider() - result: " + modelProvider);

		return modelProvider;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.ResourceMappingMerger#merge(org.eclipse.team.core.mapping.IMergeContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus merge(IMergeContext mergeContext, IProgressMonitor monitor) throws CoreException
	{
		if (log.isTraceEnabled())
			log.trace("merge() - mergeContext: " + mergeContext + ", monitor: " + monitor);

		try
		{
			IStatus status;

			// Only override the merge for three-way synchronisations
			if (mergeContext.getType() == SynchronizationContext.THREE_WAY)
			{
				monitor.beginTask("Merging model elements", 100);
				status = mergeModelElements(mergeContext, new SubProgressMonitor(monitor, 50));

				// Stop the merge if there was a failure
				if (!status.isOK())
				{
					if (log.isTraceEnabled())
						log.trace("merge() - merge failed, result: " + status);

					return status;
				}

				// We need to wait for any background processing to complete for the context
				// so the diff tree will be up-to-date when we delegate the rest of the merge
				// to the superclass
				try
				{
					Job.getJobManager().join(mergeContext, new SubProgressMonitor(monitor, 50));
				}
				catch (InterruptedException e)
				{
					// Ignore
				}

				// Delegate the rest of the merge to the superclass
				//status = super.merge(mergeContext, monitor);
				status = Status.OK_STATUS;
			}
			else
			{
				log
						.warn("merge() - TWO WAY merge for cpc model elements. Only THREE WAY merge supported - mergeContext: "
								+ mergeContext);
				//status = super.merge(mergeContext, monitor);
				//TODO: should we return an error here?
				//TODO: should we at least ensure that the cpc xml data file is simply overwritten?
				status = Status.OK_STATUS;
			}

			if (log.isTraceEnabled())
				log.trace("merge() - result: " + status);

			return status;
		}
		finally
		{
			monitor.done();
		}
	}

	/**
	 * Merge all the model element changes in the context
	 */
	private IStatus mergeModelElements(IMergeContext mergeContext, IProgressMonitor monitor) throws CoreException
	{
		if (log.isTraceEnabled())
			log.trace("mergeModelElements() - mergeContext: " + mergeContext);

		try
		{
			IDiff[] modDiffs = getModDiffs(mergeContext);
			List<IDiff> failures = new ArrayList<IDiff>();

			monitor.beginTask(null, 100 * modDiffs.length);

			for (int i = 0; i < modDiffs.length; i++)
			{
				IDiff diff = modDiffs[i];
				if (!mergeModelElement(mergeContext, diff, new SubProgressMonitor(monitor, 100)))
				{
					failures.add(diff);
				}
			}

			if (failures.size() > 0)
			{
				log.warn("mergeModelElements() - merge failed - failures: " + failures);
				return new MergeStatus(CPCStoreRemoteLMIPlugin.PLUGIN_ID, "Several objects could not be merged",
						getMappings(failures));
			}

			return Status.OK_STATUS;
		}
		finally
		{
			monitor.done();
		}
	}

	//TODO: do we need this?
	private ResourceMapping[] getMappings(List<IDiff> diffs)
	{
		if (log.isTraceEnabled())
			log.trace("getMappings() - diffs: " + diffs);

		List<ResourceMapping> mappings = new ArrayList<ResourceMapping>();

		for (Iterator<IDiff> iter = diffs.iterator(); iter.hasNext();)
		{
			IDiff diff = (IDiff) iter.next();
			IResource resource = ResourceDiffTree.getResourceFor(diff);

			ResourceMapping resMapping = CPCModelProvider.createMapping(resource);

			mappings.add(resMapping);
		}

		if (log.isTraceEnabled())
			log.trace("getMappings() - results: " + mappings);

		return (ResourceMapping[]) mappings.toArray(new ResourceMapping[mappings.size()]);
	}

	/**
	 * Return all the diffs for cpc data files.
	 */
	private IDiff[] getModDiffs(IMergeContext mergeContext)
	{
		if (log.isTraceEnabled())
			log.trace("getModDiffs() - mergeContext: " + mergeContext);

		final List<IDiff> result = new ArrayList<IDiff>();

		//mergeContext.getDiffTree().accept(getModelProjectTraversals(mergeContext), new IDiffVisitor()
		mergeContext.getDiffTree().accept(new Path("/"), new IDiffVisitor()
		{
			public boolean visit(IDiff diff)
			{
				IResource resource = ResourceDiffTree.getResourceFor(diff);
				if (CPCModelProvider.isCPCDataResource(resource))
				{
					result.add(diff);
				}
				return true;
			}

		}, IResource.DEPTH_INFINITE);

		if (log.isTraceEnabled())
			log.trace("getModDiffs() - result: " + result);

		return (IDiff[]) result.toArray(new IDiff[result.size()]);
	}

	/**
	 * Merge a CPC data file.
	 */
	private boolean mergeModelElement(IMergeContext mergeContext, IDiff diff, IProgressMonitor monitor)
			throws CoreException
	{
		if (log.isTraceEnabled())
			log.trace("mergeModelElement() - mergeContext: " + mergeContext + ", diff: " + diff);

		if (!(diff instanceof IThreeWayDiff))
		{
			log.error("mergeModelElement() - expecting a three way diff, diff type not supported, ignoring - diff: "
					+ diff, new Throwable());
			return false;
		}

		IThreeWayDiff twd = (IThreeWayDiff) diff;
		if (twd.getDirection() == IThreeWayDiff.INCOMING || twd.getDirection() == IThreeWayDiff.CONFLICTING)
		{
			IResource resource = ResourceDiffTree.getResourceFor(diff);

			// First, check if a change conflicts with a deletion
			if (twd.getDirection() == IThreeWayDiff.CONFLICTING)
			{
				//TODO: should we add any special handling for such conflicts here?
				if (!resource.exists())
				{
					log
							.warn("mergeModelElement() - conflicting with local deletion, skipping. - resource: "
									+ resource);
					return false;
				}
				if (((IResourceDiff) twd.getRemoteChange()).getAfterState() == null)
				{
					log.warn("mergeModelElement() - conflicting with remote deletion, skipping. - resource: "
							+ resource + ", remoteChange: " + twd.getRemoteChange());
					return false;
				}
			}

			// First determine the element files and element file changes
			IResourceDiff remoteChange = (IResourceDiff) twd.getRemoteChange();
			//				IResource[] localElements = getReferencedResources(resource);
			//				IResource[] baseElements = getReferencedResources(resource.getProject().getName(), remoteChange
			//						.getBeforeState(), monitor);
			//				IResource[] remoteElements = getReferencedResources(resource.getProject().getName(), remoteChange
			//						.getAfterState(), monitor);
			//				IResource[] addedElements = getAddedElements(baseElements, remoteElements);
			//				// Trick: The removed elements can be obtained by reversing the base and remote and looking for added
			//				IResource[] removedElements = getAddedElements(remoteElements, baseElements);
			//
			//				// Check to see if any removed elements have changed locally
			//				if (hasOutgoingChanges(mergeContext, removedElements))
			//				{
			//					return false;
			//				}

			// Now try to merge all the element files involved
			//				Set elementFiles = new HashSet();
			//				elementFiles.addAll(Arrays.asList(baseElements));
			//				elementFiles.addAll(Arrays.asList(localElements));
			//				elementFiles.addAll(Arrays.asList(remoteElements));
			//				if (!mergeElementFiles(mergeContext, (IResource[]) elementFiles.toArray(new IResource[elementFiles
			//						.size()]), monitor))
			//				{
			//					return false;
			//				}

			// Finally, merge the model definition
			if (!resource.exists())
			{
				if (log.isTraceEnabled())
					log.trace("mergeModelElement() - file doesn't exist locally, doing normal update - resource: "
							+ resource);

				/*
				 * TODO:
				 * here we need to
				 * - write a copy of the remote source and remote cpc file to temp storage
				 */

				// This is a new model definition so just merge it
				IStatus status = mergeContext.merge(diff, false, monitor);

				if (log.isTraceEnabled())
					log.trace("mergeModelElement() - super merge result: " + status + " - resource: " + resource);

				if (!status.isOK())
					return false;
			}
			else
			{
				if (log.isTraceEnabled())
					log.trace("mergeModelElement() - executing cpc data file merge - resource: " + resource);

				// Update the contents of the model definition file
				//					ModelObjectDefinitionFile file = (ModelObjectDefinitionFile) ModelObject.create(resource);
				//					elementFiles = new HashSet();
				//					elementFiles.addAll(Arrays.asList(localElements));
				//					elementFiles.addAll(Arrays.asList(addedElements));
				//					elementFiles.removeAll(Arrays.asList(removedElements));
				//					file.setElements((IResource[]) elementFiles.toArray(new IResource[elementFiles.size()]));

				//try to get the corresponding java file
				IFile sourceFile = XMLPersistenceUtils.getSourceFileForCPCDataResource(resource);
				if (sourceFile == null || !sourceFile.exists())
				{
					//source file is not part of the diff? this is probably a bug
					log.warn(
							"mergeModelElement() - source file is not part of the mergecontext for data file - data file: "
									+ resource + ", source file: " + sourceFile, new Throwable());
					return false;
				}

				//ok, the have file exists.

				//check if there is a diff pending for it
				IDiff sourceDiff = mergeContext.getDiffTree().getDiff(sourceFile);
				if (sourceDiff == null)
				{
					log.warn("mergeModelElement() - source file was not modified, but cpc data file was? - data file: "
							+ resource + ", source file: " + sourceFile, new Throwable());
					return false;
				}
				//the source file was also modified.

				//look at remote and base versions
				if (!(sourceDiff instanceof IThreeWayDiff))
				{
					log.warn(
							"mergeModelElement() - expecting three way diff, unsuported diff type for source file - diff: "
									+ sourceDiff + ", sourceFile: " + sourceFile, new Throwable());
					return false;
				}

				//ensure that we have a valid handle for the remote file revisions
				IResourceDiff remoteSourceChange = (IResourceDiff) ((IThreeWayDiff) sourceDiff).getRemoteChange();
				if (remoteSourceChange == null)
				{
					log.warn(
							"mergeModelElement() - unable to obtain remote file revision handle for source file - diff: "
									+ sourceDiff + ", sourceFile: " + sourceFile, new Throwable());
					return false;
				}

				//ok, we know that this is a local cpc file, so lets get the cpc data for it
				ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(sourceFile.getProject().getName(),
						sourceFile.getProjectRelativePath().toString(), false, false);
				if (cloneFile == null)
				{
					//the file is unknown? this shouldn't happen. (after all there is a cpc xml data file on disk!)
					log.error(
							"mergeModelElement() - unable to retrieve clone file handle for source file - source file: "
									+ sourceFile + ", cpc data file: " + resource, new Throwable());
					return false;
				}

				/*
				 * As we require all files to be persisted prior to a team operation we can savely
				 * assume that the source file on disk and the cpc xml data file on disk are both
				 * up to date.
				 */

				/*
				 * Source data.
				 */

				String localSourceContent = CoreUtils.readFileContent(sourceFile);
				if (localSourceContent == null)
				{
					log.error("mergeModelElement() - unable to retrieve source file content - source file: "
							+ sourceFile + ", cpc data file: " + resource, new Throwable());
					return false;
				}
				LMIUtils.storeTemporaryFile(cloneFile.getUuid(), "local.src", localSourceContent);

				LMIUtils.storeFileRevision(cloneFile.getUuid(), "base.src", remoteSourceChange.getBeforeState(),
						monitor);
				LMIUtils.storeFileRevision(cloneFile.getUuid(), "remote.src", remoteSourceChange.getAfterState(),
						monitor);

				/*
				 * CPC data.
				 */
				String localCpcContent = CoreUtils.readFileContent((IFile) resource);
				if (localCpcContent == null)
				{
					log.error("mergeModelElement() - unable to retrieve cpc xml data file content - source file: "
							+ sourceFile + ", cpc data file: " + resource, new Throwable());
					return false;
				}
				LMIUtils.storeTemporaryFile(cloneFile.getUuid(), "local.cpc", localCpcContent);

				//get remote and base versions
				LMIUtils.storeFileRevision(cloneFile.getUuid(), "base.cpc", remoteChange.getBeforeState(), monitor);
				LMIUtils.storeFileRevision(cloneFile.getUuid(), "remote.cpc", remoteChange.getAfterState(), monitor);
				//now write local data to "local.cpc"

				//for now we just overwrite the local change
				IStorage remoteStorage = remoteChange.getAfterState().getStorage(monitor);
				((IFile) resource).setContents(remoteStorage.getContents(), IFile.FORCE, monitor);

				// Let the merge context know we handled the file
				mergeContext.markAsMerged(diff, true, monitor);

				//merge it first
				//							if (log.isTraceEnabled())
				//								log.trace("mergeModelElement() - merging source file: " + sourceDiff);
				//							mergeContext.merge(sourceDiff, false, monitor);
				//
				//							try
				//							{
				//								Job.getJobManager().join(mergeContext, new SubProgressMonitor(monitor, 50));
				//							}
				//							catch (InterruptedException e)
				//							{
				//								// Ignore
				//							}
				//
				//							//try to access the source file content
				//							//String postContent = CoreUtils.getFileContentFromEditorOrFile(sourceFile);
				//							//^ doesn't work? still seeing old version? maybe getting the file from an editor is wrong here.
				//							String postContent = CoreUtils.readFileContent(sourceFile);
				//							if (log.isTraceEnabled())
				//							{
				//								log.trace("POST: " + postContent);
				//								log.trace("EQUAL: " + preContent.equals(postContent));
				//							}
			}
		}
		else
		{
			//TODO: this actually happens!
			//17:17:45.859 WARN  [Worker-25] lmi.model.CPCResourceMappingMerger - mergeModelElement() - unexpected diff direction -
			//diff: org.eclipse.team.core.diff.provider.ThreeWayDiff@a41f50d9, direction: 256
			//an outgoing change here?
			//just ignoring it is probably ok, right?
			log.warn("mergeModelElement() - unexpected diff direction - diff: " + twd + ", direction: "
					+ twd.getDirection(), new Throwable());
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.ResourceMappingMerger#validateMerge(org.eclipse.team.core.mapping.IMergeContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus validateMerge(IMergeContext mergeContext, IProgressMonitor monitor)
	{
		if (log.isTraceEnabled())
			log.trace("validateMerge() - mergeContext: " + mergeContext + ", monitor: " + monitor);

		try
		{
			IDiff[] modDiffs = getModDiffs(mergeContext);
			List<IDiff> failures = new ArrayList<IDiff>();

			monitor.beginTask(null, 100 * modDiffs.length);

			for (int i = 0; i < modDiffs.length; i++)
			{
				IDiff diff = modDiffs[i];
				if (!areCorrespondingSourceFilesPresent(mergeContext, diff, new SubProgressMonitor(monitor, 100)))
				{
					failures.add(diff);
				}
			}

			if (failures.size() > 0)
			{
				log.warn("mergeModelElements() - merge validation failed - failures: " + failures);
				return new MergeStatus(
						CPCStoreRemoteLMIPlugin.PLUGIN_ID,
						"CPC: Several objects did not pass pre-merge validation. Make sure all changes are saved and cpc data files and source files are always part of the same repository operation.",
						getMappings(failures));
			}

			return super.validateMerge(mergeContext, monitor);
		}
		finally
		{
			monitor.done();
		}

		//TODO: double check here that the source<->cpc data file pairs are all complete.
		//i.e. no source file without cpc data file and no cpc data file without source file is part of this merge
	}

	/**
	 * Checks for each cpc xml data file whether the corresponding source file is also part of the diff.
	 * TODO: also ensure that all source files are saved  
	 */
	private boolean areCorrespondingSourceFilesPresent(IMergeContext mergeContext, IDiff diff,
			SubProgressMonitor subProgressMonitor)
	{
		if (log.isTraceEnabled())
			log.trace("areCorrespondingSourceFilesPresent() - mergeContext: " + mergeContext + ", diff: " + diff);

		IResource resource = ResourceDiffTree.getResourceFor(diff);
		assert (resource instanceof IFile);

		IFile sourceFile = XMLPersistenceUtils.getSourceFileForCPCDataResource(resource);
		assert (sourceFile != null);

		IDiff sourceDiff = mergeContext.getDiffTree().getDiff(sourceFile);
		if (sourceDiff == null)
		{
			log
					.warn("areCorrespondingSourceFilesPresent() - source file for cpc data file not part of current team operation - cpc data file: "
							+ resource + ", source file: " + sourceFile);
			return false;
		}

		//check if the source file exists locally
		if (sourceFile.exists())
		{
			//ensure that it doesn't contain any unsaved changes
			if (CoreEditorUtils.isFileOpenInEditorAndDirty((IFile) sourceFile))
			{
				log
						.info("areCorrespondingSourceFilesPresent() - source file for cpc data file contains unsaved changes - cpc data file: "
								+ resource + ", source file: " + sourceFile);
				return false;
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.ResourceMappingMerger#getMergeRule(org.eclipse.team.core.mapping.IMergeContext)
	 */
	@Override
	public ISchedulingRule getMergeRule(IMergeContext mergeContext)
	{
		if (log.isTraceEnabled())
			log.trace("getMergeRule() - mergeContext: " + mergeContext);

		return super.getMergeRule(mergeContext);
	}

}
