package org.electrocodeogram.cpc.optimiser.task;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.optimiser.CPCOptimiserPlugin;


/**
 * An abstract task which provides framing for all task which need to do some
 * processing on all clones within the selected projects.
 * 
 * @author vw
 */
public abstract class AbstractGlobalCloneTask extends Job implements IOptimiserTask
{
	private static final Log log = LogFactory.getLog(OrphanDetectionTask.class);

	/**
	 * List of projects which were selected for this task. After initialisation never null.
	 */
	protected List<IProject> projects = null;

	private boolean enableExclusiveStoreProviderLocking;

	/**
	 * @param jobName the name for this job, never null.
	 * @param enableExclusiveStoreProviderLocking <em>true</em> if an exclusive {@link IStoreProvider}
	 * 		write lock should be obtained prior to each call of {@link #handleFile(IFile, ICloneFile)},
	 * 		<em>false</em> otherwise.
	 */
	public AbstractGlobalCloneTask(String jobName, boolean enableExclusiveStoreProviderLocking)
	{
		super(jobName);

		this.enableExclusiveStoreProviderLocking = enableExclusiveStoreProviderLocking;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.optimiser.task.IOptimiserTask#execute(java.util.List)
	 */
	@Override
	public void init(List<IProject> extProjects, Map<String, String> optionsMap)
	{
		if (log.isTraceEnabled())
			log.trace("init() - projects: " + extProjects + ", optionsMap: " + optionsMap);

		this.projects = extProjects;

		/*
		 * Get affected projects.
		 */
		if (projects == null)
		{
			//get all projects
			projects = Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects());
		}

		/*
		 * Get configuration settings.
		 */
		handleOptions(projects, optionsMap);
	}

	/**
	 * Override for option processing.<br/>
	 * Default implementation does nothing.
	 * 
	 * @param projects selected projects, may be empty, never null.
	 * @param optionsMap given options, may be NULL.
	 */
	protected void handleOptions(List<IProject> projects, Map<String, String> optionsMap)
	{

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		if (projects.isEmpty())
		{
			log.trace("run() - no projects selected, skipping.");
			return Status.OK_STATUS;
		}

		//get a list of affected files.
		List<IFile> files = CoreFileUtils.getSupportedFilesInProjects(projects);
		if (files.isEmpty())
		{
			log.trace("run() - no supported files in selected project, skipping.");
			return Status.OK_STATUS;
		}

		//get a store provider instance
		IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);
		assert (storeProvider != null);

		//begin task
		if (monitor != null)
			monitor.beginTask("searching for orphaned clones", files.size());

		for (IFile file : files)
		{
			if (log.isTraceEnabled())
				log.trace("run() - processing: " + file);

			//check if this file is known to the store provider
			ICloneFile cloneFile = CoreUtils.getCloneFileForFile(storeProvider, file, false);
			if (cloneFile == null)
			{
				//this file has no clone data, we can ignore it
				if (monitor != null)
					monitor.worked(1);
				continue;
			}

			if (enableExclusiveStoreProviderLocking)
			{
				//we have no choice but to get lots of small exclusive write locks :|
				try
				{
					storeProvider.acquireWriteLock(LockMode.DEFAULT);

					handleFile(storeProvider, file, cloneFile);
				}
				catch (StoreLockingException e)
				{
					log.error("run() - locking error - " + e, e);
					return new Status(Status.ERROR, CPCOptimiserPlugin.PLUGIN_ID, "locking error - " + e, e);
				}
				finally
				{
					storeProvider.releaseWriteLock();
				}
			}
			else
			{
				try
				{
					handleFile(storeProvider, file, cloneFile);
				}
				catch (StoreLockingException e)
				{
					log.error("run() - locking error, NO LOCK HELD! - " + e, e);
					return new Status(Status.ERROR, CPCOptimiserPlugin.PLUGIN_ID, "locking error - " + e, e);
				}
			}

			//we no longer need the clone data for this file.
			storeProvider.hintPurgeCache(cloneFile);

			//update progress
			if (monitor != null)
			{
				monitor.worked(1);

				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
			}

		}

		//end task
		if (monitor != null)
		{
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;

			monitor.done();
		}

		handleDone();

		log.trace("run() - done.");

		return Status.OK_STATUS;
	}

	/**
	 * Main processing body for this task.<br/>
	 * If <em>enableExclusiveStoreProviderLocking</em> was set, an exclusive write lock is being
	 * held during this call.
	 * 
	 * @param storeProvider valid {@link IStoreProvider} reference, never null.
	 * @param file current file, never null.
	 * @param cloneFile current clone file, never null.
	 */
	protected abstract void handleFile(IStoreProvider storeProvider, IFile file, ICloneFile cloneFile)
			throws IllegalArgumentException, StoreLockingException;

	/**
	 * Executed once the task was completely processed.<br/>
	 * The default implementation does nothing.
	 */
	protected void handleDone()
	{

	}
}
