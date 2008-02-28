package org.electrocodeogram.cpc.store.remote.lmi.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.ClonePersistenceEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.store.remote.lmi.utils.XMLPersistenceUtils;


public class ClonePersistenceListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(ClonePersistenceListener.class);

	public ClonePersistenceListener()
	{
		log.trace("ClonePersistenceListener()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener#processEvent(org.electrocodeogram.cpc.core.api.hub.event.CPCEvent)
	 */
	@Override
	public void processEvent(CPCEvent event)
	{
		if (event instanceof ClonePersistenceEvent)
		{
			processClonePersistenceEvent((ClonePersistenceEvent) event);
		}
		else
		{
			log.error("processEvent() - got event of wrong type: " + event, new Throwable());
		}
	}

	private void processClonePersistenceEvent(ClonePersistenceEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processClonePersistenceEvent() - event: " + event);

		if (event.getCloneFile() == null)
		{
			/*
			 * All clone data was removed.
			 * We should traverse the entire workspace and remove or clear
			 * all our xml files.
			 */

			//TODO: implement this
			log.warn("processClonePersistenceEvent() - full clone data purge cleanup not yet implemented");
		}
		else
		{
			/*
			 * We need to update the xml clone data file for the given clone file.
			 */

			//make sure the project is still accessible
			if (!CoreFileUtils.isProjectAccessible(event.getCloneFile()))
			{
				log
						.warn("processClonePersistenceEvent() - ignoring persistence event for file in closed project - event: "
								+ event);
				return;
			}

			//get the underlying file handle (lets double check that is still exists)
			IFile fileHandle = CoreFileUtils.getFileForCloneFile(event.getCloneFile(), true);
			assert (fileHandle != null);

			/*
			 * Make sure this is a java file.
			 */
			if (!fileHandle.getFileExtension().equalsIgnoreCase("java"))
			{
				if (log.isTraceEnabled())
					log.trace("processClonePersistenceEvent() - ignoring non-java file: " + fileHandle);
				return;
			}

			if (!fileHandle.exists())
			{
				/*
				 * The file was completely deleted.
				 * We should remove our corresponding clone data xml file too.
				 * We should also remove the clone data sub directory if it is now empty.
				 */

				try
				{
					XMLPersistenceUtils.clearXmlData(fileHandle);
				}
				catch (CoreException e)
				{
					log.error("processClonePersistenceEvent() - unable to clear xml data for file - " + fileHandle
							+ " - " + e, e);
				}
			}
			else
			{
				/*
				 * The file still exists.
				 * We only need to update the clone data xml file.
				 */

				try
				{
					XMLPersistenceUtils.writeXmlData(fileHandle, event.getCloneFile(), event.getClones());
				}
				catch (CoreException e)
				{
					log.error("processClonePersistenceEvent() - unable to write xml data for file - " + fileHandle
							+ " - " + e, e);
				}
			}
		}
	}
}
