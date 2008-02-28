package org.electrocodeogram.cpc.sensor.team.rp.listener;


import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.ISubscriberChangeEvent;
import org.eclipse.team.core.subscribers.ISubscriberChangeListener;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseTeamEvent;
import org.electrocodeogram.cpc.core.utils.CoreConfigurationUtils;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * 
 * TODO: Consider extracting all Eclipse CVS/Subversive SVN/Subclipse SVN specific code into separate plugins.
 * 
 * @author vw
 */
public class CPCTeamListener implements ISubscriberChangeListener, IJobChangeListener
{
	private static final Log log = LogFactory.getLog(CPCTeamListener.class);

	private enum State
	{
		NONE, COMMIT, UPDATE
	}

	/**
	 * The current team action {@link CPCTeamListener.State}. 
	 */
	private State teamActionState = State.NONE;

	/**
	 * The Eclipse CVS plugin (and maybe other team providers too) will sometimes use multiple
	 * jobs during a team action. This means that we can't just reset the <em>teamActionState</em>
	 * when a team action was finished. It might have been a nested action.
	 * This value is used to detect nesting. It is increased for every team action started and
	 * decreased for every team action finished.
	 */
	private int stateDepth = 0;

	/**
	 * A list of fully qualified class names which correspond to the job runnable classes
	 * which may do CVS Updates.
	 * 
	 * @see CPCTeamListener#isCVSUpdateJob(IJobChangeEvent)
	 */
	private static final List<String> cvsUpdateClassList = Arrays.asList(
			"org.eclipse.team.internal.ccvs.ui.operations.UpdateOperation",
			"org.eclipse.team.internal.ccvs.ui.mappings.ModelUpdateOperation");

	/**
	 * A list of jobs in which we have no interest.<br/>
	 * The sole purpose of this list is to suppress "job spam" if TRACE logging is enabled
	 * during debugging. This list has no effect on the function of this listener.
	 */
	private static final Set<String> ignoreJobClassList = new HashSet<String>(Arrays.asList(
			"org.eclipse.ui.internal.progress.AnimationManager$1",
			"org.eclipse.ui.internal.decorators.DecorationScheduler$1",
			"org.eclipse.ui.internal.decorators.DecorationScheduler$2",
			"org.eclipse.ui.internal.decorators.DecorationScheduler$3",
			"org.eclipse.ui.internal.progress.ProgressViewUpdater$1",
			"org.eclipse.ui.internal.ide.application.IDEIdleHelper$3",
			"org.eclipse.jdt.internal.ui.viewsupport.SelectionListenerWithASTManager$3",
			"org.eclipse.core.internal.events.AutoBuildJob",
			"org.eclipse.ui.internal.activities.MutableActivityManager$2",
			"org.eclipse.core.internal.utils.StringPoolJob",
			"org.eclipse.ui.internal.console.IOConsolePartitioner$QueueProcessingJob",
			"org.eclipse.core.internal.events.NotificationManager$NotifyJob",
			"org.eclipse.ui.internal.progress.WorkbenchSiteProgressService$SiteUpdateJob",
			"org.eclipse.ui.internal.progress.ProgressMonitorFocusJobDialog$3",
			"org.eclipse.ui.internal.progress.ProgressMonitorFocusJobDialog$14",
			"org.eclipse.ui.internal.progress.ProgressManager$2", "org.eclipse.ui.internal.progress.ProgressManager$4",
			"org.eclipse.ui.internal.navigator.extensions.StructuredViewerManager$1",
			"org.eclipse.ui.internal.Workbench$54",
			"org.eclipse.ui.internal.editors.quickdiff.LastSaveReferenceProvider$ReadJob",
			"org.eclipse.jdt.internal.core.search.processing.JobManager$1$ProgressJob",
			"org.eclipse.ui.internal.texteditor.quickdiff.DocumentLineDiffer$2",
			"org.eclipse.ui.internal.console.ConsoleManager$ShowConsoleViewJob",
			"org.eclipse.ui.internal.console.ConsolePatternMatcher$MatchJob",
			"org.eclipse.ui.console.TextConsoleViewer$3", "org.eclipse.core.internal.resources.DelayedSnapshotJob",
			"org.eclipse.ui.internal.RectangleAnimation",
			"org.eclipse.debug.internal.ui.contextlaunching.LaunchingResourceManager$2"));

	public CPCTeamListener()
	{
		log.trace("CPCTeamListener()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISubscriberChangeListener#subscriberResourceChanged(org.eclipse.team.core.subscribers.ISubscriberChangeEvent[])
	 */
	@Override
	public void subscriberResourceChanged(ISubscriberChangeEvent[] deltas)
	{
		if (log.isDebugEnabled())
			log.debug("subscriberResourceChanged() - deltas: " + CoreUtils.arrayToString(deltas)
					+ " - current teamActionState: " + teamActionState);

		if (deltas == null || deltas.length == 0)
			return;

		//		TeamOperationEvent newEvent = new TeamOperationEvent();
		//		List<TeamOperationEvent.TeamOperationFile> affectedFiles = new LinkedList<TeamOperationEvent.TeamOperationFile>();

		for (ISubscriberChangeEvent event : deltas)
		{
			IResource resource = event.getResource();

			if (resource == null)
			{
				//does this ever happen?
				log.error(
						"subscriberResourceChanged() - resource in ISubscriberChangeEvent is null, skipping event - event: "
								+ event, new Throwable());
				continue;
			}

			//we're only interested in files
			if (resource.getType() != IResource.FILE)
				continue;

			//we're only interested in files of supported types
			if (!CoreConfigurationUtils.isSupportedFile(resource) && !CoreConfigurationUtils.isCpcFile(resource))
			{
				if (log.isDebugEnabled())
					log.debug("subscriberResourceChanged() - ignoring file of unsupported type - file: " + resource);
				continue;
			}

			if (log.isDebugEnabled())
				log.debug("subscriberResourceChanged() - flags: " + event.getFlags() + ", resource: " + resource);

			try
			{
				SyncInfo sync = event.getSubscriber().getSyncInfo(resource);

				if (log.isDebugEnabled())
					log.debug("subscriberResourceChanged() - syncInfo: " + sync);

				if (sync == null)
					continue;

				//get old and new revision identifiers
				String oldRevision = CoreFileUtils.getFileRevisionProperty((IFile) resource);
				String newRevision = sync.getLocalContentIdentifier();

				if (log.isDebugEnabled())
				{
					log.debug("subscriberResourceChanged() - Old-Revision: " + oldRevision);
					log.debug("subscriberResourceChanged() - New-Revision: " + newRevision);
					log.debug("subscriberResourceChanged() - Base: " + sync.getBase());
					log.debug("subscriberResourceChanged() - Local: " + sync.getLocal());
					log.debug("subscriberResourceChanged() - Remote: " + sync.getRemote());
				}

				/*
				 * We're only sending out events if:
				 * 
				 *  1) we're aware of a currently pending team action (otherwise we wouldn't be able
				 *     to decide whether this is an update or a commit anyway)
				 *     
				 *  2) some sanity checks hold
				 *  	a) try to identify deletions  
				 *  
				 *   	b) if this is a COMMIT or UPDATE(/MERGE), then local and remote resource revision must be equal
				 *   
				 *   	c) the new revision must differ from the old revision 
				 *  
				 * 		TODO: should we look at the sync state here too? (in sync/incoming/outgoing/conflict)
				 */

				// 1) check current team action
				if (teamActionState.equals(State.NONE))
				{
					/*
					 * Integrity Check.
					 * At this point old and new revisions should be equal.
					 */
					if (!revisionsEqual(oldRevision, newRevision))
					{
						log.error(
								"subscriberResourceChanged() - not inside of a commit or update action but revision changed - old: "
										+ oldRevision + ", new: " + newRevision + ", resource: " + resource,
								new Throwable());
					}
					continue;
				}

				// 2a) maybe this is a deletion?
				if (
				//local and remote identifier are NULL
				((sync.getLocalContentIdentifier() == null || sync.getBase() == null) && (sync.getRemote() == null || sync
						.getRemote().getContentIdentifier() == null)))
				{
					log
							.warn("subscriberResourceChanged() - ignored team event because it might be a deletion request - local: "
									+ sync.getLocalContentIdentifier()
									+ ", base: "
									+ sync.getBase()
									+ ", remote: "
									+ sync.getRemote());
					continue;
				}

				// 2b) make sure that local and remote resource revisions match
				if (sync.getLocalContentIdentifier() == null
						|| !sync.getLocalContentIdentifier().equals(sync.getRemote().getContentIdentifier()))
				{
					log
							.warn("subscriberResourceChanged() - ignored team event because local and remote revisions don't match - local: "
									+ sync.getLocalContentIdentifier()
									+ ", remote: "
									+ sync.getRemote().getContentIdentifier());
					continue;
				}

				// 2c) make sure that the revision of the file was modified
				if (revisionsEqual(oldRevision, newRevision))
				{
					log
							.warn("subscriberResourceChanged() - ignored team event because old and new revisions are equal - old: "
									+ oldRevision + ", new: " + newRevision);
					continue;
				}

				/*
				 * Ok, this is a real team action.
				 * Let's generate a team event.
				 */

				if (log.isDebugEnabled())
					log.debug("subscriberResourceChanged() - resource affected by " + teamActionState
							+ " operation - resource: " + resource + ", new revision: " + newRevision);

				//ok, remember the new revision
				CoreFileUtils.setFileRevisionProperty((IFile) resource, newRevision);

				//remember affected file
				//								affectedFiles.add(newEvent.new TeamOperationFile(resource.getProject().getName(),
				//										resource.getProjectRelativePath().toString(), newRevision));

				//generate team event
				EclipseTeamEvent newEvent = new EclipseTeamEvent(CoreUtils.getUsername(), resource.getProject()
						.getName());

				newEvent.setFilePath(resource.getProjectRelativePath().toString());
				newEvent.setNewRevision(newRevision);
				newEvent.setOldRevision(oldRevision);
				if (teamActionState.equals(State.COMMIT))
					newEvent.setType(EclipseTeamEvent.Type.COMMIT);
				else if (teamActionState.equals(State.UPDATE))
					newEvent.setType(EclipseTeamEvent.Type.UPDATE);
				else
					assert (false);

				if (log.isDebugEnabled())
					log.debug("subscriberResourceChanged() - Team Event detected: " + newEvent);

				//send event
				CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);

			}
			catch (TeamException e)
			{
				log
						.error("subscriberResourceChanged() - error while getting sync info for: " + resource + " - "
								+ e, e);
			}
		}

		//		if (!affectedFiles.isEmpty())
		//		{
		//			newEvent.setAffectedFiles(affectedFiles);
		//			if (teamActionState.equals(State.COMMIT))
		//				newEvent.setType(TeamOperationEvent.Type.COMMIT);
		//			else if (teamActionState.equals(State.UPDATE))
		//				newEvent.setType(TeamOperationEvent.Type.UPDATE);
		//			else
		//				assert (false);
		//
		//			if (log.isDebugEnabled())
		//				log.debug("subscriberResourceChanged() - Team Operation Event generated: " + newEvent);
		//
		//			//dispatch event
		//			CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
		//		}
	}

	/**
	 * @return true if both revisions are either null or equal
	 */
	private boolean revisionsEqual(String oldRev, String newRev)
	{
		if (oldRev == null && newRev == null)
			return true;

		//special case
		// old-rev is null, new-rev is 0
		// can happen during the preparation of an update operation
		if (oldRev == null && "0".equals(newRev))
			return true;

		if (oldRev != null && newRev != null)
			return oldRev.equals(newRev);

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void aboutToRun(IJobChangeEvent event)
	{
		if (log.isTraceEnabled() && !isIgnoreJob(event.getJob()))
			log.trace("aboutToRun() - event: " + event + " - job: " + event.getJob() + " - class: "
					+ event.getJob().getClass());

		/*
		 * Eclipse CVS plugin
		 */
		if (isCVSUpdateJob(event))
		{
			log.debug("CVS Update - pending");
			beginJob(event.getJob(), State.UPDATE);
		}
		else if (isCVSCommitJob(event))
		{
			log.debug("CVS Commit - pending");
			beginJob(event.getJob(), State.COMMIT);
		}

		/*
		 * Subclipse SVN plugin
		 */
		else if (isSVNSubclipseCheckout(event))
		{
			log.debug("SVN Checkout - pending (subclipse)");
			beginJob(event.getJob(), State.UPDATE);
			//TODO: is it correct to handle a checkout like an update?
		}
		else if (isSVNSubclipseUpdate(event))
		{
			log.debug("SVN Update - pending (subclipse)");
			beginJob(event.getJob(), State.UPDATE);
		}
		else if (isSVNSubclipseCommit(event))
		{
			log.debug("SVN Commit - pending (subclipse)");
			beginJob(event.getJob(), State.COMMIT);
		}

		/*
		 * Subversive SVN plugin
		 */
		else if (isSVNSubversiveCheckOut(event))
		{
			log.debug("SVN Check Out - pending (subversive)");
			beginJob(event.getJob(), State.UPDATE);
		}
		else if (isSVNSubversiveUpdate(event))
		{
			log.debug("SVN Update - pending (subversive)");
			beginJob(event.getJob(), State.UPDATE);
		}
		else if (isSVNSubversiveCommit(event))
		{
			log.debug("SVN Commit - pending (subversive)");
			beginJob(event.getJob(), State.COMMIT);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void done(IJobChangeEvent event)
	{
		if (log.isTraceEnabled() && !isIgnoreJob(event.getJob()))
			log.trace("done() - event: " + event + " - job: " + event.getJob() + " - class: "
					+ event.getJob().getClass());

		/*
		 * Eclipse CVS plugin
		 */
		if (isCVSUpdateJob(event))
		{
			log.debug("CVS Update - done");
			endJob(event.getJob(), State.UPDATE);
		}
		else if (isCVSCommitJob(event))
		{
			log.debug("CVS Commit - done");
			endJob(event.getJob(), State.COMMIT);
		}

		/*
		 * Subclipse SVN plugin
		 */
		else if (isSVNSubclipseCheckout(event))
		{
			log.debug("SVN Checkout - done (subclipse)");
			endJob(event.getJob(), State.UPDATE);
			//TODO: is it correct to handle a checkout like an update?
		}
		else if (isSVNSubclipseUpdate(event))
		{
			log.debug("SVN Update - done (subclipse)");
			endJob(event.getJob(), State.UPDATE);
		}
		else if (isSVNSubclipseCommit(event))
		{
			log.debug("SVN Commit - done (subclipse)");
			endJob(event.getJob(), State.COMMIT);
		}

		/*
		 * Subversive SVN plugin
		 */
		else if (isSVNSubversiveCheckOut(event))
		{
			log.debug("SVN Check Out - done (subversive)");
			endJob(event.getJob(), State.UPDATE);
		}
		else if (isSVNSubversiveUpdate(event))
		{
			log.debug("SVN Update - done (subversive)");
			endJob(event.getJob(), State.UPDATE);
		}
		else if (isSVNSubversiveCommit(event))
		{
			log.debug("SVN Commit - done (subversive)");
			endJob(event.getJob(), State.COMMIT);
		}
	}

	private void beginJob(Job job, State state)
	{
		assert (job != null && state != null);

		if (stateDepth > 0 && !state.equals(teamActionState))
		{
			//Changing states in nested jobs are not allowed.
			log.error("beginJob() - state change in nested job - job: " + job + ", old state: " + teamActionState
					+ ", new state: " + state + ", stateDepth: " + stateDepth, new Throwable());
			return;
		}

		teamActionState = state;
		++stateDepth;
	}

	private void endJob(Job job, State state)
	{
		assert (job != null && state != null);

		if (stateDepth <= 0)
		{
			//more jobs finished than started? That can't be right.
			log.error("endJob() - more jobs ended than started - current job: " + job + ", old state: "
					+ teamActionState + ", new state: " + state + ", stateDepth: " + stateDepth, new Throwable());
			return;
		}
		if (!state.equals(teamActionState))
		{
			//Changing states in nested jobs are not allowed.
			log.error("endJob() - state change while closing job - job: " + job + ", old state: " + teamActionState
					+ ", new state: " + state + ", stateDepth: " + stateDepth, new Throwable());
		}

		--stateDepth;
		if (stateDepth <= 0)
			teamActionState = State.NONE;
	}

	/*
	 * Eclipse CVS
	 */
	private boolean isCVSCommitJob(IJobChangeEvent event)
	{
		return isJobMatch(event, "Committing resources",
				"org.eclipse.team.internal.ccvs.ui.wizards.CommitWizard$AddAndCommitOperation", null);
	}

	private boolean isCVSUpdateJob(IJobChangeEvent event)
	{
		/*
		 * First check for a normal CVS Update operation.
		 */
		if (isJobMatch(event, "CVS Update", null, cvsUpdateClassList))
			return true;

		/*
		 * Now check for a synchronise view update action.
		 */
		if (isJobRegExpMatch(event, "Merging \\d+ resource.*",
				"org.eclipse.team.internal.ui.mapping.ResourceMergeHandler$1", null))
			return true;

		/*
		 * Now check for a synchronise view mark as merged action.
		 */
		if (isJobRegExpMatch(event, "Marking \\d+ resource.* as merged\\.",
				"org.eclipse.team.internal.ui.mapping.ResourceMarkAsMergedHandler$1", null))
			return true;

		/*
		 * Now check for a synchronise view overwrite and update action.
		 */
		if (isJobRegExpMatch(event, "Overwriting \\d+ resource.*",
				"org.eclipse.team.internal.ui.mapping.ResourceMergeHandler$1", null))
			return true;

		return false;
	}

	/*
	 * Subclipse SVN
	 */

	private boolean isSVNSubclipseCheckout(IJobChangeEvent event)
	{
		//TODO: add class name
		return isJobMatch(event, "SVN Checkout", null, null);
	}

	private boolean isSVNSubclipseUpdate(IJobChangeEvent event)
	{
		//TODO: add class name
		return isJobMatch(event, "SVN Update", "org.tigris.subversion.subclipse.ui.operations.UpdateOperation", null);
	}

	private boolean isSVNSubclipseCommit(IJobChangeEvent event)
	{
		//TODO: add class name
		return isJobMatch(event, "SVN Commit", "org.tigris.subversion.subclipse.ui.operations.CommitOperation", null);
	}

	/*
	 * Subversive SVN
	 */

	private boolean isSVNSubversiveCheckOut(IJobChangeEvent event)
	{
		//TODO: verify class name
		return isJobMatch(event, "Check Out", "org.eclipse.team.svn.ui.utility.SVNTeamOperationWrapper", null);
	}

	private boolean isSVNSubversiveUpdate(IJobChangeEvent event)
	{
		return isJobMatch(event, "Update", "org.eclipse.team.svn.ui.utility.SVNTeamOperationWrapper", null);
	}

	private boolean isSVNSubversiveCommit(IJobChangeEvent event)
	{
		return isJobMatch(event, "Commit", "org.eclipse.team.svn.ui.utility.SVNTeamOperationWrapper", null);
	}

	private boolean isJobRegExpMatch(IJobChangeEvent event, String jobName, String jobClass, List<String> jobClassList)
	{
		return isJobMatch(event, jobName, true, jobClass, jobClassList);
	}

	private boolean isJobMatch(IJobChangeEvent event, String jobName, String jobClass, List<String> jobClassList)
	{
		return isJobMatch(event, jobName, false, jobClass, jobClassList);
	}

	private boolean isJobMatch(IJobChangeEvent event, String jobName, boolean regExp, String jobClass,
			List<String> jobClassList)
	{
		assert (jobClass == null || jobClassList == null);

		boolean result = false;

		/*
		 * First check job name.
		 */
		if (!regExp)
		{
			if (event.getJob().getName().equals(jobName))
				result = true;
		}
		else
		{
			if (event.getJob().getName().matches(jobName))
				result = true;
		}

		if (log.isTraceEnabled())
		{
			//This is not needed for anything here, we just do it for debugging purposes
			//It is important that this block stays within the if(log.isTraceEnabled()) body.
			debugLogJob(event.getJob());
		}

		/*
		 * Now try to get some class name to check against.
		 * 
		 * This is a bit tricky as the actual team provider class is hidden behind
		 * an Eclipse ResourceJob class, which is internal and private...
		 * Our "workaround" here is to use relfection to directly access the private
		 * field "runnable" which contains the team providers class. 
		 */
		//we're only doing this check if the name check was successful and a job class was given.
		if (result && (jobClass != null || jobClassList != null))
		{
			try
			{
				if (log.isTraceEnabled())
					log.trace("isJobMatch() - class name: " + event.getJob().getClass().getName());

				if (event.getJob().getClass().getName().equals(
						"org.eclipse.team.internal.ui.actions.JobRunnableContext$ResourceJob"))
				{
					Field field = event.getJob().getClass().getDeclaredField("runnable");
					field.setAccessible(true);
					Object runnable = field.get(event.getJob());
					if (log.isTraceEnabled())
						log.trace("isJobMatch() - runnable: " + runnable + " (class: "
								+ (runnable != null ? runnable.getClass() : "null") + ")");

					if (jobClass != null && !runnable.getClass().getName().equals(jobClass))
					{
						log.warn("isJobMatch() - job name matches but job class doesn't - job: " + event.getJob()
								+ " - got class: " + runnable.getClass().getName() + ", expected class: " + jobClass);
					}
					else if (jobClassList != null && !jobClassList.contains(runnable.getClass().getName()))
					{
						log.warn("isJobMatch() - job name matches but job class doesn't - job: " + event.getJob()
								+ " - got class: " + runnable.getClass().getName() + ", expected one of: "
								+ jobClassList);
					}
				}
			}
			catch (Exception e)
			{
				log.warn("isJobMatch() - unable to acquire internal runnable object for job matching - "
						+ event.getJob() + " - " + e, e);
			}
		}

		//we're done
		return result;
	}

	private void debugLogJob(Job job)
	{
		if (isIgnoreJob(job))
			return;

		if (job.getClass().getName().equals("org.eclipse.team.internal.ui.actions.JobRunnableContext$ResourceJob"))
		{
			try
			{
				Field field = job.getClass().getDeclaredField("runnable");
				field.setAccessible(true);
				Object runnable = field.get(job);
				if (log.isTraceEnabled())
					log.trace("debugLogJob() - runnable: " + runnable + " (class: "
							+ (runnable != null ? runnable.getClass() : "null") + ")");
			}
			catch (Exception e)
			{
				log.trace("debugLogJob() - exception during debug runnable class check - " + e);
			}
		}
	}

	private boolean isIgnoreJob(Job job)
	{
		if (ignoreJobClassList.contains(job.getClass().getName()))
			return true;

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#awake(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void awake(IJobChangeEvent event)
	{
		//unused
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#running(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void running(IJobChangeEvent event)
	{
		//unused
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void scheduled(IJobChangeEvent event)
	{
		//unused
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void sleeping(IJobChangeEvent event)
	{
		//unused
	}

}
