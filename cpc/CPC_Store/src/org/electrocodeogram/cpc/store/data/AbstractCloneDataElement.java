package org.electrocodeogram.cpc.store.data;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.ICloneDataElement;


/**
 * Abstract default implementation of {@link ICloneDataElement}.<br/>
 * Implements seal handling.
 * 
 * @author vw
 */
public class AbstractCloneDataElement implements ICloneDataElement
{
	private static final Log log = LogFactory.getLog(AbstractCloneDataElement.class);

	private boolean sealed = false;

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneDataElement#isSealed()
	 */
	@Override
	public boolean isSealed()
	{
		return sealed;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneDataElement#seal()
	 */
	@Override
	public void seal()
	{
		this.sealed = true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		AbstractCloneDataElement clonedInstance = (AbstractCloneDataElement) super.clone();

		clonedInstance.sealed = false;

		return clonedInstance;
	}

	/**
	 * Ensures that this object has not yet been sealed.
	 * 
	 * @throws IllegalStateException if the object was already sealed.
	 */
	protected void checkSeal()
	{
		if (sealed)
		{
			log.error("checkSeal() - trying to modify a sealed object: " + this, new Throwable());
			throw new IllegalStateException("trying to modify a sealed object");
		}
	}

	/**
	 * Retrieves a character which indicates the seal status of this object.<br/>
	 * Meant for use in <em>toString()</em> methods of subclasses.
	 * @return '#' if sealed, '$' if not sealed.
	 */
	protected char getSealStatus()
	{
		return (this.sealed ? '#' : '$');
	}
}
