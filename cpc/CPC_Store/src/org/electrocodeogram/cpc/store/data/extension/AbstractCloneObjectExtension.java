package org.electrocodeogram.cpc.store.data.extension;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.collection.ICloneObjectExtensionInterfaces;
import org.electrocodeogram.cpc.store.data.AbstractCloneDataElement;


public abstract class AbstractCloneObjectExtension extends AbstractCloneDataElement implements
		ICloneObjectExtensionInterfaces
{
	private static final Log log = LogFactory.getLog(AbstractCloneObjectExtension.class);

	private String parentUuid = null;

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension#setParentUuid(java.lang.String)
	 */
	@Override
	public void setParentUuid(String parentUuid)
	{
		if (log.isTraceEnabled())
			log.trace("setParentUuid() - parentUuid: " + parentUuid);
		assert (parentUuid != null);

		if (this.parentUuid != null && !this.parentUuid.equals(parentUuid))
			throw new IllegalArgumentException("illegal change of parent UUID from " + this.parentUuid + " to "
					+ parentUuid);

		this.parentUuid = parentUuid;
	}

	/**
	 * @return the parentUuid of this extension, may be NULL while the extension hasn't been added to an
	 * 		{@link ICloneObject}. After that it is immutable and will always be non-null.
	 */
	protected String getParentUuid()
	{
		return parentUuid;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension#isPartial()
	 */
	@Override
	public boolean isPartial()
	{
		return false;
	}

}
