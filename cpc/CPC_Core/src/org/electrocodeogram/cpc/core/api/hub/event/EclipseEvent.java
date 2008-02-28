package org.electrocodeogram.cpc.core.api.hub.event;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.utils.CoreConfigurationUtils;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;


/**
 * Abstract parent class for all CPC Events which are created by Eclipse sensors.
 * 
 * @author vw
 * 
 * @see CPCEvent
 */
public abstract class EclipseEvent extends CPCEvent
{
	private static Log log = LogFactory.getLog(EclipseEvent.class);

	protected String user;
	protected String project;
	protected String filePath;

	protected boolean supportedFile;
	protected boolean supportedFileCached = false;
	protected boolean fileLocatedInWorkspace;
	protected boolean fileLocatedInWorkspaceCached = false;

	/**
	 * Creates a new {@link EclipseEvent} instance.
	 * 
	 * @param user username of current user, never null.
	 * @param project name of project, never null.
	 */
	public EclipseEvent(String user, String project)
	{
		if (log.isTraceEnabled())
			log.trace("EclipseEvent() - user: " + user + ", project: " + project);
		assert (user != null && project != null);

		this.user = user;
		this.project = project;
	}

	/**
	 * Retrieves username of the currently logged in user.
	 * 
	 * @return username of current user, never null.
	 */
	public String getUser()
	{
		return user;
	}

	/**
	 * Retrieves the project name for this event.
	 * 
	 * @return project name, never null.
	 */
	public String getProject()
	{
		return project;
	}

	/**
	 * Retrieves the project relative file path for this event.
	 * 
	 * @return path to file, relative to project, never null 
	 */
	public String getFilePath()
	{
		return filePath;
	}

	/**
	 * Sets the project relative file path for this event.
	 * 
	 * @param filePath project relative file path, never null.
	 */
	public void setFilePath(String filePath)
	{
		if (log.isTraceEnabled())
			log.trace("setFilePath(): " + filePath);
		assert (filePath != null);

		checkSeal();

		this.filePath = filePath;
	}

	/**
	 * Caching convenience method which yields the same result as
	 * {@link CoreConfigurationUtils#isSupportedFile(String, String)}.
	 * <br>
	 * However, the value is only calculated once, on first call of this
	 * method and is than reused for all further calls.
	 * <p>
	 * This method is thread safe.
	 * 
	 * @return true if the file is a supported source file, false otherwise.
	 * 
	 * @see CoreConfigurationUtils#isSupportedFile(String, String)
	 */
	public synchronized boolean isSupportedFile()
	{
		if (!supportedFileCached)
		{
			supportedFile = CoreConfigurationUtils.isSupportedFile(project, filePath);
			supportedFileCached = true;
		}

		return supportedFile;
	}

	/**
	 * Caching convenience method which yields the same result as
	 * {@link CoreFileUtils#isFileLocatedInWorkspace(String, String)}.
	 * <br>
	 * However, the value is only calculated once, on first call of this
	 * method and is than reused for all further calls.
	 * <p>
	 * <This method is thread safe.
	 * 
	 * @return true if the file exists <b>and</b> is located within the workspace, false otherwise.
	 * 
	 * @see CoreFileUtils#isFileLocatedInWorkspace(String, String)
	 */
	public synchronized boolean isFileLocatedInWorkspace()
	{
		if (!fileLocatedInWorkspaceCached)
		{
			fileLocatedInWorkspace = CoreFileUtils.isFileLocatedInWorkspace(project, filePath);
			fileLocatedInWorkspaceCached = true;
		}

		return fileLocatedInWorkspace;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.event.CPCEvent#isValid()
	 */
	@Override
	public boolean isValid()
	{
		if (user == null || project == null || filePath == null)
			return false;

		return super.isValid();
	}

	/**
	 * Can be called by sub classes in order to obtain a string which can be included
	 * in their <em>toString()</em> output.
	 * 
	 * @return string representation of the values of this class, never null.
	 */
	protected String subToString()
	{
		return "user: " + user + ", project: " + project + ", filePath: " + filePath;
	}
}
