package org.electrocodeogram.cpc.core.api.hub.event;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * An {@link EclipseFileModifiedEvent} indicates that the content of a file was modified by
 * a background operation. Or more specifically, a file was modified while not being
 * displayed by any of the currently active editors.
 * <p>
 * This is typically caused by refactorings, automated source-code reformatings and external
 * changes to files.
 * 
 * @author vw
 * 
 * @see EclipseFileChangeEvent
 * 
 * @deprecated replaced by {@link EclipseFileChangeEvent}
 */
@Deprecated
public class EclipseFileModifiedEvent extends EclipseEvent
{
	private static Log log = LogFactory.getLog(EclipseFileModifiedEvent.class);

	public EclipseFileModifiedEvent(String user, String project)
	{
		super(user, project);

		log.trace("EclipseFileModifiedEvent(...)");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.event.CPCEvent#toString()
	 */
	@Override
	public String toString()
	{
		return "EclipseFileModifiedEvent[" + super.subToString() + "]";
	}
}
