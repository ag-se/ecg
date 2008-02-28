package org.electrocodeogram.cpc.core.api.hub.event;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A special team action event which is generated by repository provider specific CPC sensors
 * whenever files are committed to or updated from the repository.
 * 
 * @author vw
 */
public class EclipseTeamEvent extends EclipseEvent
{
	private static Log log = LogFactory.getLog(EclipseTeamEvent.class);

	/**
	 * Type for {@link EclipseTeamEvent}s. 
	 */
	public enum Type
	{
		/**
		 * The file was committed to the repository.
		 * <br>
		 * The revision of the local file will have increased but the
		 * content should be unaffected.
		 */
		COMMIT,

		/**
		 * The file was updated from the repository.
		 * <br>
		 * The revision as well as the content of the local file
		 * will have been affected.
		 */
		UPDATE

		//TODO: do we need to handle moves to specific revisions/branches separately?
	}

	private Type type;
	private String newRevision = null;
	private String oldRevision = null;

	public EclipseTeamEvent(String user, String project)
	{
		super(user, project);

		log.trace("EclipseTeamEvent(...)");
	}

	/**
	 * Retrieves the type of this event.
	 * 
	 * @return the type of this event, never null.
	 */
	public Type getType()
	{
		assert (type != null);
		return type;
	}

	/**
	 * Sets the type of this event.
	 * 
	 * @param type the type of this event, never null.
	 */
	public void setType(Type type)
	{
		if (log.isTraceEnabled())
			log.trace("setType(): " + type);

		checkSeal();

		this.type = type;
	}

	/**
	 * Retrieves the new revision identifier as provided by the repository provider.
	 * <br>
	 * May be NULL, if no revision data was provided by the repository provider.
	 * 
	 * @return new revision identifier for this file version, may be NULL.
	 */
	public String getNewRevision()
	{
		return newRevision;
	}

	/**
	 * Sets the new revision identifier as provided by the repository provider.
	 * 
	 * @param revision new revision identifier for this file version, may be NULL.
	 */
	public void setNewRevision(String revision)
	{
		if (log.isTraceEnabled())
			log.trace("setRevision(): " + revision);

		checkSeal();

		this.newRevision = revision;
	}

	/**
	 * Retrieves the old revision identifier as provided by the repository provider.
	 * <br>
	 * May be NULL, if no old revision data was available.
	 * 
	 * @return old revision identifier for the file before this team action, may be NULL.
	 */
	public String getOldRevision()
	{
		return oldRevision;
	}

	/**
	 * Sets the old revision identifier as provided by the repository provider.
	 * 
	 * @param oldRevision the old revision identifier for the file before this team action, may be NULL.
	 */
	public void setOldRevision(String oldRevision)
	{
		if (log.isTraceEnabled())
			log.trace("setOldRevision(): " + oldRevision);

		checkSeal();

		this.oldRevision = oldRevision;
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

		return super.isValid();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.event.CPCEvent#toString()
	 */
	@Override
	public String toString()
	{
		return "EclipseTeamEvent[" + super.subToString() + ", type: " + type + ", newRev: " + newRevision
				+ ", oldRev: " + oldRevision + "]";
	}

}
