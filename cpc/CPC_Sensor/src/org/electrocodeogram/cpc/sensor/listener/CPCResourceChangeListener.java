package org.electrocodeogram.cpc.sensor.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseFileChangeEvent;
import org.electrocodeogram.cpc.core.utils.CoreConfigurationUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


public class CPCResourceChangeListener implements IResourceChangeListener
{
	private static Log log = LogFactory.getLog(CPCResourceChangeListener.class);

	//private static CPCSubscriberChangeListener subscriberChangeListener = new CPCSubscriberChangeListener();

	public CPCResourceChangeListener()
	{
		log.trace("CPCResourceChangeListener()");
	}

	public void resourceChanged(IResourceChangeEvent event)
	{
		/**
		 * some docs:
		 * 
		 * The first category of special events are pre-change notifications.
		 * Since the POST_CHANGE event is broadcast after-the-fact, some valuable information that
		 * the listener needs may be missing. In these cases, use the PRE_CLOSE and PRE_DELETE events,
		 * broadcast before a project is closed or deleted. The project in question can be obtained
		 * from IResourceChangeEvent.getResource. There is no resource delta for these event types.
		 * These events allow listeners to do important cleanup work before a project is removed
		 * from memory. These events do not allow listeners to veto the impending operation.
		 */

		if (log.isTraceEnabled())
		{
			log.trace("resourceChanged(" + event + ")");
			log.trace("type: " + event.getType());
			log.trace("delta: " + event.getDelta());
			log.trace("delta kind: " + resolveResourceDeltaKind(event.getDelta().getKind()));
			log.trace("delta rescource: " + event.getDelta().getResource());
			log.trace("delta rescource path: " + event.getDelta().getResource().getFullPath());
		}

		IResourceDeltaVisitor visitor = new MyResourceDeltaVisitor();
		try
		{
			event.getDelta().accept(visitor);
		}
		catch (CoreException e)
		{
			log.error("resourceChanged() - error while visiting of resource delta - " + e, e);
		}

	}

	protected class MyResourceDeltaVisitor implements IResourceDeltaVisitor
	{

		public boolean visit(IResourceDelta delta) throws CoreException
		{
			if (log.isTraceEnabled())
				log.trace("changed resource: " + delta + ", " + resolveResourceDeltaKind(delta.getKind()) + ", "
						+ resolveResouceDeltaFlags(delta.getFlags()));

			//check if this is a file change event
			if ((delta.getResource() instanceof IFile))
			{
				//				//and the file content was modified.
				//				if ((delta.getKind() == IResourceDelta.CHANGED) && ((delta.getFlags() & IResourceDelta.CONTENT) != 0))
				//				{
				//					//ok, the file content was changed
				//					log.trace("resource change matched filter criteria.");
				//
				//					//now make sure that there is no open editor for this file
				//					if (!CoreUtils.isFileOpenInEditor((IFile) delta.getResource()))
				//					{
				//						log.trace("creating new EclipseFileModifiedEvent.");
				//
				//						EclipseFileModifiedEvent newEvent = new EclipseFileModifiedEvent(CoreUtils.getUsername(), delta
				//								.getResource().getProject().getName());
				//						newEvent.setFilePath(delta.getProjectRelativePath().toString());
				//						CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
				//					}
				//					else
				//					{
				//						log.trace("resource is currently open in an editor, ignored.");
				//					}
				//				}

				//make sure we're interested in this type of file
				if (!CoreConfigurationUtils.isSupportedFile(delta.getResource()))
				{
					log.trace("MyResourceDeltaVisitor.visit() - unsupported file type, ignoring.");
					return true;
				}

				//check whether the entry way moved
				if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0)
				{
					if (log.isTraceEnabled())
						log.trace("  moved to: " + delta.getMovedToPath());

					/*
					 * Special debug logging. While we have some potential issues with
					 * rename/move refactorings, it is a good idea to log file moves
					 * even if debug logging is not enabled.
					 */
					log.info("MyResourceDeltaVisitor.visit() - file moved - from: " + delta.getResource().getFullPath()
							+ ", to: " + delta.getMovedToPath());

					EclipseFileChangeEvent newEvent = new EclipseFileChangeEvent(CoreUtils.getUsername(), delta
							.getResource().getProject().getName());
					newEvent.setFilePath(delta.getProjectRelativePath().toString());
					newEvent.setType(EclipseFileChangeEvent.Type.MOVED);
					newEvent.setNewProject(delta.getMovedToPath().segment(0));
					//the returned string contains the project name, however, newFilePath is a project relative path
					newEvent.setNewFilePath(delta.getMovedToPath().removeFirstSegments(1).toString());
					CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
				}
				/*
				 * The file was removed.
				 * 
				 * Note: this check needs to be an else branch of the MOVED_TO check above as that
				 * 		 event is of kind REMOVED too, but the file is only moved, not deleted.
				 */
				else if (delta.getKind() == IResourceDelta.REMOVED)
				{
					if (log.isTraceEnabled())
						log.trace("  removed.");

					/*
					 * Special debug logging. While we have some potential issues with
					 * rename/move refactorings, it is a good idea to log file deletions
					 * even if debug logging is not enabled.
					 */
					log.info("MyResourceDeltaVisitor.visit() - file deleted - file: "
							+ delta.getResource().getFullPath());

					/*
					 * The tricky part here is that we might not be able to receive the paths
					 * as we're used to, if the project was deleted too.
					 * The CoreUtils methods will take care of these special cases.
					 */
					String project = CoreUtils.getProjectnameFromFile((IFile) delta.getResource());
					String filePath = CoreUtils.getProjectRelativePathFromFile((IFile) delta.getResource());

					if (project == null || filePath == null)
					{
						log.warn(
								"MyResourceDeltaVisitor.visit() - failed to get project name or filePath, skipping - resource: "
										+ delta.getResource() + ", project: " + project + ", filePath: " + filePath,
								new Throwable());
						return true;
					}

					EclipseFileChangeEvent newEvent = new EclipseFileChangeEvent(CoreUtils.getUsername(), project);
					newEvent.setFilePath(filePath);
					newEvent.setType(EclipseFileChangeEvent.Type.REMOVED);
					CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
				}
			}
			//			else if (delta.getResource() instanceof IFolder)
			//			{
			//				/*
			//				 * TODO: Experimental code / exploration only, REMOVE ME
			//				 */
			//
			//				//look at each file in this folder
			//				IFolder folder = (IFolder) delta.getResource();
			//				for (IResource member : folder.members())
			//				{
			//					if (member instanceof IFile)
			//					{
			//						ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(member);
			//						if (cvsResource != null && cvsResource.isManaged())
			//						{
			//							log.trace("CVS RESOURCE - file: " + member + ", cvsResource: " + cvsResource);
			//							log.trace("Syncinfo: " + cvsResource.getSyncInfo());
			//						}
			//
			//						//CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().
			//
			//						//						ICVSFile repoFile = (ICVSFile) member.getAdapter(ICVSFile.class);
			//						//						if (repoFile != null)
			//						//						{
			//						//							if (log.isTraceEnabled())
			//						//							{
			//						//								log.trace("IFile is adaptable to ICVSFile - file: " + member + ", repoFile: "
			//						//										+ repoFile);
			//						//							}
			//						//						}
			//						//						else
			//						//						{
			//						//							if (log.isTraceEnabled())
			//						//								log.trace("NOT adaptable to ICVSFile - file: " + member);
			//						//						}
			//					}
			//				}
			//			}
			//			else if (delta.getResource() instanceof IProject)
			//			{
			//				IProject project = delta.getResource().getProject();
			//				if (RepositoryProvider.isShared(project))
			//				{
			//					log.trace("Project is shared: " + project);
			//
			//					RepositoryProvider rprovider = RepositoryProvider.getProvider(project);
			//					log.trace("RepositoryProvider: " + rprovider);
			//
			//					Subscriber subscriber = rprovider.getSubscriber();
			//					log.trace("Subscriber: " + subscriber);
			//
			//					//RepositoryProviderType.getProviderType("providerid").getSubscriber();
			//
			//					if (subscriber != null)
			//					{
			//						log.trace("registering CPCSubscriberChangeListener...");
			//						subscriber.addListener(subscriberChangeListener);
			//					}
			//				}
			//				else if (log.isTraceEnabled())
			//				{
			//					log.trace("MyResourceDeltaVisitor.visit() - project is not shared: " + project);
			//				}
			//			}
			else
			{
				log.trace("MyResourceDeltaVisitor.visit() - resource change did not match filter criteria, ignored.");
			}

			//visit all children
			return true;
		}
	}

	/**
	 * For debug output only.
	 */
	protected static String resolveResourceDeltaKind(int kind)
	{
		switch (kind)
		{
			case IResourceDelta.ADDED:
				return kind + "[ADDED]";
			case IResourceDelta.REMOVED:
				return kind + "[REMOVED]";
			case IResourceDelta.CHANGED:
				return kind + "[CHANGED]";
			case IResourceDelta.ADDED_PHANTOM:
				return kind + "[ADDED_PHANTOM]";
			case IResourceDelta.REMOVED_PHANTOM:
				return kind + "[REMOVED_PHANTOM]";
			default:
				return kind + "[UNKNOWN KIND]";
		}
	}

	/**
	 * For debug output only.
	 */
	protected static String resolveResouceDeltaFlags(int flags)
	{
		/**
		 * it's not clear whether we can use the SYNC flag for anything,
		 * it seems to be used very differently by the different team providers
		 * 
		 * resource delta flags:
		 *  - CVS does NOT set SYNC flag
		 *  - Subclipse SVN ??? (sets it sometimes)
		 *  - Subversive SVN ???
		 */

		String flagStr = flags + "[";

		if ((flags & IResourceDelta.CONTENT) != 0)
			flagStr += "CONTENT, ";
		if ((flags & IResourceDelta.DESCRIPTION) != 0)
			flagStr += "DESCRIPTION, ";
		if ((flags & IResourceDelta.ENCODING) != 0)
			flagStr += "ENCODING, ";
		if ((flags & IResourceDelta.OPEN) != 0)
			flagStr += "OPEN, ";
		if ((flags & IResourceDelta.MOVED_TO) != 0)
			flagStr += "MOVED_TO, ";
		if ((flags & IResourceDelta.MOVED_FROM) != 0)
			flagStr += "MOVED_FROM, ";
		if ((flags & IResourceDelta.TYPE) != 0)
			flagStr += "TYPE, ";
		if ((flags & IResourceDelta.SYNC) != 0)
			flagStr += "SYNC, ";
		if ((flags & IResourceDelta.MARKERS) != 0)
			flagStr += "MARKERS, ";
		if ((flags & IResourceDelta.REPLACED) != 0)
			flagStr += "REPLACED, ";

		flagStr += "]";

		return flagStr;
	}

}
