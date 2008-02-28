package org.electrocodeogram.eclipse.core.logging;


import java.util.ArrayList;
import java.util.Iterator;


/**
 * Keeps references to all log managers which were created using this logging package.<br/>
 * They are cleaned up on shutdown of this plugin.
 * 
 * @author vw
 */
public class PluginLogRegistry
{
	private ArrayList<ILogManager> logManagers = new ArrayList<ILogManager>();

	/**
	 * Cleans up all log manager known to this plugin. 
	 */
	public void stop() throws Exception
	{
		synchronized (this.logManagers)
		{
			Iterator<ILogManager> it = this.logManagers.iterator();
			while (it.hasNext())
			{
				ILogManager logManager = it.next();
				//casting here isn't nice, however, if we were to add this method to the
				//interface, java would force us to make it public.
				if (logManager instanceof PluginLogManager)
					((PluginLogManager) logManager).internalShutdown();
			}
			this.logManagers.clear();
		}
	}

	/**
	 * Adds a log manager object to the list of active log managers
	 */
	void addLogManager(ILogManager logManager)
	{
		synchronized (this.logManagers)
		{
			if (logManager != null)
				this.logManagers.add(logManager);
		}
	}

	/**
	 * Removes a log manager object from the list of active log managers
	 */
	void removeLogManager(PluginLogManager logManager)
	{
		synchronized (this.logManagers)
		{
			if (logManager != null)
				this.logManagers.remove(logManager);
		}
	}

}
