package org.electrocodeogram.cpc.core.api.hub.event;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubRegistry;


/**
 * Abstract parent class for all types of CPC Events. This is the root of the event class hierarchy.
 * <p>
 * Comparability is defined based on the creation time of an event object.
 * <br>
 * Creation times are guaranteed to be unique.
 * 
 * @author vw
 */
public abstract class CPCEvent implements Comparable<CPCEvent>, Cloneable
{
	private static final Log log = LogFactory.getLog(CPCEvent.class);

	/**
	 * We need to keep track of used creation times to ensure that they are always unique.
	 */
	private static long lastCreationTime = -1;

	private boolean sealed = false;
	private long creationTime;

	/**
	 * Creates a new {@link CPCEvent} instance with a unique creation time.
	 */
	public CPCEvent()
	{
		/*
		 * Ensure unique creation times.
		 */
		synchronized (CPCEvent.class)
		{
			creationTime = System.currentTimeMillis();

			if (creationTime <= lastCreationTime)
				creationTime = lastCreationTime + 1;

			lastCreationTime = creationTime;
		}
	}

	/**
	 * Marks this event as sealed.
	 * <br>
	 * Once sealed, no more modifications to the contents of the event are allowed.
	 * <br>
	 * This method may only be called once per event.
	 * <p>
	 * <b>IMPORTANT:</b> An event is always sealed by the {@link IEventHubRegistry} once the
	 * 		event is being dispatched. The creator of the event, <b>must not</b> call this method.
	 * <p>
	 * Trying to modify a sealed event will throw an IllegalStateException.
	 * <p>
	 * Subclasses may override this method but must ensure that they call it in their new
	 * <em>seal()</em> implementation.
	 */
	public void seal()
	{
		checkSeal();

		sealed = true;
	}

	/**
	 * Ensures that this event has not yet been sealed.
	 * 
	 * @throws IllegalStateException if the event was already sealed.
	 */
	protected void checkSeal()
	{
		if (sealed)
		{
			log.error("checkSeal() - trying to modify a sealed event: " + this, new Throwable());
			throw new IllegalStateException("trying to modify a sealed event");
		}
	}

	/**
	 * Retrieves the creation time of this event in milliseconds.
	 * <br>
	 * The value corresponds to {@link System#currentTimeMillis()} at the time of
	 * the creation of the event object.
	 * <p>
	 * Creation times are guaranteed to be unique. Equality and comparability are
	 * based on event creation times.
	 * 
	 * @return creation time of this event.
	 */
	public long getCreationTime()
	{
		return creationTime;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException
	{
		CPCEvent clone = (CPCEvent) super.clone();

		clone.sealed = false;

		return clone;
	}

	/**
	 * Checks if this event has been fully initialised.
	 * <br>
	 * Will return <em>false</em> if one of the mandatory fields of the event has not yet been filled out.
	 * <p>
	 * Subclasses should override this method but should never return <em>true</em>. Instead they should
	 * delegate to the super class implementation once all validity checks on their level have passed.
	 * <p>
	 * The {@link CPCEvent#isValid()} implementation always returns <em>true</em>.
	 * 
	 * @return <em>true</em> if this event is valid, <em>false</em> otherwise.
	 * 
	 * @see IEventHubRegistry#dispatch(CPCEvent)
	 */
	public boolean isValid()
	{
		//events at this level are always valid
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CPCEvent o)
	{
		int result = (int) (creationTime - o.getCreationTime());

		/*
		 * We do not override hashCode() and equals(), even though this should usually
		 * be done, when compareTo() is implemented. However, two events with equal
		 * creation times are not really equal events.
		 * To make this somewhat sane, we make sure that we return 0 if the two
		 * objects are identical and -1 of they only have the same creation time. 
		 */
		if (this == o)
		{
			result = 0;
		}
		else if (result == 0)
		{
			/*
			 * We have the same creation date for two different events.
			 * This should never happen.
			 */
			log.error("compareTo() - non-unique event creation date detected - this: " + this.creationTime + "@" + this
					+ ", other: " + o.creationTime + "@" + o, new Throwable());
			return -1;
		}

		return result;
	}

	/*
	 * The default equals and hashcode implementation should be sufficient.
	 * We do not copy or clone events anywhere. Equality/hashcode based on
	 * object identity should thus always match equality based on creation time.
	 */

	/**
	 * Every event should implement a sensible toString method for use in debugging log messages.
	 * 
	 * @return debug string representation, never null.
	 */
	@Override
	public abstract String toString();
}
