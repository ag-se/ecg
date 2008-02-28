package org.electrocodeogram.cpc.core.api.hub.event;


import org.electrocodeogram.cpc.core.api.data.ICloneFile;


/**
 * Abstract base event for all clone data related events.
 * 
 * @author vw
 * 
 * @see CPCEvent
 */
public abstract class CloneEvent extends CPCEvent
{
	private ICloneFile cloneFile;

	/**
	 * Create a new clone event, this abstract constructor needs to be called by all
	 * sub-implementations. The clone file value may be NULL if the event is not
	 * related to a specific file.
	 * 
	 * @param cloneFile the clone file which this event is related to, may be NULL.
	 */
	public CloneEvent(ICloneFile cloneFile)
	{
		this.cloneFile = cloneFile;
	}

	/**
	 * Retrieves the clone file for this event.
	 * <br>
	 * If the event is not specifically related to a single clone file, this value is NULL.
	 * <p>
	 * <b>NOTE:</b> In case of a file deletion this clone file entry may point to a no longer existing file
	 * 		and it may also be no longer possible to retrieve this file or any data about it from the
	 *		store provider.
	 * 
	 * @return the clone file which this event is related to, may be NULL.
	 */
	public ICloneFile getCloneFile()
	{
		return cloneFile;
	}

	/**
	 * Should be called as part of {@link Object#toString()} implementations of sub-classes.
	 * 
	 * @return data values from this class, never null.
	 */
	protected String subToString()
	{
		return "cloneFile: " + cloneFile;
	}

}
