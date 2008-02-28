package org.electrocodeogram.cpc.core.api.hub.event;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * deprecated
 * 
 * @author vw
 * @deprecated this event is no longer in use, please refer to the {@link EclipseTeamEvent}.
 */
@Deprecated
public class TeamOperationEvent extends CPCEvent
{
	private static Log log = LogFactory.getLog(TeamOperationEvent.class);

	/**
	 * @deprecated no longer in use
	 */
	@Deprecated
	public enum Type
	{
		/**
		 * The file was committed to the repository.<br/>
		 * The revision of the local file will have increased but the
		 * content should be unaffected.
		 */
		COMMIT,

		/**
		 * The file was updated from the repository.<br/>
		 * The revision as well as the content of the local file
		 * will have been affected.
		 */
		UPDATE

		//TODO: do we need to handle moves to specific revisions/branches separately?
	}

	private Type type;
	private List<TeamOperationFile> affectedFiles;

	public TeamOperationEvent()
	{
		log.trace("EclipseTeamEvent()");
	}

	/**
	 * @return the type of this event, never null.
	 */
	public Type getType()
	{
		return type;
	}

	public void setType(Type type)
	{
		if (log.isTraceEnabled())
			log.trace("setType(): " + type);

		checkSeal();

		this.type = type;
	}

	public List<TeamOperationFile> getAffectedFiles()
	{
		return affectedFiles;
	}

	public void setAffectedFiles(List<TeamOperationFile> affectedFiles)
	{
		if (log.isTraceEnabled())
			log.trace("setAffectedFiles(): " + affectedFiles);

		checkSeal();

		this.affectedFiles = affectedFiles;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.event.EclipseEvent#isValid()
	 */
	@Override
	public boolean isValid()
	{
		if (type == null)
			return false;

		if (affectedFiles == null || affectedFiles.isEmpty())
			return false;

		return super.isValid();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.event.CPCEvent#toString()
	 */
	@Override
	public String toString()
	{
		return "TeamEvent[type: " + type + ", affectedFiles: " + affectedFiles + "]";
	}

	public class TeamOperationFile
	{
		private String project;
		private String filePath;
		private String revision;

		public TeamOperationFile(String project, String filePath, String revision)
		{
			assert (project != null && filePath != null);

			this.project = project;
			this.filePath = filePath;
			this.revision = revision;
		}

		public String getProject()
		{
			return project;
		}

		public String getFilePath()
		{
			return filePath;
		}

		/**
		 * New revision identifier as provided by the repository provider.<br/>
		 * May be NULL, if no revision data was provided by the repository provider.
		 * 
		 * @return new revision identifier for this file version, may be NULL.
		 */
		public String getRevision()
		{
			return revision;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "TeamOperationFile[project: " + project + ", filePath: " + filePath + ", revision: " + revision
					+ "]";
		}
	}
}
