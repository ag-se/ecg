package org.electrocodeogram.cpc.store.data.extension;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneNonWsPositionExtension;


public class CloneNonWsPositionExtension extends AbstractCloneObjectExtension implements ICloneNonWsPositionExtension
{
	private static Log log = LogFactory.getLog(CloneNonWsPositionExtension.class);

	private static final long serialVersionUID = 1L;

	private int startNonWsOffset = -1;
	private int endNonWsOffset = -1;

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneNonWsPositionExtension#getStartNonWsOffset()
	 */
	@Override
	public int getStartNonWsOffset()
	{
		return startNonWsOffset;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneNonWsPositionExtension#setStartNonWsOffset(int)
	 */
	@Override
	public void setStartNonWsOffset(int startNonWsOffset)
	{
		if (log.isTraceEnabled())
			log.trace("setStartNonWsOffset(): " + startNonWsOffset);

		checkSeal();

		this.startNonWsOffset = startNonWsOffset;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneNonWsPositionExtension#getEndNonWsOffset()
	 */
	@Override
	public int getEndNonWsOffset()
	{
		return endNonWsOffset;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneNonWsPositionExtension#setEndNonWsOffset(int)
	 */
	@Override
	public void setEndNonWsOffset(int endNonWsOffset)
	{
		if (log.isTraceEnabled())
			log.trace("setEndNonWsOffset(): " + endNonWsOffset);

		checkSeal();

		this.endNonWsOffset = endNonWsOffset;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension#getExtensionInterfaceClass()
	 */
	@Override
	public Class<? extends ICloneObjectExtension> getExtensionInterfaceClass()
	{
		return ICloneNonWsPositionExtension.class;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + endNonWsOffset;
		result = prime * result + startNonWsOffset;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CloneNonWsPositionExtension other = (CloneNonWsPositionExtension) obj;
		if (endNonWsOffset != other.endNonWsOffset)
			return false;
		if (startNonWsOffset != other.startNonWsOffset)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return getSealStatus() + "CloneNonWsPositionExtension[startOff: " + startNonWsOffset + ", endOff: "
				+ endNonWsOffset + "]";
	}
}
